package client;

import shared.ThreadMonitor;
import shared.dto.RequestType;
import shared.dto.ResponseType;

/**
 * Assumes that only one message is sent at a time.
 *
 * @author Gustavo
 *
 */
public class ClientRunnableMonitor implements ThreadMonitor {

	private final ClientMasterMonitor masterMonitor;
	private final Integer clientId;
	private Long experimentElapsed;
	private Long messageStart;
	private Long messageElapsed;
	private Long serializeStart;
	private Long serializeElapsed;
	private Long networkStart;
	private Long networkElapsed;
	private Long deserializeStart;
	private Long deserializeElapsed;
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
		networkStart = null;
		networkElapsed = null;
		requestType = null;
		responseType = null;
	}

	public ClientRunnableMonitor(ClientMasterMonitor masterMonitor, Integer clientId) {
		this.masterMonitor = masterMonitor;
		this.clientId = clientId;
		clear();
	}

	public void messageStart() {
		clear();
		messageStart = System.nanoTime();
	}

	public void messageEnd() {
		long nanoTime = System.nanoTime();
		messageElapsed = nanoTime - messageStart;
		experimentElapsed = nanoTime - masterMonitor.getExperimentStart();
		storeMeasurement();
	}

	@Override
	public void serializeStart() {
		serializeStart = System.nanoTime();
	}

	@Override
	public void networkWriteStart() {
		networkStart = System.nanoTime();
		serializeElapsed = networkStart - serializeStart;
	}

	@Override
	public void networkReadEnd() {
		deserializeStart = System.nanoTime();
		networkElapsed = deserializeStart - networkStart;
	}

	@Override
	public void deserializeEnd() {
		deserializeElapsed = System.nanoTime() - deserializeStart;
	}

	@Override
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	@Override
	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	private void storeMeasurement() {
		if (masterMonitor.isActive()) {
			masterMonitor.getQueue().offer(
					new ClientMeasurement(clientId, experimentElapsed, requestType, responseType, messageElapsed,
							serializeElapsed, deserializeElapsed, networkElapsed));
		}
	}

}
