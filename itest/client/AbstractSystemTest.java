package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import middleware.MiddlewareServer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import shared.PropertiesLoader;
import shared.SampleMessage;
import shared.dto.ErrorResponseDTO;
import shared.dto.FindQueuesWithMessagesResponseDTO;
import shared.dto.OKResponseDTO;
import shared.dto.ReadMessageResponseDTO;
import shared.dto.ResponseDTO;
import shared.dto.ResponseType;

public class AbstractSystemTest {

	protected final static String DROP_TABLE_FILE = "sql/drop_tables.sql";
	protected final static String CREATE_TABLE_FILE = "sql/create_tables.sql";
	protected final static String DROP_CREATE_MONITOR_TABLE_FILE = "sql/monitor-tables.sql";
	protected final static String PROCEDURES_FILE = "sql/procedures.sql";

	protected static String smallMsgText;
	protected static String largeMsgText;
	private static Properties prop;

	protected Connection dbConnection;
	protected Connection monitorDBConnection;
	protected int timeout = 5000;

	protected final List<Throwable> threadExceptions = new ArrayList<Throwable>();

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException {
		smallMsgText = SampleMessage.SMALL_MSG;
		largeMsgText = SampleMessage.LARGE_MSG;
		prop = PropertiesLoader.load("config/localhost.properties");
	}

	@Before
	public void before() throws Exception {
		createDBConnections(prop);
		execSQLFile(dbConnection, DROP_TABLE_FILE, false);
		execSQLFile(dbConnection, CREATE_TABLE_FILE, false);
		execSQLFile(dbConnection, PROCEDURES_FILE, true);
		execSQLFile(monitorDBConnection, DROP_CREATE_MONITOR_TABLE_FILE, false);

		try (CallableStatement stmt = dbConnection.prepareCall("{call initial_data()}")) {
			stmt.execute();
		}
	}

	@After
	public void after() throws Exception {
		MiddlewareServer.shutdownNow();
		ClientWorkloadPool.shutdownNow();

		if (dbConnection != null) {
			dbConnection.close();
		}

		if (monitorDBConnection != null) {
			monitorDBConnection.close();
		}
	}

	protected static void assertOk(ResponseDTO responseDTO) {
		Assert.assertNotNull(responseDTO);
		Assert.assertEquals(ResponseType.OK, responseDTO.getResponseType());
		Assert.assertTrue(responseDTO instanceof OKResponseDTO);
	}

	protected static void assertError(ResponseDTO responseDTO, ResponseType errorType) {
		Assert.assertNotNull(responseDTO);
		Assert.assertTrue(responseDTO instanceof ErrorResponseDTO);
		Assert.assertEquals(errorType, ((ErrorResponseDTO) responseDTO).getResponseType());
	}

	protected static void assertRead(Long messageId, Integer senderId, Integer recipientId, Integer queueId,
			String messageText, ResponseDTO responseDTO) {
		Assert.assertNotNull(responseDTO);
		Assert.assertEquals(ResponseType.OK, responseDTO.getResponseType());
		Assert.assertTrue(responseDTO instanceof ReadMessageResponseDTO);
		Assert.assertEquals(messageId, ((ReadMessageResponseDTO) responseDTO).getMessageId());
		Assert.assertEquals(senderId, ((ReadMessageResponseDTO) responseDTO).getSenderId());
		Assert.assertEquals(recipientId, ((ReadMessageResponseDTO) responseDTO).getRecipientId());
		Assert.assertEquals(queueId, ((ReadMessageResponseDTO) responseDTO).getQueueId());
		Assert.assertEquals(messageText, ((ReadMessageResponseDTO) responseDTO).getMessageText());
		Assert.assertTrue(((ReadMessageResponseDTO) responseDTO).getArrival() > 0);
	}

	protected static void assertFindQueues(List<Long> queueIds, ResponseDTO responseDTO) {
		Assert.assertNotNull(responseDTO);
		Assert.assertEquals(ResponseType.OK, responseDTO.getResponseType());
		Assert.assertTrue(responseDTO instanceof FindQueuesWithMessagesResponseDTO);
		Assert.assertEquals(queueIds, ((FindQueuesWithMessagesResponseDTO) responseDTO).getQueueIds());
	}

