package shared.dto;

import java.util.HashMap;
import java.util.Map;

public enum ResponseType {
	NOT_DEFINED(-1), OK(0), BUILD_ERROR(1), UNEXPECTED_ERROR(2), QUEUE_DOES_NOT_EXIST(3), QUEUE_IS_EMPTY(4), FAILURE_TO_CREATE_QUEUE(
			5), FAILURE_TO_DELETE_QUEUE(6), FAILURE_TO_READ(7), FAILURE_TO_WRITE(8), FAILURE_TO_FIND_QUEUES(9), BAD_QUERY(
			10), NO_MESSAGE_MATCHING_QUERY(11), ACCOUNT_NOT_FOUND(12), FAILURE_TO_CHECK_ACCOUNT(13);

	private static final Map<Integer, ResponseType> idTOEnum = new HashMap<>();

	static {
		for (ResponseType r : ResponseType.values()) {
			if (idTOEnum.put(r.getId(), r) != null) {
				throw new IllegalArgumentException("duplicate id: " + r.getId());
			}
		}
	}

	private final Integer id;

	private ResponseType(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public boolean isError() {
		return id != 1;
	}

	public static ResponseType getById(Integer id) {
		return idTOEnum.get(id);
	}
}
