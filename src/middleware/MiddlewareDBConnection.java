package middleware;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import shared.DatabaseMonitor;
import shared.Logger;
import shared.NullSafe;
import shared.dto.CreateQueueResponseDTO;
import shared.dto.DeleteQueueRequestDTO;
import shared.dto.ErrorResponseDTO;
import shared.dto.FindQueuesWithMessagesResponseDTO;
import shared.dto.LoginRequestDTO;
import shared.dto.OKResponseDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.ReadMessageResponseDTO;
import shared.dto.ResponseDTO;
import shared.dto.ResponseType;
import shared.dto.SendMessageRequestDTO;

public class MiddlewareDBConnection implements AutoCloseable {

	private Connection conn; // only one connection

	private CallableStatement checkAccount;
	private CallableStatement createQueue;
	private CallableStatement deleteQueue;
	private CallableStatement writeMessage;
	private CallableStatement readMessage;
	private CallableStatement findQueuesWithMessage;

	public MiddlewareDBConnection(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword)
			throws SQLException {
		Properties props = new Properties();
		props.setProperty("user", dbUser);
		props.setProperty("password", dbPassword);
		conn = DriverManager.getConnection("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName, props);

		checkAccount = conn.prepareCall("{call check_account(?)}");
		createQueue = conn.prepareCall("{call create_queue()}");
		deleteQueue = conn.prepareCall("{call delete_queue(?)}");
		writeMessage = conn.prepareCall("{call write_message(?,?,?,?)}");
		readMessage = conn.prepareCall("{call read_message(?,?,?,?)}");
		findQueuesWithMessage = conn.prepareCall("{call find_queues_with_message(?)}");
	}

	@Override
	public void close() throws Exception {
		NullSafe.quietClose(checkAccount);
		NullSafe.quietClose(createQueue);
		NullSafe.quietClose(deleteQueue);
		NullSafe.quietClose(writeMessage);
		NullSafe.quietClose(readMessage);
		NullSafe.quietClose(findQueuesWithMessage);

		NullSafe.quietClose(conn);
	}

	public ResponseDTO checkAccount(LoginRequestDTO dto, DatabaseMonitor monitor) {
		Exception e;
		try {
			checkAccount.setInt(1, dto.getAccountId());
			checkAccount.execute();
			try (ResultSet rs = checkAccount.getResultSet()) {
				if (rs.next()) {
					monitor.setStatementExecTime(rs.getLong(1));
					int status = rs.getInt(2);
					if (status == 0) {
						return new OKResponseDTO();
					}
					return new ErrorResponseDTO(ResponseType.ACCOUNT_NOT_FOUND);
				}
				e = new SQLException("Empty result set");
			}
		} catch (SQLException sqlException) {
			e = sqlException;
		}
		monitor.setStatementExecTimeToUnknown();
		if (MiddlewareServer.active) {
			Logger.error(ResponseType.FAILURE_TO_CHECK_ACCOUNT, e);
		}
		return new ErrorResponseDTO(ResponseType.FAILURE_TO_CHECK_ACCOUNT);
	}

	public ResponseDTO createQueue(DatabaseMonitor monitor) {
		Exception e;
		try {
			createQueue.execute();
			try (ResultSet rs = createQueue.getResultSet()) {
				if (rs.next()) {
					monitor.setStatementExecTime(rs.getLong(1));
					return new CreateQueueResponseDTO(rs.getInt(2));
				}
				e = new SQLException("Empty result set");
			}
		} catch (SQLException sqlException) {
			e = sqlException;
		}
		monitor.setStatementExecTimeToUnknown();
		if (MiddlewareServer.active) {
			Logger.error(ResponseType.FAILURE_TO_CREATE_QUEUE, e);
		}
		return new ErrorResponseDTO(ResponseType.FAILURE_TO_CREATE_QUEUE);
	}

	public ResponseDTO deleteQueue(DeleteQueueRequestDTO dto, DatabaseMonitor monitor) {
		Exception e;
		try {
			deleteQueue.setInt(1, dto.getQueueId());
			deleteQueue.execute();
			try (ResultSet rs = deleteQueue.getResultSet()) {
				if (rs.next()) {
					monitor.setStatementExecTime(rs.getLong(1));
					int status = rs.getInt(2);
					if (status == 0) {
						return new OKResponseDTO();
					}
					if (status == -1) {
						return new ErrorResponseDTO(ResponseType.QUEUE_DOES_NOT_EXIST);
					}
					e = new SQLException("Uknown status");
				}
				e = new SQLException("Empty result set");
			}
		} catch (SQLException sqlException) {
			e = sqlException;
		}
		monitor.setStatementExecTimeToUnknown();
		if (MiddlewareServer.active) {
			Logger.error(ResponseType.FAILURE_TO_DELETE_QUEUE, e);
		}
		return new ErrorResponseDTO(ResponseType.FAILURE_TO_DELETE_QUEUE);
	}

	public ResponseDTO getMessage(Integer clientId, ReadMessageRequestDTO dto, DatabaseMonitor monitor) {
		Exception e;
		try {
			readMessage.setInt(1, clientId);
			if (dto.getSenderId() == null) {
				readMessage.setNull(2, Types.INTEGER);
			} else {
				readMessage.setInt(2, dto.getSenderId());
			}
			if (dto.getQueueId() == null) {
				readMessage.setNull(3, Types.INTEGER);
			} else {
				readMessage.setInt(3, dto.getQueueId());
			}
			readMessage.setBoolean(4, dto.isOnlyPeek());
			readMessage.execute();
			try (ResultSet rs = readMessage.getResultSet()) {
				if (rs.next()) {
					monitor.setStatementExecTime(rs.getLong(1));
					int status = rs.getInt(2);
					if (status == 0) {
						Integer recipientId = rs.getInt(5);
						if (rs.wasNull()) {
							recipientId = null;
						}
						return new ReadMessageResponseDTO(rs.getLong(3), rs.getInt(4), recipientId, rs.getInt(6),
								rs.getString(7), rs.getDate(8).getTime());
					}
					if (status == -1) {
						return new ErrorResponseDTO(ResponseType.QUEUE_DOES_NOT_EXIST);
					}
					if (status == -2) {
						return new ErrorResponseDTO(ResponseType.QUEUE_IS_EMPTY);
					}
					if (status == -3) {
						return new ErrorResponseDTO(ResponseType.BAD_QUERY);
					}
					if (status == -4) {
						return new ErrorResponseDTO(ResponseType.NO_MESSAGE_MATCHING_QUERY);
					}
					e = new SQLException("Uknown status");
				} else {
					e = new SQLException("Empty result set");
				}
			}
		} catch (SQLException sqlException) {
			e = sqlException;
		}
		monitor.setStatementExecTimeToUnknown();
		if (MiddlewareServer.active) {
			Logger.error(ResponseType.FAILURE_TO_READ, e);
		}
		return new ErrorResponseDTO(ResponseType.FAILURE_TO_READ);
	}

	public ResponseDTO writeMessage(Integer clientId, SendMessageRequestDTO dto, DatabaseMonitor monitor) {
		Exception e;
		try {
			writeMessage.setInt(1, clientId);
			writeMessage.setInt(2, dto.getQueueId());
			writeMessage.setString(3, dto.getMessageText());
			if (dto.getRecipientId() != null) {
				writeMessage.setInt(4, dto.getRecipientId());
			} else {
				writeMessage.setNull(4, Types.INTEGER);
			}
			writeMessage.execute();
			try (ResultSet rs = writeMessage.getResultSet()) {
				if (rs.next()) {
					monitor.setStatementExecTime(rs.getLong(1));
					int status = rs.getInt(2);
					if (status == 0) {
						return new OKResponseDTO();
					}
					if (status == -1) {
						return new ErrorResponseDTO(ResponseType.QUEUE_DOES_NOT_EXIST);
					}
					e = new SQLException("Uknown status");
				}
				throw new SQLException("No results.");
			}
		} catch (SQLException sqlException) {
			e = sqlException;
		}
		monitor.setStatementExecTimeToUnknown();
		if (MiddlewareServer.active) {
			Logger.error(ResponseType.FAILURE_TO_WRITE, e);
		}
		return new ErrorResponseDTO(ResponseType.FAILURE_TO_WRITE);
	}

	public ResponseDTO findQueuesWithMessagesRequestDTO(Integer clientId, DatabaseMonitor monitor) {
		Exception e;
		try {
			findQueuesWithMessage.setInt(1, clientId);
			findQueuesWithMessage.execute();
			try (ResultSet rs = findQueuesWithMessage.getResultSet()) {
				List<Long> results = new ArrayList<>();
				while (rs.next()) {
					results.add(rs.getLong(1));
				}
				// last int is the execution time
				Long stmtExecTime = results.remove(results.size() - 1);
				monitor.setStatementExecTime(stmtExecTime);
				return new FindQueuesWithMessagesResponseDTO(results);
			}
		} catch (SQLException sqlException) {
			e = sqlException;
		}
		monitor.setStatementExecTimeToUnknown();
		if (MiddlewareServer.active) {
			Logger.error(ResponseType.FAILURE_TO_FIND_QUEUES, e);
		}
		return new ErrorResponseDTO(ResponseType.FAILURE_TO_FIND_QUEUES);
	}
}
