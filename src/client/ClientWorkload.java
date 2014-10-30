package client;

import java.io.IOException;

import shared.dto.RequestDTO;
import shared.dto.ResponseDTO;

public abstract class ClientWorkload implements Runnable {

	private ClientMasterMonitor masterMonitor;
	private ClientConnection clientConnection;

	public void init(Integer id, String serverHost, Integer serverPort, ClientMasterMonitor masterMonitor)
			throws IOException {
		this.masterMonitor = masterMonitor;
		clientConnection = new ClientConnection(id, serverHost, serverPort, masterMonitor);
	}

	public ClientConnection getClientConnection() {
		return clientConnection;
	}

	public Integer getClientId() {
		return clientConnection.getId();
	}

	@Override
	public void run() {
		try {
			while (true) {
				runWorkload();
			}
		} catch (IOException e) {
			// might throw exception if shutting down
			if (ClientWorkloadPool.active && !masterMonitor.storing) {
				e.printStackTrace();
			}
		}
	}

	public abstract void runWorkload() throws IOException;

	public void shutdown() {
		try {
			clientConnection.close();
		} catch (Exception e) {
			// ignore
		}
	}

	protected ResponseDTO request(RequestDTO request) throws IOException {
		return getClientConnection().writeRequestAndReadResponse(request);
	}
}
