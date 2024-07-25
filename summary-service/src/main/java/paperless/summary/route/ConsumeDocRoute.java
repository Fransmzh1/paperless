package paperless.summary.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;

import paperless.summary.dto.Document;

@Component
public class ConsumeDocRoute extends RouteBuilder{

	@Value("${consumefolder}")
	private String consumeFolder;
	
	@Override
	public void configure() throws Exception {

		JacksonDataFormat jdf = new JacksonDataFormat(Document.class);
		jdf.disableFeature(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//		jdf.setInclude("NON_NULL");
//		jdf.setInclude("NON_EMPTY");

		from("file://" + consumeFolder + "?includeExt=log").routeId("fileRoute")
		
			.log(" Process Started ${file:name}") 
		
			.setHeader("Authorization", simple("{{dms-auth}}"))
			.setHeader("CamelHttpMethod", constant("GET"))
			.setHeader("CamelHttpPath", constant("/api/documents/9/"))
			.log("${headers}")
			
			.toD("{{dms-url}}")
			.convertBodyTo(String.class)
		
			.log("${body}")
			.unmarshal(jdf)
			
			.log("${body.class}")
			.log("${body}")
			
			;
		
	}

	
}