	private void assertClientMeasurements(final Integer expectedClientId, final int offset, List<TestWorkload> workloads)
			throws SQLException {
		Statement stmt = monitorDBConnection.createStatement();
		stmt.execute("SELECT * from client_time OFFSET " + (offset + 1)); // the + 1 skips the login message
		ResultSet rs = stmt.getResultSet();

		int expectedCount = 0;
		for (TestWorkload workload : workloads) {
			Assert.assertEquals(workload.requestTypes.size(), workload.responseTypes.size());
			expectedCount += workload.requestTypes.size();
		}
		int rowCount = 0;
		Set<Integer> seenClients = new HashSet<>();
		while (rs.next()) {
			if (rowCount < expectedCount) {
				int paramIdx = 0;
				Long id = rs.getLong(++paramIdx);// id
				Long experimentId = rs.getLong(++paramIdx);// experiment_id
				Integer clientId = rs.getInt(++paramIdx);// client_id
				Long elapsedTime = rs.getLong(++paramIdx);// elapsed_time
				Long responseTime = rs.getLong(++paramIdx);// response_time
				Integer requestTypeId = rs.getInt(++paramIdx);// request_type
				Integer responseTypeId = rs.getInt(++paramIdx);// response_type
				Long serializationTime = rs.getLong(++paramIdx);// serialization_time
				Long deserializationTime = rs.getLong(++paramIdx);// deserialization_time
				Long networkTime = rs.getLong(++paramIdx);// network_time

				Assert.assertTrue(id > 0);
				Assert.assertTrue(experimentId > 0);
				seenClients.add(clientId);
				Assert.assertTrue("client id: " + clientId + ", expectedClientId: " + expectedClientId + ", threads: "
						+ workloads.size(),
						clientId >= expectedClientId && clientId <= (expectedClientId + workloads.size() - 1));
				Assert.assertTrue(elapsedTime > 0);
				Assert.assertTrue(responseTime > 0);
				if (workloads.size() == 1) {
					TestWorkload workload = workloads.get(0);
					Assert.assertEquals(workload.requestTypes.get(rowCount).getId(), requestTypeId);
					Assert.assertEquals(workload.responseTypes.get(rowCount).getId(), responseTypeId);
				}
				Assert.assertTrue(serializationTime > 0);
				Assert.assertTrue(deserializationTime > 0);
				Assert.assertTrue(networkTime > 0);
				Assert.assertTrue(responseTime >= serializationTime + networkTime + deserializationTime);
			}
			++rowCount;
		}
		Assert.assertEquals(expectedCount, rowCount);
		Assert.assertEquals(workloads.size(), seenClients.size());
	}

