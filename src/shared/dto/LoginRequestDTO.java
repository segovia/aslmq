package shared.dto;

public class LoginRequestDTO extends RequestDTO {
	private Integer accountId;

	public LoginRequestDTO(String[] tokens) {
		this(Integer.parseInt(tokens[1]));
	}

	public LoginRequestDTO(Integer accountId) {
		super(RequestType.LOGIN);
		this.accountId = accountId;
	}

	public Integer getAccountId() {
		return accountId;
	}

	@Override
	public String serialize() {
		return LoginRequestDTO.class.getSimpleName() + "|" + accountId;
	}
}
