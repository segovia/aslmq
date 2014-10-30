package shared.dto;

public class LogoutRequestDTO extends RequestDTO {
	public LogoutRequestDTO(String[] tokens) {
		this();
	}

	public LogoutRequestDTO() {
		super(RequestType.LOGOUT);
	}

	@Override
	public String serialize() {
		return LogoutRequestDTO.class.getSimpleName();
	}
}
