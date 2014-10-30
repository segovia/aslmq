package shared.dto;

public class ReadMessageResponseDTO extends ResponseDTO {

	private Long messageId;
	private Integer senderId;
	private Integer recipientId;
	private Integer queueId;
	private String messageText;
	private Long arrival;

	public ReadMessageResponseDTO(String[] tokens) {
		this(Long.parseLong(tokens[1]), Integer.parseInt(tokens[2]), "null".equals(tokens[3]) ? null : Integer
				.parseInt(tokens[3]), Integer.parseInt(tokens[4]), unescape(tokens[5]), Long.parseLong(tokens[6]));
	}

	public ReadMessageResponseDTO(Long messageId, Integer senderId, Integer recipientId, Integer queueId,
			String messageText, Long arrival) {
		super(ResponseType.OK);
		this.messageId = messageId;
		this.senderId = senderId;
		this.recipientId = recipientId;
		this.queueId = queueId;
		this.messageText = messageText;
		this.arrival = arrival;
	}

	public Long getMessageId() {
		return messageId;
	}

	public Integer getSenderId() {
		return senderId;
	}

	public Integer getRecipientId() {
		return recipientId;
	}

	public Integer getQueueId() {
		return queueId;
	}

	public String getMessageText() {
		return messageText;
	}

	public Long getArrival() {
		return arrival;
	}

	@Override
	public String serialize() {
		return ReadMessageResponseDTO.class.getSimpleName() + "|" + messageId + "|" + senderId + "|" + recipientId
				+ "|" + queueId + "|" + escape(messageText) + "|" + arrival;
	}
}
