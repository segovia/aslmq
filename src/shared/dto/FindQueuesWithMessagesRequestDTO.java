package shared.dto;

public class FindQueuesWithMessagesRequestDTO extends RequestDTO {

	public FindQueuesWithMessagesRequestDTO(String[] tokens) {
		this();
	}

	public FindQueuesWithMessagesRequestDTO() {
		super(RequestType.FIND_QUEUES);
	}

	@Override
	public String serialize() {
		return FindQueuesWithMessagesRequestDTO.class.getSimpleName();
	}

}
