package shared.dto;

public class ReadMessageRequestDTO extends RequestDTO {

	private Integer senderId;
	private Integer queueId;
	private boolean onlyPeek;

	public ReadMessageRequestDTO(String[] tokens) {
		this("null".equals(tokens[1]) ? null : Integer.parseInt(tokens[1]), "null".equals(tokens[2]) ? null : Integer
				.parseInt(tokens[2]), Boolean.parseBoolean(tokens[3]));
	}

	public ReadMessageRequestDTO(Integer senderId, Integer queueId, boolean onlyPeek) {
		super(onlyPeek ? RequestType.READ_PEEK : RequestType.READ_POP);
		this.senderId = senderId;
		this.queueId = queueId;
		this.onlyPeek = onlyPeek;
	}

	public Integer getQueueId() {
		return queueId;
	}

	public Integer getSenderId() {
		return senderId;
	}

	public boolean isOnlyPeek() {
		return onlyPeek;
	}

	@Override
	public String serialize() {
		return ReadMessageRequestDTO.class.getSimpleName() + "|" + senderId + "|" + queueId + "|" + onlyPeek;
	}

}
