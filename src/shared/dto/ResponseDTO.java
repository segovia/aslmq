package shared.dto;

public abstract class ResponseDTO extends DataTransferObject {
	private final ResponseType responseType;

	public ResponseDTO(ResponseType responseType) {
		this.responseType = responseType;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

}
