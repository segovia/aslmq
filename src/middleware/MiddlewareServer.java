package middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import shared.Logger;
import shared.PropertiesLoader;
import shared.dto.ResponseType;

// TODO figure out benchmarking serialization
public class MiddlewareServer {

	private static ExecutorService pool;

	private static Set<RequestHandler> activeRequestHandlers = new HashSet<>();
	private static ServerSocket serverSocket;
	public static volatile boolean active = false;
	private static volatile MiddlewareMasterMonitor masterMonitor = null;

	// TODO I should read oldest message, not newest.
	// TODO make sure I am not create statements all the time
	// TODO consider using Externalizable for serialization performance
	// TODO have a test that proves concurrency is ok with one big queue and every one deleting it
	// TODO and a check for concurrency by having 20 big queues and 20 threads deleting it all
	// TODO test queue delete on cascade
	// TODO check if index is actually used (mention that the queries were analyzed)
	// TODO create a cache for getting only peek messages
	public static void main(String[] args) throws SQLException, IOException {
		int argIdx = 0;
		String propertiesFile = args[argIdx++];
		Properties prop = PropertiesLoader.load(propertiesFile);
		Integer maxDbConnections = Integer.parseInt(args[argIdx++]);
		boolean monitor = Boolean.parseBoolean(args[argIdx++]);
		Integer serverPort = Integer.parseInt(prop.getProperty("serverPort"));

		addShutdownHook();

		MiddlewareDBConnectionPool.configure(maxDbConnections, prop.getProperty("dbHost"), prop.getProperty("dbPort"),
				prop.getProperty("dbName"), prop.getProperty("dbUser"), prop.getProperty("dbPassword"));

		pool = Executors.newCachedThreadPool();
		boolean mockMode = false;
		if (monitor) {
			int measurementSamples = Integer.parseInt(args[argIdx++]);
			masterMonitor = new MiddlewareMasterMonitor(measurementSamples, prop.getProperty("monitorDbHost"),
					prop.getProperty("monitorDbPort"), prop.getProperty("monitorDbName"),
					prop.getProperty("monitorDbUser"), prop.getProperty("monitorDbPassword"));
			pool.execute(masterMonitor);
		} else {
			mockMode = Boolean.parseBoolean(args[argIdx++]);
			masterMonitor = new MiddlewareMasterMonitor(); // inactive monitor
		}

		serverSocket = new ServerSocket(serverPort);
		active = true;
		try {
			Logger.info("Server - online on port " + serverPort);
			while (true) {
				RequestHandler requestHandler = new RequestHandler(serverSocket.accept(), masterMonitor, mockMode);
				activeRequestHandlers.add(requestHandler);
				pool.execute(requestHandler);
				checkActiveRequestHandlers(); // garbage collection
			}
		} catch (IOException e) {
			if (e instanceof SocketException && e.getMessage().toLowerCase().startsWith("socket closed")) {
				// it ok, shutting down
			} else {
				Logger.error(ResponseType.UNEXPECTED_ERROR, e);
			}
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

	public static void checkActiveRequestHandlers() {
		Set<RequestHandler> toRemove = null;
		for (RequestHandler handler : activeRequestHandlers) {
			if (!handler.isActive()) {
				if (toRemove == null) {
					toRemove = new HashSet<>();
				}
				toRemove.add(handler);
			}
		}
		if (toRemove != null) {
			activeRequestHandlers.removeAll(toRemove);
		}
	}

	public static void shutdownNow() {
		if (!active) {
			return;
		}
		active = false;
		closeSockets();
		MiddlewareDBConnectionPool.closeConnections();

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
		Logger.info("Server - terminated");
	}

	private static void closeSockets() {
		for (RequestHandler handler : activeRequestHandlers) {
			if (handler.isActive()) {
				handler.shutdown();
			}
		}
		activeRequestHandlers = new HashSet<>();
		try {
			serverSocket.close();
		} catch (IOException e) {
			// ignore
		}
		serverSocket = null;
	}

}
