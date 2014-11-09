package middleware;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import shared.MasterMonitor;

/**
 * Middleware's implementation of {@link MasterMonitor}
 * 
 * @author gustavo
 *
 */
public class MiddlewareMasterMonitor extends MasterMonitor<MiddlewareMeasurement> {

	public LinkedBlockingQueue<MiddlewareMeasurement> queue;
	private List<MiddlewareMeasurement> measurements;

	public MiddlewareMasterMonitor() {
		// inactive
	}

	public MiddlewareMasterMonitor(int measurementSamples, String monitorDbHost, String monitorDbPort,
			String monitorDbName, String monitorDbUser, String monitorDbPassword) throws SQLException {
		super(measurementSamples, monitorDbHost, monitorDbPort, monitorDbName, monitorDbUser, monitorDbPassword);
		queue = new LinkedBlockingQueue<>(measurementSamples);
		measurements = new ArrayList<>();
	}

	public LinkedBlockingQueue<MiddlewareMeasurement> getQueue() {
		return queue;
	}

	@Override
	protected void prepareInsertStatement() throws SQLException {
		// @formatter:off
		insertStatement = monitorConnection.prepareStatement("INSERT INTO middle_time ("
				+ "experiment_id,"
				+ "client_id,"
				+ "elapsed_time,"
				+ "response_time,"
				+ "request_type,"
				+ "response_type,"
				+ "serialization_time,"
				+ "deserialization_time,"
				+ "acquire_db_connection_time,"
				+ "release_db_connection_time,"
				+ "database_network_time,"
				+ "statement_exec_time,"
				+ "free_db_connections,"
				+ "created_db_connections"
				+ " ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		// @formatter:on
	}

	@Override
	public void run() {
		super.run(queue, measurements);
	}

	@Override
	protected void fillInsertStatement(long currentExperiment, MiddlewareMeasurement measurement) throws SQLException {
		int paramIdx = 0;
		insertStatement.setLong(++paramIdx, currentExperiment);// experiment_id
		insertStatement.setInt(++paramIdx, measurement.getClientId());// client_id
		insertStatement.setLong(++paramIdx, measurement.getElapsedTime());// elapsed_time
		insertStatement.setLong(++paramIdx, measurement.getResponseTime());// response_time
		insertStatement.setInt(++paramIdx, measurement.getRequestType().getId());// request_type
		insertStatement.setInt(++paramIdx, measurement.getResponseType().getId());// response_type
		insertStatement.setLong(++paramIdx, measurement.getSerializationTime());// serialization_time
		insertStatement.setLong(++paramIdx, measurement.getDeserializationTime());// deserialization_time
		insertStatement.setLong(++paramIdx, measurement.getAcquireDBConnectionTime());// acquire_db_connection_time
		insertStatement.setLong(++paramIdx, measurement.getReleaseDBConnectionTime());// release_db_connection_time
		insertStatement.setLong(++paramIdx, measurement.getDatabaseNetworkTime());// database_network_time
		insertStatement.setLong(++paramIdx, measurement.getStatementExecutionTime());// statement_exec_time
		insertStatement.setInt(++paramIdx, measurement.getFreeDBConnections());// free_db_connections
		insertStatement.setInt(++paramIdx, measurement.getCreatedDBConnections());// created_db_connections
	}

	@Override
	protected void subClassShutdown() {
		MiddlewareServer.shutdownNow();
	}

}
