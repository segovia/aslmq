package middleware;

import shared.dto.RequestType;
import shared.dto.ResponseType;

public class MiddlewareMeasurement {
	private final Integer clientId;
	private final Long elapsedTime;
	private final RequestType requestType;
	private final ResponseType responseType;
	private final Long responseTime;
	private final Long serializationTime;
	private final Long deserializationTime;
	private final Long acquireDBConnectionTime;
	private final Long releaseDBConnectionTime;
	private final Long databaseNetworkTime;
	private final Long statementExecutionTime;
	private final Integer freeDBConnections;
	private final Integer createdDBConnections;

	public MiddlewareMeasurement(Integer clientId, Long elapsedTime, RequestType requestType,
			ResponseType responseType, Long responseTime, Long serializationTime, Long deserializationTime,
			Long acquireDBConnectionTime, Long releaseDBConnectionTime, Long databaseNetworkTime,
			Long statementExecutionTime, Integer freeDBConnections, Integer createdDBConnections) {
		this.clientId = clientId;
		this.elapsedTime = elapsedTime;
		this.requestType = requestType;
		this.responseType = responseType;
		this.responseTime = responseTime;
		this.serializationTime = serializationTime;
		this.deserializationTime = deserializationTime;
		this.acquireDBConnectionTime = acquireDBConnectionTime;
		this.releaseDBConnectionTime = releaseDBConnectionTime;
		this.databaseNetworkTime = databaseNetworkTime;
		this.statementExecutionTime = statementExecutionTime;
		this.freeDBConnections = freeDBConnections;
		this.createdDBConnections = createdDBConnections;
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

	public Long getAcquireDBConnectionTime() {
		return acquireDBConnectionTime;
	}

	public Long getReleaseDBConnectionTime() {
		return releaseDBConnectionTime;
	}

	public Long getDatabaseNetworkTime() {
		return databaseNetworkTime;
	}

	public Long getStatementExecutionTime() {
		return statementExecutionTime;
	}

	public Integer getCreatedDBConnections() {
		return createdDBConnections;
	}

	public Integer getFreeDBConnections() {
		return freeDBConnections;
	}

}
