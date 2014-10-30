package client;

import java.io.IOException;

public interface WorkloadFactory {

	ClientWorkload instanciate(String workloadName, Integer id, String serverHost, Integer serverPort,
			ClientMasterMonitor masterMonitor) throws IOException;
}
