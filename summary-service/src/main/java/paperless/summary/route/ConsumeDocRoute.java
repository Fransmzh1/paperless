package paperless.summary.route;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;

import paperless.summary.dto.DeleteRequest;
import paperless.summary.dto.PromptRequest;
import paperless.summary.dto.UploadResponse;

@Component
public class ConsumeDocRoute extends RouteBuilder{

	@Value("${consumefolder}")
	private String consumeFolder;
	
	@Override
	public void configure() throws Exception {

		JacksonDataFormat uploadResponseJdf = new JacksonDataFormat(UploadResponse.class);
		uploadResponseJdf.disableFeature(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		JacksonDataFormat deleteRequestJdf = new JacksonDataFormat(DeleteRequest.class);
		deleteRequestJdf.disableFeature(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		JacksonDataFormat chatRequestJdf = new JacksonDataFormat(PromptRequest.class);

		from("file://" + consumeFolder + "?antInclude=file_*.txt")
			.routeId("fileRoute")
		
			.log("Process Started ${file:name}") 
			.process(exchange -> {
				String fname = exchange.getMessage().getHeader("CamelFileName", String.class);
				fname = fname.replace("file_", "").replace(".txt", "");
				exchange.getMessage().setHeader("docid", fname);
			})
			.log("File ID: ${header.docid}")

			/* download pdf dari DMS */
			
			.setHeader("Authorization", simple("{{dms-auth}}"))
			.setHeader("CamelHttpMethod", constant("GET"))
			.setHeader("CamelHttpPath", simple("/api/documents/${header.docid}/preview/"))			
			.toD("{{dms-url}}")
            .removeHeader("Authorization")
            .removeHeaders("Camel*")
            .removeHeader("docid")
			
			/* Posting konten dokumen ke ChatPDF  */
            
			.setHeader("CamelHttpMethod", constant("POST"))
			.setHeader("x-api-key", simple("{{pdfgpt-auth}}"))

			.process(this::convertMultipart)

			.toD("{{post-url}}")	
			.convertBodyTo(String.class)
			.log("Response: ${body}")
			.unmarshal(uploadResponseJdf)
			.log("Post document complete")

			// save sourceId dari chatPDF
			.process(exchange -> {
				UploadResponse srcId = (UploadResponse) exchange.getMessage().getBody(UploadResponse.class);
				exchange.getMessage().setHeader("sourceid", srcId.getSourceId());
			})
			
			/* Post Chat */
			.process(this::buildRequestPrompt)
			.marshal(chatRequestJdf)
			.log("Chat request: ${body}")
			.toD("{{chat-url}}")
			.convertBodyTo(String.class)
			.log("Chat response: ${body}")
			
			/* Delete uploaded dokument from ChatPDF */
			
			.process(exchange -> {
				String sourceId = exchange.getMessage().getHeader("sourceid", String.class);
				List<String> sources = Arrays.asList(sourceId);
				exchange.getMessage().setBody(new DeleteRequest(sources));
			})
			
			.marshal(deleteRequestJdf)
			.log("Delete request: ${body}")
			.toD("{{del-url}}")
			.log("Delete document complete")
			;
		
	}

	void convertMultipart (Exchange exchange) throws UnsupportedEncodingException {
        byte[] pdfData = exchange.getMessage().getBody(byte[].class);
        
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String multipartBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"document.pdf\"\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n\r\n" +
                new String(pdfData, "ISO-8859-1") + "\r\n" +
                "--" + boundary + "--\r\n";
        
        exchange.getMessage().setBody(multipartBody);
        exchange.getMessage().setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
	}
	
	void buildRequestPrompt(Exchange exchange) {
		String sourceid = exchange.getMessage().getHeader("sourceid", String.class);
		PromptRequest request = new PromptRequest();
		request.setSourceId(sourceid);
		PromptRequest.PromptMessage message = request.new PromptMessage();
		message.setRole("user");
		message.setContent("Buatkan ringkasan dari dokumen ini");
		request.setMessages(Arrays.asList(message));
		exchange.getMessage().setBody(request);
	}
}
