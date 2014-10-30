package shared.dto;

public class SendMessageRequestDTO extends RequestDTO {

	private Integer recipientId;
	private Integer queueId;
	private String messageText;

	public SendMessageRequestDTO(String[] tokens) {
		this("null".equals(tokens[1]) ? null : Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]),
				unescape(tokens[3]));
	}

	public SendMessageRequestDTO(Integer recipientId, Integer queueId, String messageText) {
		super(RequestType.SEND);
		this.recipientId = recipientId;
		this.queueId = queueId;
		this.messageText = messageText;
	}

	public String getMessageText() {
		return messageText;
	}

	public Integer getQueueId() {
		return queueId;
	}

	public Integer getRecipientId() {
		return recipientId;
	}

	@Override
	public String serialize() {
		return SendMessageRequestDTO.class.getSimpleName() + "|" + recipientId + "|" + queueId + "|"
				+ escape(messageText);
	}

}
