package middleware;

import shared.DatabaseMonitor;
import shared.ThreadMonitor;
import shared.dto.RequestType;
import shared.dto.ResponseType;

/**
 * Monitors a request handler. Assumes that only one message is sent at a time.
 *
 * @author Gustavo
 *
 */
public class RequestHandlerMonitor implements ThreadMonitor, DatabaseMonitor {

	private static final long UNKNOWN = -1;

	private final MiddlewareMasterMonitor masterMonitor;
	private Integer clientId;
	private Long experimentElapsed;
	private Long messageStart;
	private Long messageElapsed;
	private Long serializeStart;
	private Long serializeElapsed;
	private Long deserializeStart;
	private Long deserializeElapsed;
	private Long acquireDBConnectionStart;
	private Long acquireDBConnectionElapsed;
	private Long releaseDBConnectionStart;
	private Long releaseDBConnectionElapsed;
	private Long databaseStart;
	private Long databaseElapsed;
	private Long statementExecTime;
	private Integer freeDBConnections;
	private Integer createdDBConnections;
	private RequestType requestType;
	private ResponseType responseType;

	public void clear() {
		experimentElapsed = null;
		messageStart = null;
		messageElapsed = null;
		serializeStart = null;
		serializeElapsed = null;
		deserializeStart = null;
		deserializeElapsed = null;
		acquireDBConnectionStart = null;
		acquireDBConnectionElapsed = null;
		releaseDBConnectionStart = null;
		releaseDBConnectionElapsed = null;
		databaseStart = null;
		databaseElapsed = null;
		statementExecTime = null;
		freeDBConnections = null;
		createdDBConnections = null;
		requestType = null;
		responseType = null;
	}

	public RequestHandlerMonitor(MiddlewareMasterMonitor masterMonitor) {
		this.masterMonitor = masterMonitor;
		clear();
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	@Override
	public void networkReadEnd() {
		clear();
		messageStart = System.nanoTime();
		deserializeStart = messageStart;
	}

	@Override
	public void deserializeEnd() {
		databaseStart = System.nanoTime();
		deserializeElapsed = databaseStart - deserializeStart;
	}

	@Override
	public void setStatementExecTime(Long statementExecTime) {
		this.statementExecTime = statementExecTime;
	}

	@Override
	public void setStatementExecTimeToUnknown() {
		statementExecTime = UNKNOWN;
	}

	@Override
	public void serializeStart() {
		serializeStart = System.nanoTime();
	}

	@Override
	public void networkWriteStart() {
		long nanoTime = System.nanoTime();
		messageElapsed = nanoTime - messageStart;
		serializeElapsed = nanoTime - serializeStart;
		experimentElapsed = nanoTime - masterMonitor.getExperimentStart();
		storeMeasurement();
	}

	public void acquireDBConnectionStart() {
		acquireDBConnectionStart = System.nanoTime();
	}

	public void databaseStart() {
		databaseStart = System.nanoTime();
		acquireDBConnectionElapsed = databaseStart - acquireDBConnectionStart;
	}

	public void databaseEnd() {
		releaseDBConnectionStart = System.nanoTime();
		databaseElapsed = releaseDBConnectionStart - databaseStart;
	}

	public void releaseDBConnectionEnd() {
		releaseDBConnectionElapsed = System.nanoTime() - releaseDBConnectionStart;
	}

	@Override
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	@Override
	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	public void setCreatedDBConnections(Integer createdDBConnections) {
		this.createdDBConnections = createdDBConnections;
	}

	public void setFreeDBConnections(Integer freeDBConnections) {
		this.freeDBConnections = freeDBConnections;
	}

	private void storeMeasurement() {
		if (masterMonitor.isActive()) {
			long databaseNetworkTime = databaseElapsed;
			if (statementExecTime != UNKNOWN) {
				if (databaseElapsed >= statementExecTime) {
					databaseNetworkTime = databaseElapsed - statementExecTime;
				} else {
					// this might be caused by the db precision error
					statementExecTime = databaseElapsed;
					databaseNetworkTime = 0L;
				}
			}
			masterMonitor.getQueue().offer(
					new MiddlewareMeasurement(clientId, experimentElapsed, requestType, responseType, messageElapsed,
							serializeElapsed, deserializeElapsed, acquireDBConnectionElapsed,
							releaseDBConnectionElapsed, databaseNetworkTime, statementExecTime, freeDBConnections,
							createdDBConnections));
		}
	}

}
