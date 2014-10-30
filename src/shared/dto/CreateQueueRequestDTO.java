package shared.dto;

public class CreateQueueRequestDTO extends RequestDTO {

	public CreateQueueRequestDTO(String[] tokens) {
		this();
	}

	public CreateQueueRequestDTO() {
		super(RequestType.CREATE_QUEUE);
	}

	@Override
	public String serialize() {
		return CreateQueueRequestDTO.class.getSimpleName();
	}
}
