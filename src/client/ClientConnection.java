package client;

import java.io.IOException;
import java.net.Socket;

import shared.MessageReader;
import shared.MessageWriter;
import shared.dto.LoginRequestDTO;
import shared.dto.LogoutRequestDTO;
import shared.dto.OKResponseDTO;
import shared.dto.RequestDTO;
import shared.dto.ResponseDTO;

public class ClientConnection implements AutoCloseable {

	private Integer id;
	private String serverHost;
	private Integer serverPort;
	private Socket socket;
	private MessageWriter writer;
	private MessageReader reader;
	private ClientRunnableMonitor monitor;

	public ClientConnection(Integer id, String serverHost, Integer serverPort, ClientMasterMonitor masterMonitor)
			throws IOException {
		this.id = id;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		socket = new Socket(this.serverHost, this.serverPort);
		System.out.println("Client(" + this.id + ") - connected to " + this.serverHost + ":" + this.serverPort);
		monitor = new ClientRunnableMonitor(masterMonitor, id);
		writer = new MessageWriter(socket.getOutputStream(), monitor);
		reader = new MessageReader(socket.getInputStream(), monitor);

		ResponseDTO responseDTO = writeRequestAndReadResponse(new LoginRequestDTO(id));
		if (!(responseDTO instanceof OKResponseDTO)) {
			throw new RuntimeException("Unable to log in!");
		}
	}

	public Integer getId() {
		return id;
	}

	public ClientRunnableMonitor getMonitor() {
		return monitor;
	}

	public void logout() throws IOException {
		writer.write(new LogoutRequestDTO());
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public ResponseDTO writeRequestAndReadResponse(RequestDTO request) throws IOException {
		monitor.messageStart();
		monitor.setRequestType(request.getRequestType());
		writer.write(request);
		ResponseDTO response = (ResponseDTO) reader.read();
		monitor.setResponseType(response.getResponseType());
		monitor.messageEnd();
		return response;
	}

	@Override
	public void close() throws Exception {
		try {
			if (isConnected()) {
				writer.write(new LogoutRequestDTO());
			}
		} catch (Exception e) {
			// ignore
		}
		if (reader != null) {
			reader.close();
		}
		if (writer != null) {
			writer.close();
		}
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
		System.out.println("Client(" + id + ") - closed");
	}

}
