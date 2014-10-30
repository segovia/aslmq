package middleware;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MiddlewareDBConnectionPool {

	private static int maxConnections;
	private static volatile int createdConnections;
	private static boolean maxedOut;
	private static BlockingQueue<MiddlewareDBConnection> availableConnections;
	private static List<MiddlewareDBConnection> connections;

	private static String dbHost;
	private static String dbPort;
	private static String dbName;
	private static String dbUser;
	private static String dbPassword;

	public static void configure(int maxConnections, String dbHost, String dbPort, String dbName, String dbUser,
			String dbPassword) {
		if (maxConnections == 0) {
			// should be mocking
			return;
		}
		MiddlewareDBConnectionPool.maxConnections = maxConnections;
		availableConnections = new ArrayBlockingQueue<>(maxConnections, false);
		MiddlewareDBConnectionPool.dbHost = dbHost;
		MiddlewareDBConnectionPool.dbPort = dbPort;
		MiddlewareDBConnectionPool.dbName = dbName;
		MiddlewareDBConnectionPool.dbUser = dbUser;
		MiddlewareDBConnectionPool.dbPassword = dbPassword;
		maxedOut = false;
		createdConnections = 0;
		connections = new ArrayList<>();
	}

	public static MiddlewareDBConnection acquireConnection() throws InterruptedException {
		MiddlewareDBConnection dbConnection = availableConnections.poll();
		if (dbConnection != null) {
			return dbConnection;
		}
		if (!maxedOut) {
			dbConnection = createConnection();
			if (dbConnection != null) {
				return dbConnection;
			}
		}
		// if code reaches here, connections are maxed out. Nothing to do but wait
		while (true) {
			try {
				return availableConnections.take();
			} catch (InterruptedException e) {
				if (MiddlewareServer.active) {
					System.out.println("Wait for connection interrupted. Trying again.");
				} else {
					throw e;
				}
			}
		}
	}

	private static synchronized MiddlewareDBConnection createConnection() {
		if (createdConnections == maxConnections) {
			maxedOut = true;
			return null;
		}
		// not maxed out yet, create connection
		++createdConnections;
		if (createdConnections == maxConnections) {
			maxedOut = true;
		}
		try {
			MiddlewareDBConnection dbConnection = new MiddlewareDBConnection(dbHost, dbPort, dbName, dbUser, dbPassword);
			connections.add(dbConnection);
			return dbConnection;
		} catch (SQLException e) {
			throw new RuntimeException("Unable to create connection", e);
		}
	}

	public static void releaseConnection(MiddlewareDBConnection dbConnection) {
		availableConnections.add(dbConnection);
	}

	public static int getCreatedDBConnectionsCount() {
		return createdConnections;
	}

	public static int getFreeDBConnectionsCount() {
		return availableConnections.size();
	}

	public static void closeConnections() {
		for (MiddlewareDBConnection dbConnection : connections) {
			try {
				dbConnection.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
