package dbtester;

import shared.dto.RequestType;
import shared.dto.ResponseType;

/**
 * Measurement for Database tester
 * 
 * @author gustavo
 *
 */
public class DatabaseTesterMeasurement {
	private final Integer clientId;
	private final Long elapsedTime;
	private final RequestType requestType;
	private final ResponseType responseType;
	private final Long databaseNetworkTime;
	private final Long statementExecutionTime;

	public DatabaseTesterMeasurement(Integer clientId, Long elapsedTime, RequestType requestType,
			ResponseType responseType, Long databaseNetworkTime, Long statementExecutionTime) {
		this.clientId = clientId;
		this.elapsedTime = elapsedTime;
		this.requestType = requestType;
		this.responseType = responseType;
		this.databaseNetworkTime = databaseNetworkTime;
		this.statementExecutionTime = statementExecutionTime;
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

	public Long getDatabaseNetworkTime() {
		return databaseNetworkTime;
	}

	public Long getStatementExecutionTime() {
		return statementExecutionTime;
	}

}
