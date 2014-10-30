package middleware;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import shared.Logger;
import shared.MessageReader;
import shared.MessageWriter;
import shared.SampleMessage;
import shared.dto.CreateQueueRequestDTO;
import shared.dto.CreateQueueResponseDTO;
import shared.dto.DataTransferObject;
import shared.dto.DeleteQueueRequestDTO;
import shared.dto.FindQueuesWithMessagesRequestDTO;
import shared.dto.FindQueuesWithMessagesResponseDTO;
import shared.dto.LoginRequestDTO;
import shared.dto.LogoutRequestDTO;
import shared.dto.OKResponseDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.ReadMessageResponseDTO;
import shared.dto.RequestDTO;
import shared.dto.ResponseDTO;
import shared.dto.ResponseType;
import shared.dto.SendMessageRequestDTO;

public class RequestHandler implements Runnable {

	private volatile boolean active = true;
	private Socket socket;
	private Integer clientAccountId = null;
	private RequestHandlerMonitor monitor;
	private boolean mockMode;
	private Map<Class<? extends RequestDTO>, ResponseDTO> mocks;

	public RequestHandler(Socket socket, MiddlewareMasterMonitor masterMonitor, boolean mockMode) {
		this.socket = socket;
		this.mockMode = mockMode;
		Logger.info("Server - connected with client at: " + socket.getRemoteSocketAddress());
		monitor = new RequestHandlerMonitor(masterMonitor);
	}

	@Override
	public void run() {
		try (MessageWriter writer = new MessageWriter(socket.getOutputStream(), monitor);
				MessageReader reader = new MessageReader(socket.getInputStream(), monitor)) {
			RequestDTO requestDTO;
			while ((requestDTO = (RequestDTO) reader.read()) != null) {

				if (requestDTO instanceof LogoutRequestDTO) {
					break;
				}

				if (mockMode) {
					writer.write(getMockResponse(requestDTO));
					continue;
				}

				monitor.setRequestType(requestDTO.getRequestType());
				ResponseDTO responseDTO;
				if (clientAccountId == null) {
					// first message must be login
					if (!(requestDTO instanceof LoginRequestDTO)) {
						throw new RuntimeException("Client did not login!");
					}
					responseDTO = queryDatabase(requestDTO);
					if (responseDTO instanceof OKResponseDTO) {
						clientAccountId = ((LoginRequestDTO) requestDTO).getAccountId();
						monitor.setClientId(clientAccountId);
						Logger.info("Server - client(" + clientAccountId + ") logged in");
					} else {
						monitor.setClientId(-1);
					}
				} else {
					responseDTO = queryDatabase(requestDTO);
				}

				monitor.setResponseType(responseDTO.getResponseType());
				writer.write(responseDTO);

			}

		} catch (IOException e) {
			if (e instanceof SocketException && e.getMessage().toLowerCase().startsWith("socket closed")) {
				// it ok, shutting down
			} else {
				Logger.error(ResponseType.UNEXPECTED_ERROR, e);
			}
		} catch (ClassNotFoundException e) {
			Logger.error(ResponseType.BUILD_ERROR, e);
		} catch (InterruptedException e) {
			if (!MiddlewareServer.active) {
				// it ok, shutting down
			} else {
				Logger.error(ResponseType.BUILD_ERROR, e);
			}
		} catch (Exception e) {
			Logger.error(ResponseType.UNEXPECTED_ERROR, e);
		} finally {
			shutdown();
		}
	}

	private DataTransferObject getMockResponse(RequestDTO requestDTO) {
		if (mocks == null) {
			mocks = new HashMap<>();
			mocks.put(CreateQueueRequestDTO.class, new CreateQueueResponseDTO(1));
			mocks.put(DeleteQueueRequestDTO.class, new OKResponseDTO());
			mocks.put(FindQueuesWithMessagesRequestDTO.class,
					new FindQueuesWithMessagesResponseDTO(Arrays.asList(1L, 2L, 3L)));
			mocks.put(LoginRequestDTO.class, new OKResponseDTO());
			mocks.put(ReadMessageRequestDTO.class, new ReadMessageResponseDTO(1L, 2, 3, 4, SampleMessage.SMALL_MSG, 0L));
			mocks.put(SendMessageRequestDTO.class, new OKResponseDTO());
		}
		ResponseDTO responseDTO = mocks.get(requestDTO.getClass());
		return responseDTO;
	}

	private ResponseDTO queryDatabase(RequestDTO dto) throws InterruptedException {
		monitor.acquireDBConnectionStart();
		MiddlewareDBConnection dbConnection = MiddlewareDBConnectionPool.acquireConnection();
		monitor.setCreatedDBConnections(MiddlewareDBConnectionPool.getCreatedDBConnectionsCount());
		monitor.setFreeDBConnections(MiddlewareDBConnectionPool.getFreeDBConnectionsCount());
		try {
			monitor.databaseStart();
			return queryDatabase(dto, dbConnection);
		} finally {
			monitor.databaseEnd();
			MiddlewareDBConnectionPool.releaseConnection(dbConnection);
			monitor.releaseDBConnectionEnd();
		}
	}

	private ResponseDTO queryDatabase(RequestDTO dto, MiddlewareDBConnection dbConnection) {
		if (dto instanceof SendMessageRequestDTO) {
			// TODO Create incoming message queue and batch inserts. Maybe not possible if needs to give
			// feedback on insert. also, how to check that queue does not exist error if in batch
			return dbConnection.writeMessage(clientAccountId, (SendMessageRequestDTO) dto, monitor);
		} else if (dto instanceof ReadMessageRequestDTO) {
			return dbConnection.getMessage(clientAccountId, (ReadMessageRequestDTO) dto, monitor);
		} else if (dto instanceof CreateQueueRequestDTO) {
			return dbConnection.createQueue(monitor);
		} else if (dto instanceof FindQueuesWithMessagesRequestDTO) {
			return dbConnection.findQueuesWithMessagesRequestDTO(clientAccountId, monitor);
		} else if (dto instanceof DeleteQueueRequestDTO) {
			return dbConnection.deleteQueue((DeleteQueueRequestDTO) dto, monitor);
		} else if (dto instanceof LoginRequestDTO) {
			return dbConnection.checkAccount((LoginRequestDTO) dto, monitor);
		}
		throw new RuntimeException("Unknown message type: " + dto.getClass().getSimpleName());
	}

	public void shutdown() {
		if (!active) {
			// already shutdown
			return;
		}
		try {
			active = false;
			socket.close();
			Logger.info("Server - disconnected from client(" + clientAccountId + ")");
		} catch (IOException e) {
			Logger.error(ResponseType.UNEXPECTED_ERROR, e);
		}
	}

	public boolean isActive() {
		return active;
	}
}
