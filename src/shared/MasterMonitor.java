package shared;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public abstract class MasterMonitor<M> implements Runnable {

	protected final static int BATCH_SIZE = 10000;
	protected boolean active = false;
	public volatile boolean storing = false;
	protected Long experimentStart = 0L;
	protected int measurementSamples = 0;
	protected long logTimeStep = 1000L * 60L;

	protected Connection monitorConnection;
	protected PreparedStatement insertStatement;
	protected long experimentId = -1;

	public MasterMonitor(int measurementSamples, String monitorDbHost, String monitorDbPort, String monitorDbName,
			String monitorDbUser, String monitorDbPassword) throws SQLException {
		this.measurementSamples = measurementSamples;
		Properties props = new Properties();
		props.setProperty("user", monitorDbUser);
		props.setProperty("password", monitorDbPassword);
		monitorConnection = DriverManager.getConnection("jdbc:postgresql://" + monitorDbHost + ":" + monitorDbPort
				+ "/" + monitorDbName, props);

		generateExperimentIdAndExperimentStart();

		monitorConnection.setAutoCommit(false);
		prepareInsertStatement();

		active = true;
	}

	public MasterMonitor() {
		// inactive master monitor
		active = false;
	}

	protected abstract void prepareInsertStatement() throws SQLException;

	private void generateExperimentIdAndExperimentStart() throws SQLException {
		long queryStart = 0L;
		long experimentStartError = 0L;
		long jvmStartUp = 0L;
		long jvmQueryStart = 0L;
		try (PreparedStatement stmt = monitorConnection.prepareStatement("INSERT INTO experiment VALUES (default)",
				Statement.RETURN_GENERATED_KEYS)) {
			jvmStartUp = ManagementFactory.getRuntimeMXBean().getStartTime();
			jvmQueryStart = System.currentTimeMillis();
			queryStart = System.nanoTime();

			stmt.execute();
			experimentStartError = (System.nanoTime() - queryStart) / 2;
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (!generatedKeys.next()) {
					throw new SQLException(getName() + " - creating experiment failed, no ID obtained.");
				}
				experimentId = generatedKeys.getLong(1);
			}
		}
		try (PreparedStatement stmt = monitorConnection
				.prepareStatement("UPDATE experiment SET jvm_startup=?, jvm_query_start=?, start_error=? WHERE id=?")) {
			stmt.setTimestamp(1, new Timestamp(jvmStartUp));
			stmt.setTimestamp(2, new Timestamp(jvmQueryStart + experimentStartError / 1000000));
			stmt.setLong(3, experimentStartError);
			stmt.setLong(4, experimentId);
			stmt.execute();
		}
		experimentStart = queryStart + experimentStartError;
	}

	protected void run(BlockingQueue<M> queue, List<M> measurements) {
		Logger.info(getName() + " - starting experiment: " + experimentId);
		while (measurements.size() < measurementSamples) {
			try {
				measurements.add(queue.take());
				msgAddedHook();
			} catch (InterruptedException e) {
				if (active) {
					System.out.println(getName() + " - queue wait interrupted. Try Again.");
					e.printStackTrace();
				} else {
					// shutting down!
					return;
				}
			}
		}

		storing = true;
		Logger.info(getName() + " - storing measurements");
		sendMeasurementsToMonitorDB(experimentId, measurements);

		// experiment over, shutdown
		shutdown();
	}

	protected void msgAddedHook() {
		// nothing
	}

	public void shutdown() {
		if (active) {
			active = false;
			subClassShutdown();
			storing = false;
			NullSafe.quietClose(insertStatement);
			NullSafe.quietClose(monitorConnection);
		}
	}

	protected abstract void subClassShutdown();

	protected void sendMeasurementsToMonitorDB(long currentExperiment, List<M> measurements) {
		try {
			int curBatchSize = 0;
			for (int i = 0; i < measurements.size(); i++) {
				M measurement = measurements.get(i);
				fillInsertStatement(currentExperiment, measurement);
				insertStatement.addBatch();
				++curBatchSize;

				if (i > 0 && curBatchSize == BATCH_SIZE || i + 1 == measurements.size()) {
					insertStatement.executeBatch();
					monitorConnection.commit();
					insertStatement.clearBatch();
					Logger.info(getName() + " - batch of size " + curBatchSize + " executed. "
							+ String.format("%.2f%%", (i + 1) * 100.0 / measurements.size()));
					curBatchSize = 0;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unable to send measurements to monitor_db", e);
		}
	}

	protected abstract void fillInsertStatement(long currentExperiment, M measurement) throws SQLException;

	protected String getName() {
		return this.getClass().getSimpleName();
	}

	public boolean isActive() {
		return active;
	}

	public Long getExperimentStart() {
		return experimentStart;
	}
}
