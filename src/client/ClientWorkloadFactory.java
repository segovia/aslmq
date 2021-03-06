package client;

import java.io.IOException;

/**
 * Client implementation of workload factory
 * 
 * @author gustavo
 *
 */
public class ClientWorkloadFactory implements WorkloadFactory {

	@Override
	public ClientWorkload instanciate(String workloadName, Integer id, String serverHost, Integer serverPort,
			ClientMasterMonitor masterMonitor) throws IOException {
		DefaultWorkload defaultWorkload = new DefaultWorkload(workloadName);
		defaultWorkload.init(id, serverHost, serverPort, masterMonitor);
		return defaultWorkload;
	}
}
