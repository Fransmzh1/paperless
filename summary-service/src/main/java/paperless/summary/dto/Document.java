package paperless.summary.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Document {

	private Integer id;
	private String title;
	private String content;
	private List<Integer> tags;

	@JsonProperty("document_type")
	private Integer documentType;
	
	private Integer correspondent;

	@JsonProperty("storage_path")
	private String storagePath;
	
	private String created;
	private String created_date;
	private String modified;
	private String added;
	private Integer owner;
	
	@JsonProperty("archive_serial_number")
	private String archiveSerialNumber;
	
	@JsonProperty("original_file_name")
	private String originalFileName;

	@JsonProperty("archived_file_name")
	private String archivedFileName;

	private List<String> notes;
	
	@JsonProperty("custom_fields")
	private List<CustomField> customFields;
//	private Boolean user_can_change;
//	private Boolean is_shared_by_requester;
	
}
