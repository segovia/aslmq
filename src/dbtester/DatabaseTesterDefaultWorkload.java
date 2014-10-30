package dbtester;

import middleware.MiddlewareDBConnection;
import shared.dto.CreateQueueResponseDTO;
import shared.dto.DeleteQueueRequestDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.RequestType;
import shared.dto.ResponseDTO;
import shared.dto.SendMessageRequestDTO;

public class DatabaseTesterDefaultWorkload {

	private static final int TARGET_QUEUE = 1;

	private int clientId;
	private MiddlewareDBConnection conn;
	private DatabaseTesterMonitor monitor;

	private String msg;

	public DatabaseTesterDefaultWorkload(int clientId, MiddlewareDBConnection conn, DatabaseTesterMonitor monitor,
			String msg) {
		this.clientId = clientId;
		this.conn = conn;
		this.monitor = monitor;
		this.monitor.setClientId(clientId);
		this.msg = msg;
	}

	public void runWorkload() {
		writeMessage(new SendMessageRequestDTO(null, TARGET_QUEUE, msg));
		getMessage(new ReadMessageRequestDTO(null, TARGET_QUEUE, true));
		getMessage(new ReadMessageRequestDTO(null, TARGET_QUEUE, false));

		writeMessage(new SendMessageRequestDTO(clientId, TARGET_QUEUE, msg));
		getMessage(new ReadMessageRequestDTO(clientId, TARGET_QUEUE, true));
		getMessage(new ReadMessageRequestDTO(clientId, TARGET_QUEUE, false));

		Integer createdQueueId = createQueue();
		writeMessage(new SendMessageRequestDTO(null, createdQueueId, msg));
		findQueuesWithMessagesRequestDTO();
		getMessage(new ReadMessageRequestDTO(clientId, null, true));
		getMessage(new ReadMessageRequestDTO(null, createdQueueId, false));
		deleteQueue(new DeleteQueueRequestDTO(createdQueueId));
	}

	private void writeMessage(SendMessageRequestDTO dto) {
		monitor.databaseStart();
		monitor.setRequestType(dto.getRequestType());
		ResponseDTO responseDTO = conn.writeMessage(clientId, dto, monitor);
		monitor.setResponseType(responseDTO.getResponseType());
		monitor.databaseEnd();
	}

	private void getMessage(ReadMessageRequestDTO dto) {
		monitor.databaseStart();
		monitor.setRequestType(dto.getRequestType());
		ResponseDTO responseDTO = conn.getMessage(clientId, dto, monitor);
		monitor.setResponseType(responseDTO.getResponseType());
		monitor.databaseEnd();
	}

	private int createQueue() {
		monitor.databaseStart();
		monitor.setRequestType(RequestType.CREATE_QUEUE);
		ResponseDTO responseDTO = conn.createQueue(monitor);
		monitor.setResponseType(responseDTO.getResponseType());
		monitor.databaseEnd();
		return ((CreateQueueResponseDTO) responseDTO).getQueueId();
	}

	private void deleteQueue(DeleteQueueRequestDTO dto) {
		monitor.databaseStart();
		monitor.setRequestType(dto.getRequestType());
		ResponseDTO responseDTO = conn.deleteQueue(dto, monitor);
		monitor.setResponseType(responseDTO.getResponseType());
		monitor.databaseEnd();
	}

	private void findQueuesWithMessagesRequestDTO() {
		monitor.databaseStart();
		monitor.setRequestType(RequestType.FIND_QUEUES);
		ResponseDTO responseDTO = conn.findQueuesWithMessagesRequestDTO(clientId, monitor);
		monitor.setResponseType(responseDTO.getResponseType());
		monitor.databaseEnd();
	}
}
