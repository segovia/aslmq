package dbtester;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import middleware.MiddlewareDBConnection;
import shared.Logger;
import shared.PropertiesLoader;
import shared.SampleMessage;
import client.ClientWorkloadPool;

/**
 * Starting point for Database tester program
 *
 * @author gustavo
 *
 */
public class DatabaseTester {

	private static ExecutorService pool;
	private static Set<DatabaseNetworkTesterRunnable> activeRunnables = new HashSet<>();
	public static volatile boolean active = false;
	private static volatile DatabaseTesterMasterMonitor masterMonitor = null;

	public static void main(String[] args) throws SQLException {
		int argIdx = 0;
		String propertiesFile = args[argIdx++];
		Properties prop = PropertiesLoader.load(propertiesFile);
		Integer maxDbConnections = Integer.parseInt(args[argIdx++]);
		int measurementSamples = Integer.parseInt(args[argIdx++]);

		addShutdownHook();
		masterMonitor = new DatabaseTesterMasterMonitor(measurementSamples, prop.getProperty("monitorDbHost"),
				prop.getProperty("monitorDbPort"), prop.getProperty("monitorDbName"),
				prop.getProperty("monitorDbUser"), prop.getProperty("monitorDbPassword"));

		String msg = "large".equals(args[argIdx++]) ? SampleMessage.LARGE_MSG : SampleMessage.SMALL_MSG;
		for (int i = 0; i < maxDbConnections; i++) {
			activeRunnables.add(new DatabaseNetworkTesterRunnable(i + 1, prop.getProperty("dbHost"), prop
					.getProperty("dbPort"), prop.getProperty("dbName"), prop.getProperty("dbUser"), prop
					.getProperty("dbPassword"), masterMonitor, msg));
		}

		Logger.info("DatabaseNetworkTester - online");
		pool = Executors.newCachedThreadPool();
		pool.execute(masterMonitor);
		active = true;
		for (DatabaseNetworkTesterRunnable runnable : activeRunnables) {
			pool.execute(runnable);
		}
	}

	private static class DatabaseNetworkTesterRunnable implements Runnable, AutoCloseable {

		private DatabaseTesterMasterMonitor masterMonitor;
		private MiddlewareDBConnection conn;
		private DatabaseTesterDefaultWorkload workload;

		public DatabaseNetworkTesterRunnable(int clientId, String dbHost, String dbPort, String dbName, String dbUser,
				String dbPassword, DatabaseTesterMasterMonitor masterMonitor, String msg) throws SQLException {
			this.masterMonitor = masterMonitor;
			conn = new MiddlewareDBConnection(dbHost, dbPort, dbName, dbUser, dbPassword);
			workload = new DatabaseTesterDefaultWorkload(clientId, conn, new DatabaseTesterMonitor(masterMonitor), msg);
		}

		@Override
		public void run() {
			try {
				int i = 0;
				while (true) {
					workload.runWorkload();
					if (i == 1600) { // check every 16000 if it should stop
						if (masterMonitor.storing) {
							break;
						}
						i = 0;
					}
					++i;
				}
			} catch (RuntimeException e) {
				// might throw exception if shutting down
				if (ClientWorkloadPool.active && !masterMonitor.storing) {
					e.printStackTrace();
				}
			}

		};

		@Override
		public void close() throws Exception {
			conn.close();
		}
	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdownNow();
			}
		});
	}

	public static void shutdownNow() {
		if (!active) {
			return;
		}
		active = false;
		for (DatabaseNetworkTesterRunnable runnable : activeRunnables) {
			try {
				runnable.close();
			} catch (Exception e) {
				// ignore
			}
		}
		activeRunnables = null;

		if (masterMonitor != null) {
			masterMonitor.shutdown();
			masterMonitor = null;
		}

		pool.shutdownNow();
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
		pool = null;
		Logger.info("DatabaseNetworkTester - terminated");
	}
}
