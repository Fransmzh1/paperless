package paperless.summary.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PromptRequest {

	private String sourceId;
	private List<PromptMessage> messages;
	
	@Data
	@NoArgsConstructor
	public class PromptMessage {
		private String role;
		private String content;
	}
}
