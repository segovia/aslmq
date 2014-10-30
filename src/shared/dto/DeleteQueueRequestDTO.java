package shared.dto;

public class DeleteQueueRequestDTO extends RequestDTO {

	private Integer queueId;

	public DeleteQueueRequestDTO(String[] tokens) {
		this(Integer.parseInt(tokens[1]));
	}

	public DeleteQueueRequestDTO(Integer queueId) {
		super(RequestType.DELETE_QUEUE);
		this.queueId = queueId;
	}

	public Integer getQueueId() {
		return queueId;
	}

	@Override
	public String serialize() {
		return DeleteQueueRequestDTO.class.getSimpleName() + "|" + queueId;
	}
}
