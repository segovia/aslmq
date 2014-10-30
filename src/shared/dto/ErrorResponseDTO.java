package shared.dto;

public class ErrorResponseDTO extends ResponseDTO {

	public ErrorResponseDTO(String[] tokens) {
		this(ResponseType.getById(Integer.parseInt(tokens[1])));
	}

	public ErrorResponseDTO(ResponseType errorType) {
		super(errorType);
		if (errorType == ResponseType.OK) {
			throw new RuntimeException("Response type should not be OK if it is an error reposne");
		}
	}

	@Override
	public String serialize() {
		return ErrorResponseDTO.class.getSimpleName() + "|" + this.getResponseType().getId();
	}

}
