package client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Assert;
import org.junit.Test;

import shared.dto.CreateQueueRequestDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.ReadMessageResponseDTO;
import shared.dto.ResponseDTO;
import shared.dto.ResponseType;
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

	@Test
	public void testConcurrentLoad() throws Throwable {
		setTimeout(1000 * 60 * 60);
		final int msgCount = 3200;
		int threads = 16;
		final int msgPerThread = msgCount / threads;
		System.out.println(msgPerThread);
		runSystemWithThreadsSkipAssert(1, msgPerThread * threads + threads - 1, threads, new TestWorkloadFactory() {
			@Override
			public TestWorkload newInstance() {
				return new TestWorkload() {
					@Override
					public void runTestWorkload() throws IOException {
						for (int i = 0; i < msgPerThread; i++) {
							request(new SendMessageRequestDTO(null, 1, String.valueOf((getClientId() - 1)
									* msgPerThread + i)));
						}
					}
				};
			}
		});

		System.out.println("Loading done");

		int readThreads = 16;
		final int msgPerReadThread = msgCount / readThreads;

		final BlockingQueue<Integer> q = new LinkedBlockingQueue<>();
		runSystemWithThreadsSkipAssert(1, msgPerReadThread * readThreads + readThreads - 1, readThreads,
				new TestWorkloadFactory() {
			@Override
			public TestWorkload newInstance() {
				return new TestWorkload() {
					@Override
					public void runTestWorkload() throws IOException {
						for (int i = 0; i < msgPerReadThread; i++) {
							ResponseDTO request = request(new ReadMessageRequestDTO(null, 1, false));
							if (request.getResponseType() == ResponseType.OK) {
								int val = Integer.parseInt(((ReadMessageResponseDTO) request).getMessageText());
								q.add(val);
								continue;
							}
							throw new RuntimeException(request.getResponseType() + "");
						}
					}
				};
			}
		});

		boolean[] vals = new boolean[msgPerThread * threads];
		for (int i = 0; i < vals.length; i++) {
			Assert.assertFalse("i = " + i + " is not true", vals[i]);
		}

		int count = 0;
		while (count < msgPerThread * threads) {
			Integer val = q.take();
			vals[val] = true;
			++count;
		}

		Assert.assertEquals(msgPerThread * threads, count);
		for (int i = 0; i < vals.length; i++) {
			Assert.assertTrue("i = " + i + " is not true", vals[i]);
		}

	}
}
