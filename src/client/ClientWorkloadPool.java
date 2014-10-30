package client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import shared.Logger;
import shared.PropertiesLoader;
import shared.dto.ResponseType;

public class ClientWorkloadPool {

	public static volatile boolean active = false;
	public static WorkloadFactory workloadFactoryOverride = null; // for testing

	private static volatile List<ClientWorkload> clientWorkloads = new ArrayList<>();
	private static volatile ClientMasterMonitor masterMonitor = null;
	private static volatile ExecutorService pool = null;

	public static void main(String[] args) throws SQLException {
		int argIdx = 0;
		String propertiesFile = args[argIdx++];
		Properties prop = PropertiesLoader.load(propertiesFile);
		String serverHost = args[argIdx++];
		Integer serverPort = Integer.parseInt(prop.getProperty("serverPort"));
		int minClientId = Integer.parseInt(args[argIdx++]);
		int maxClientId = Integer.parseInt(args[argIdx++]);
		String workloadName = args[argIdx++];
		boolean monitor = Boolean.parseBoolean(args[argIdx++]);

		// instantiate all before running
		if (monitor) {
			int measurementSamples = Integer.parseInt(args[argIdx++]);
			masterMonitor = new ClientMasterMonitor(measurementSamples, prop.getProperty("monitorDbHost"),
					prop.getProperty("monitorDbPort"), prop.getProperty("monitorDbName"),
					prop.getProperty("monitorDbUser"), prop.getProperty("monitorDbPassword"));
		} else {
			masterMonitor = new ClientMasterMonitor(); // inactive monitor
		}

		WorkloadFactory workloadFactory = workloadFactoryOverride == null ? new ClientWorkloadFactory()
				: workloadFactoryOverride;
		for (int clientId = minClientId; clientId <= maxClientId; clientId++) {
			try {
				clientWorkloads.add(workloadFactory.instanciate(workloadName, clientId, serverHost, serverPort,
						masterMonitor));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		addShutdownHook();

		active = true;
		System.out.println("ClientWorkloadPool - online");
		pool = Executors.newCachedThreadPool();
		if (monitor) {
			pool.execute(masterMonitor);
		}
		for (ClientWorkload clientThread : clientWorkloads) {
			pool.execute(clientThread);
		}

		pool.shutdown();
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Logger.error(ResponseType.UNEXPECTED_ERROR, e);
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
		for (ClientWorkload clientWorkload : clientWorkloads) {
			clientWorkload.shutdown();
		}
		clientWorkloads = new ArrayList<>();

		if (masterMonitor != null) {
			masterMonitor.shutdown();
			masterMonitor = null;
		}

		if (pool != null) {
			pool.shutdownNow();
			try {
				pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// ignore
			}
			pool = null;
		}
		Logger.info("ClientWorkloadPool - terminated");
	}

}
