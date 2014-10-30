package client;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestWorkloadFactory implements WorkloadFactory {

	private TestWorkload workloadOverride;
	private Queue<TestWorkload> workloads = new LinkedBlockingQueue<TestWorkload>();

	public TestWorkloadFactory() {
		// no override
	}

	public TestWorkloadFactory(TestWorkload workloadOverride) {
		this.workloadOverride = workloadOverride;
	}

	@Override
	public ClientWorkload instanciate(String workloadName, Integer id, String serverHost, Integer serverPort,
			ClientMasterMonitor masterMonitor) throws IOException {
		TestWorkload testWorkload = workloadOverride != null ? workloadOverride : newInstance();
		testWorkload.init(id, serverHost, serverPort, masterMonitor);
		register(testWorkload);
		return testWorkload;
	}

	public TestWorkload newInstance() {
		throw new RuntimeException("You should implement this, if you do not override workload");
	}

	public void register(TestWorkload testWorkload) {
		workloads.add(testWorkload);
	}

	public Queue<TestWorkload> getWorkloads() {
		return workloads;
	}

}
