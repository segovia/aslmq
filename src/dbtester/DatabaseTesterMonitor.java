package dbtester;

import shared.DatabaseMonitor;
import shared.dto.RequestType;
import shared.dto.ResponseType;

/**
 * Assumes that only one message is sent at a time.
 *
 * @author Gustavo
 *
 */
public class DatabaseTesterMonitor implements DatabaseMonitor {

	private static final long UNKNOWN = -1;

	private final DatabaseTesterMasterMonitor masterMonitor;
	private Integer clientId;
	private Long experimentElapsed;
	private Long databaseStart;
	private Long databaseElapsed;
	private Long statementExecTime;
	private RequestType requestType;
	private ResponseType responseType;

	public void clear() {
		experimentElapsed = null;
		databaseStart = null;
		databaseElapsed = null;
		statementExecTime = null;
		requestType = null;
		responseType = null;
	}

	public DatabaseTesterMonitor(DatabaseTesterMasterMonitor masterMonitor) {
		this.masterMonitor = masterMonitor;
		clear();
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	@Override
	public void setStatementExecTime(Long statementExecTime) {
		this.statementExecTime = statementExecTime;
	}

	@Override
	public void setStatementExecTimeToUnknown() {
		statementExecTime = UNKNOWN;
	}

	public void databaseStart() {
		clear();
		databaseStart = System.nanoTime();
	}

	public void databaseEnd() {
		long nanoTime = System.nanoTime();
		databaseElapsed = nanoTime - databaseStart;
		experimentElapsed = nanoTime - masterMonitor.getExperimentStart();
		storeMeasurement();
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	private void storeMeasurement() {
		if (masterMonitor.isActive()) {
			long databaseNetworkTime = databaseElapsed;
			// if (statementExecTime != UNKNOWN) {
			// if (databaseElapsed >= statementExecTime) {
			// databaseNetworkTime = databaseElapsed - statementExecTime;
			// } else {
			// // this might be caused by the db precision error
			// statementExecTime = databaseElapsed;
			// databaseNetworkTime = 0L;
			// }
			// }
			masterMonitor.getQueue().offer(
					new DatabaseTesterMeasurement(clientId, experimentElapsed, requestType, responseType,
							databaseNetworkTime, statementExecTime));
		}
	}

}
