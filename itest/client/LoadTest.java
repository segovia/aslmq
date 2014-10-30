package client;

import java.io.IOException;

import org.junit.Test;

import shared.dto.CreateQueueRequestDTO;
import shared.dto.SendMessageRequestDTO;

public class LoadTest extends AbstractSystemTest {

	@Test
	public void testSendLoad() throws Throwable {
		setTimeout(1000 * 60 * 5);
		final int msgCount = 20002;
		int threads = 32;
		final int msgPerThread = msgCount / threads;
		runSystemWithThreads(1, (msgPerThread + 1) * threads, threads, new TestWorkloadFactory() {
			@Override
			public TestWorkload newInstance() {
				return new TestWorkload() {
					@Override
					public void runTestWorkload() throws IOException {
						request(new CreateQueueRequestDTO());
						for (int i = 0; i < msgPerThread; i++) {
							request(new SendMessageRequestDTO(null, 1, smallMsgText));
						}
					}
				};
			}
		});
	}
}