	private void assertMiddlewareMeasurements(final Integer expectedClientId, final int offset,
			List<TestWorkload> workloads) throws SQLException {
		Statement stmt = monitorDBConnection.createStatement();
		stmt.execute("SELECT * from middle_time OFFSET " + (offset + 1));// the + 1 skips the login message
		ResultSet rs = stmt.getResultSet();

		int expectedCount = 0;
		for (TestWorkload workload : workloads) {
			Assert.assertEquals(workload.requestTypes.size(), workload.responseTypes.size());
			expectedCount += workload.requestTypes.size();
		}
		int rowCount = 0;
		Set<Integer> seenClients = new HashSet<>();
		while (rs.next()) {
			if (rowCount < expectedCount) {
				int paramIdx = 0;
				Long id = rs.getLong(++paramIdx);// id
				Long experimentId = rs.getLong(++paramIdx);// experiment_id
				Integer clientId = rs.getInt(++paramIdx);// client_id
				Long elapsedTime = rs.getLong(++paramIdx);// elapsed_time
				Long responseTime = rs.getLong(++paramIdx);// response_time
				Integer requestTypeId = rs.getInt(++paramIdx);// request_type
				Integer responseTypeId = rs.getInt(++paramIdx);// response_type
				Long serializationTime = rs.getLong(++paramIdx);// serialization_time
				Long deserializationTime = rs.getLong(++paramIdx);// deserialization_time
				Long acquireDBConnectionTime = rs.getLong(++paramIdx);// acquire_db_connection_time
				Long releaseDBConnectionTime = rs.getLong(++paramIdx);// release_db_connection_time
				Long databaseNetworkTime = rs.getLong(++paramIdx);// database_network_time
				Long statmentExecutionTime = rs.getLong(++paramIdx);// statement_exec_time
				Integer freeDBConnections = rs.getInt(++paramIdx);// free_db_connections
				Integer createdDBConnections = rs.getInt(++paramIdx);// created_db_connections

				Assert.assertTrue(id > 0);
				Assert.assertTrue(experimentId > 0);
				seenClients.add(clientId);
				Assert.assertTrue(clientId >= expectedClientId && clientId <= (expectedClientId + workloads.size() - 1));
				Assert.assertTrue(elapsedTime > 0);
				Assert.assertTrue(responseTime > 0);
				if (workloads.size() == 1) {
					TestWorkload workload = workloads.get(0);
					Assert.assertEquals(workload.requestTypes.get(rowCount).getId(), requestTypeId);
					Assert.assertEquals(workload.responseTypes.get(rowCount).getId(), responseTypeId);
				}
				Assert.assertTrue(serializationTime > 0);
				Assert.assertTrue(deserializationTime > 0);
				Assert.assertTrue(acquireDBConnectionTime > 0);
				Assert.assertTrue(releaseDBConnectionTime > 0);
				Assert.assertTrue("databaseNetworkTime: " + databaseNetworkTime, databaseNetworkTime >= 0);
				Assert.assertTrue("statmentExecutionTime = " + statmentExecutionTime, statmentExecutionTime >= 0
						|| statmentExecutionTime == -1);
				Assert.assertTrue(freeDBConnections >= 0);
				Assert.assertTrue(createdDBConnections >= 0);
				Assert.assertTrue(createdDBConnections >= freeDBConnections);
				long sum = serializationTime + acquireDBConnectionTime + databaseNetworkTime + statmentExecutionTime
						+ releaseDBConnectionTime + deserializationTime;
				Assert.assertTrue("databaseNetworkTime: " + responseTime + "; sum: " + sum
						+ "; statmentExecutionTime: " + statmentExecutionTime, responseTime >= sum);
			}
			++rowCount;
		}
		Assert.assertEquals(expectedCount, rowCount);
		Assert.assertEquals(workloads.size(), seenClients.size());
	}

