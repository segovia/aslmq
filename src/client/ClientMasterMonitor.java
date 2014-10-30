package client;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import shared.MasterMonitor;

public class ClientMasterMonitor extends MasterMonitor<ClientMeasurement> {

	private LinkedBlockingQueue<ClientMeasurement> queue;
	private List<ClientMeasurement> measurements;
	private int lastMeasurementCount = 0;
	private Long lastLog;

	public ClientMasterMonitor() {
		// inactive monitor
	}

	public ClientMasterMonitor(Integer measurementSamples, String monitorDbHost, String monitorDbPort,
			String monitorDbName, String monitorDbUser, String monitorDbPassword) throws SQLException {
		super(measurementSamples, monitorDbHost, monitorDbPort, monitorDbName, monitorDbUser, monitorDbPassword);
		queue = new LinkedBlockingQueue<>(measurementSamples);
		measurements = new ArrayList<>();
		lastLog = System.currentTimeMillis();
	}

	public LinkedBlockingQueue<ClientMeasurement> getQueue() {
		return queue;
	}

	@Override
	protected void prepareInsertStatement() throws SQLException {
		// @formatter:off
		insertStatement = monitorConnection.prepareStatement("INSERT INTO client_time ("
				+ "experiment_id,"
				+ "client_id,"
				+ "elapsed_time,"
				+ "response_time,"
				+ "request_type,"
				+ "response_type,"
				+ "serialization_time,"
				+ "deserialization_time,"
				+ "network_time"
				+ " ) VALUES (?,?,?,?,?,?,?,?,?)");
		// @formatter:on
	}

	@Override
	public void run() {
		super.run(queue, measurements);
	}

	@Override
	protected void fillInsertStatement(long currentExperiment, ClientMeasurement measurement) throws SQLException {
		int paramIdx = 0;
		insertStatement.setLong(++paramIdx, currentExperiment);// experiment_id
		insertStatement.setInt(++paramIdx, measurement.getClientId());// client_id
		insertStatement.setLong(++paramIdx, measurement.getElapsedTime());// elapsed_time
		insertStatement.setLong(++paramIdx, measurement.getResponseTime());// response_time
		insertStatement.setInt(++paramIdx, measurement.getRequestType().getId());// request_type
		insertStatement.setInt(++paramIdx, measurement.getResponseType().getId());// response_type
		insertStatement.setLong(++paramIdx, measurement.getSerializationTime());// serialization_time
		insertStatement.setLong(++paramIdx, measurement.getDeserializationTime());// deserialization_time
		insertStatement.setLong(++paramIdx, measurement.getNetworkTime());// network_time
	}

	@Override
	protected void subClassShutdown() {
		ClientWorkloadPool.shutdownNow();
	}

	@Override
	protected void msgAddedHook() {
		long curTime = System.currentTimeMillis();
		if (System.currentTimeMillis() - lastLog > logTimeStep) {
			long timeInterval = curTime - lastLog;
			double percent = (double) measurements.size() / measurementSamples;
			long throughput = measurements.size() - lastMeasurementCount;
			double messagesPerSecond = throughput * 1000 / timeInterval;
			long timeRemaining = (long) (((measurementSamples - measurements.size()) * 1000.0) / messagesPerSecond);

			long hours = timeRemaining / 3600000;
			long mins = timeRemaining / 60000 % 60;
			long seconds = timeRemaining / 1000 % 60;
			System.out.format("%.2f%% done; msg/s: %.1f; time remaining: %02d:%02d:%02d\n", percent * 100,
					messagesPerSecond, hours, mins, seconds);
			lastMeasurementCount = measurements.size();
			lastLog = curTime;
		}
	}

}
