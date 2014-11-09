package client;

import shared.dto.RequestType;
import shared.dto.ResponseType;

/**
 * A measurement for cliennt
 * 
 * @author gustavo
 *
 */
public class ClientMeasurement {
	private final Integer clientId;
	private final Long elapsedTime;
	private final RequestType requestType;
	private final ResponseType responseType;
	private final Long responseTime;
	private final Long serializationTime;
	private final Long deserializationTime;
	private final Long networkTime;

	public ClientMeasurement(Integer clientId, Long elapsedTime, RequestType requestType, ResponseType responseType,
			Long responseTime, Long serializationTime, Long deserializationTime, Long networkTime) {
		this.clientId = clientId;
		this.elapsedTime = elapsedTime;
		this.requestType = requestType;
		this.responseType = responseType;
		this.responseTime = responseTime;
		this.serializationTime = serializationTime;
		this.deserializationTime = deserializationTime;
		this.networkTime = networkTime;
	}

	public Integer getClientId() {
		return clientId;
	}

	public Long getElapsedTime() {
		return elapsedTime;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public Long getSerializationTime() {
		return serializationTime;
	}

	public Long getDeserializationTime() {
		return deserializationTime;
	}

	public Long getNetworkTime() {
		return networkTime;
	}
}