	protected static String fileToString(String sqlFile) throws FileNotFoundException, IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
			sb.append(reader.readLine()).append("\n");
		}
		return sb.toString();
	}

	private void createDBConnections(Properties prop) throws SQLException {
		Properties props1 = new Properties();
		props1.setProperty("user", prop.getProperty("dbUser"));
		props1.setProperty("password", prop.getProperty("dbPassword"));
		dbConnection = DriverManager.getConnection(
				"jdbc:postgresql://" + prop.getProperty("dbHost") + ":" + prop.getProperty("dbPort") + "/"
						+ prop.getProperty("dbName"), props1);
		Properties props2 = new Properties();
		props2.setProperty("user", prop.getProperty("monitorDbUser"));
		props2.setProperty("password", prop.getProperty("monitorDbPassword"));
		monitorDBConnection = DriverManager.getConnection("jdbc:postgresql://" + prop.getProperty("monitorDbHost")
				+ ":" + prop.getProperty("monitorDbPort") + "/" + prop.getProperty("monitorDbName"), props2);
	}

	protected void execSQLFile(Connection conn, String sqlFile, boolean allTogether) throws IOException, SQLException,
	FileNotFoundException {
		int failCount = 0;
		try (Statement stmt = conn.createStatement();
				BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
			StringBuilder sql = new StringBuilder();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				int commentIndex = line.indexOf("--");
				if (commentIndex != -1) {
					line = line.substring(0, commentIndex);
				}
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				sql.append(line + " ");
				if (!allTogether && line.endsWith(";")) {
					failCount += runSQL(stmt, sql.toString()) ? 0 : 1;
					sql.setLength(0);

				}

			}
			if (allTogether) {
				failCount += runSQL(stmt, sql.toString()) ? 0 : 1;
				;
			}

		}
		System.out.print("Running " + sqlFile + ": ");
		System.out
		.println(failCount == 0 ? "All SQL staments ran successfully" : failCount + " statements have failed");
	}

	protected boolean runSQL(Statement stmt, String sqlString) throws SQLException {
		try {
			// System.out.print("Executing: " + sqlString);
			stmt.execute(sqlString);
			// System.out.println(" OK!");
			return true;
		} catch (SQLException e) {
			System.err.println(" Failed!: " + e.getMessage());
			return false;
		}
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private void startSystem(final int clientId, final int samples, final int threads,
			final TestWorkloadFactory testTestWorkloadFactory) throws IOException, InterruptedException {
		Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread th, Throwable ex) {
				threadExceptions.add(ex);
			}
		};

		Thread serverThread = new Thread() {
			@Override
			public void run() {
				try {
					MiddlewareServer.main(new String[] { "config/localhost.properties", threads + "", "true",
							"" + (samples + 1) });
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		serverThread.start();
		serverThread.setUncaughtExceptionHandler(exceptionHandler);

		long start = System.currentTimeMillis();
		while (!MiddlewareServer.active) {
			if (System.currentTimeMillis() - start > timeout) {
				Assert.fail("Timeout for server start up");
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// nothing
			}
		}

		ClientWorkloadPool.active = true;

		ClientWorkloadPool.workloadFactoryOverride = testTestWorkloadFactory;
		Thread clientThread = new Thread() {
			@Override
			public void run() {
				try {
					ClientWorkloadPool.main(new String[] { "config/localhost.properties", "localhost", clientId + "",
							(clientId + threads - 1) + "", "none", "true", "" + (samples + 1) });
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		clientThread.start();
		clientThread.setUncaughtExceptionHandler(exceptionHandler);

	}

	protected void runSystemWithThreads(final int clientId, final int samples, int threads,
			TestWorkloadFactory testTestWorkloadFactory) throws Throwable {
		runSystem(clientId, samples, 0, threads, testTestWorkloadFactory);
	}

	protected void runSystem(final int clientId, final int samples, TestWorkload workload) throws Throwable {
		runSystem(clientId, samples, 0, workload);
	}

	protected void runSystem(final int clientId, final int samples, final int offset, TestWorkload workload)
			throws Throwable {
		runSystem(clientId, samples, offset, 1, new TestWorkloadFactory(workload));
	}

	protected void runSystem(final int clientId, final int samples, final int offset, int threads,
			TestWorkloadFactory testWorkloadFactory) throws Throwable {
		startSystem(clientId, samples, threads, testWorkloadFactory);

		List<TestWorkload> workloads = waitForShutdown(testWorkloadFactory);

		assertClientMeasurements(clientId, offset, workloads);
		assertMiddlewareMeasurements(clientId, offset, workloads);
	}

	protected List<TestWorkload> waitForShutdown(TestWorkloadFactory testWorkloadFactory) throws Throwable {
		long start = System.currentTimeMillis();
		List<TestWorkload> workloads = new ArrayList<TestWorkload>();
		boolean breakOuter = false;
		while (ClientWorkloadPool.active || MiddlewareServer.active) {
			if (threadExceptions.size() > 0) {
				break;
			}

			while (true) {
				TestWorkload workload = testWorkloadFactory.getWorkloads().poll();
				if (workload == null) {
					break;
				}
				workloads.add(workload);
			}

			for (TestWorkload workload : workloads) {
				if (workload.getCaughtThrowables().size() > 0) {
					breakOuter = true;
					break;
				}
				if (System.currentTimeMillis() - start > timeout) {
					Assert.fail("Timeout. Check if you specified the correct amout of samples...");
				}
			}
			if (breakOuter) {
				break;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// nothing
			}
		}

		for (Throwable t : threadExceptions) {
			throw t;
		}
		for (TestWorkload workload : workloads) {
			for (Throwable t : workload.getCaughtThrowables()) {
				throw t;
			}
		}
		return workloads;
	}
}
