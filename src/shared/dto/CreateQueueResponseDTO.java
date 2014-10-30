package shared.dto;

public class CreateQueueResponseDTO extends ResponseDTO {

	private final Integer queueId;

	public CreateQueueResponseDTO(String[] tokens) {
		this(Integer.parseInt(tokens[1]));
	}

	public CreateQueueResponseDTO(Integer queueId) {
		super(ResponseType.OK);
		this.queueId = queueId;
	}

	public Integer getQueueId() {
		return queueId;
	}

	@Override
	public String serialize() {
		return CreateQueueResponseDTO.class.getSimpleName() + "|" + queueId;
	}
}
