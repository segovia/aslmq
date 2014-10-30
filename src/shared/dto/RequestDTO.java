package shared.dto;

public abstract class RequestDTO extends DataTransferObject {
	private final RequestType requestType;

	public RequestDTO(RequestType requestType) {
		this.requestType = requestType;
	}

	public RequestType getRequestType() {
		return requestType;
	}

}
