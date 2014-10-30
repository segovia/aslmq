package shared.dto;

public enum RequestType {
	NOT_DEFINED(-1), SEND(0), READ_PEEK(1), READ_POP(2), CREATE_QUEUE(3), DELETE_QUEUE(4), LOGIN(5), LOGOUT(6), FIND_QUEUES(
			7);

	private final Integer id;

	private RequestType(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
