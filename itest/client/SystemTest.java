package client;

import java.io.IOException;
import java.sql.Statement;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import shared.dto.CreateQueueRequestDTO;
import shared.dto.CreateQueueResponseDTO;
import shared.dto.DeleteQueueRequestDTO;
import shared.dto.FindQueuesWithMessagesRequestDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.ResponseDTO;
import shared.dto.ResponseType;
import shared.dto.SendMessageRequestDTO;

public class SystemTest extends AbstractSystemTest {

	@Test
	public void testCreateQueue() throws Throwable {
		runSystem(1, 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				ResponseDTO responseDTO = request(new CreateQueueRequestDTO());
				Assert.assertNotNull(responseDTO);
				Assert.assertEquals(ResponseType.OK, responseDTO.getResponseType());
				Assert.assertTrue(responseDTO instanceof CreateQueueResponseDTO);
				Assert.assertEquals((Integer) 31, ((CreateQueueResponseDTO) responseDTO).getQueueId());
			}
		});
	}

	@Test
	public void testCreateQueueWithDatabaseError() throws Throwable {
		try (Statement stmt = dbConnection.createStatement()) {
			stmt.execute("DROP TABLE message");
			stmt.execute("DROP TABLE queue");
		}
		runSystem(1, 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new CreateQueueRequestDTO()), ResponseType.FAILURE_TO_CREATE_QUEUE);
			}
		});
	}

	@Test
	public void testDeleteQueue() throws Throwable {
		runSystem(1, 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new DeleteQueueRequestDTO(1)));
			}
		});
	}

	@Test
	public void testDeleteNonExistingQueue() throws Throwable {
		runSystem(1, 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new DeleteQueueRequestDTO(31)), ResponseType.QUEUE_DOES_NOT_EXIST);
			}
		});
	}

	@Test
	public void testDeleteWithDatabaseError() throws Throwable {
		try (Statement stmt = dbConnection.createStatement()) {
			stmt.execute("DROP TABLE message");
			stmt.execute("DROP TABLE queue");
		}
		runSystem(1, 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new DeleteQueueRequestDTO(1)), ResponseType.FAILURE_TO_DELETE_QUEUE);
			}
		});
	}

	@Test
	public void testSendMessageNoQueue() throws Throwable {
		runSystem(1, 2, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new SendMessageRequestDTO(null, 31, smallMsgText)),
						ResponseType.QUEUE_DOES_NOT_EXIST);
				assertError(request(new SendMessageRequestDTO(1, 31, smallMsgText)), ResponseType.QUEUE_DOES_NOT_EXIST);
			}
		});
	}

	@Test
	public void testReadMessageNoQueue() throws Throwable {
		runSystem(1, 2, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new ReadMessageRequestDTO(null, 31, true)), ResponseType.QUEUE_DOES_NOT_EXIST);
				assertError(request(new ReadMessageRequestDTO(null, 31, false)), ResponseType.QUEUE_DOES_NOT_EXIST);
			}
		});
	}

	@Test
	public void testSendReadMessageEmptyQueue() throws Throwable {
		runSystem(1, 2, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new ReadMessageRequestDTO(null, 1, true)), ResponseType.QUEUE_IS_EMPTY);
				assertError(request(new ReadMessageRequestDTO(null, 1, false)), ResponseType.QUEUE_IS_EMPTY);
			}
		});
	}

	@Test
	public void testSendMessageDatabaseError() throws Throwable {
		try (Statement stmt = dbConnection.createStatement()) {
			stmt.execute("DROP TABLE message");
			stmt.execute("DROP TABLE queue");
		}
		runSystem(1, 2, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new SendMessageRequestDTO(null, 1, smallMsgText)), ResponseType.FAILURE_TO_WRITE);
				assertError(request(new SendMessageRequestDTO(1, 1, smallMsgText)), ResponseType.FAILURE_TO_WRITE);
			}
		});
	}

	@Test
	public void testReadMessageDatabaseError() throws Throwable {
		try (Statement stmt = dbConnection.createStatement()) {
			stmt.execute("DROP TABLE message");
			stmt.execute("DROP TABLE queue");
		}
		runSystem(1, 2, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new ReadMessageRequestDTO(null, 1, true)), ResponseType.FAILURE_TO_READ);
				assertError(request(new ReadMessageRequestDTO(null, 1, false)), ResponseType.FAILURE_TO_READ);
			}
		});
	}

	/**
	 * Create queuue, send message, read message, pop message, read message, pop meessage. Last 2 should complain that
	 * queue is empty.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testSendReadMessageOneSmallItem() throws Throwable {
		runSystem(1, 5, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new SendMessageRequestDTO(null, 1, smallMsgText)));
				assertRead(1L, 1, null, 1, smallMsgText, request(new ReadMessageRequestDTO(null, 1, true)));
				assertRead(1L, 1, null, 1, smallMsgText, request(new ReadMessageRequestDTO(null, 1, false)));
				assertError(request(new ReadMessageRequestDTO(null, 1, true)), ResponseType.QUEUE_IS_EMPTY);
				assertError(request(new ReadMessageRequestDTO(null, 1, false)), ResponseType.QUEUE_IS_EMPTY);
			}
		});
	}

	/**
	 * Create queuue, send message, read message, pop message, read message, pop meessage. Last 2 should complain that
	 * queue is empty.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testSendReadMessageOneLargeItem() throws Throwable {
		runSystem(1, 5, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new SendMessageRequestDTO(null, 1, largeMsgText)));
				assertRead(1L, 1, null, 1, largeMsgText, request(new ReadMessageRequestDTO(null, 1, true)));
				assertRead(1L, 1, null, 1, largeMsgText, request(new ReadMessageRequestDTO(null, 1, false)));
				assertError(request(new ReadMessageRequestDTO(null, 1, true)), ResponseType.QUEUE_IS_EMPTY);
				assertError(request(new ReadMessageRequestDTO(null, 1, false)), ResponseType.QUEUE_IS_EMPTY);
			}
		});
	}

	/**
	 * Create queuue, send message, read message, pop message, read message, pop meessage. Last 2 should complain that
	 * queue is empty.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testSendReadMessageMixedSizeItems() throws Throwable {
		runSystem(1, 32, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				for (int i = 0; i < 10; ++i) {
					assertOk(request(new SendMessageRequestDTO(null, 1, i % 2 == 0 ? smallMsgText : largeMsgText)));
				}
				for (int i = 0; i < 10; ++i) {
					assertRead(1L, 1, null, 1, smallMsgText, request(new ReadMessageRequestDTO(null, 1, true)));
				}
				for (int i = 0; i < 10; ++i) {
					assertRead((long) (i + 1), 1, null, 1, i % 2 == 0 ? smallMsgText : largeMsgText,
							request(new ReadMessageRequestDTO(null, 1, false)));
				}
				assertError(request(new ReadMessageRequestDTO(null, 1, true)), ResponseType.QUEUE_IS_EMPTY);
				assertError(request(new ReadMessageRequestDTO(null, 1, false)), ResponseType.QUEUE_IS_EMPTY);
			}
		});
	}

	/**
	 * Make sure delete queue really removes queue and its messages.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testSendMessageRemovedQueue() throws Throwable {
		runSystem(1, 4, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new SendMessageRequestDTO(null, 1, smallMsgText)));
				assertOk(request(new DeleteQueueRequestDTO(1)));
				assertError(request(new SendMessageRequestDTO(null, 1, smallMsgText)),
						ResponseType.QUEUE_DOES_NOT_EXIST);
				assertError(request(new SendMessageRequestDTO(1, 1, smallMsgText)), ResponseType.QUEUE_DOES_NOT_EXIST);
			}
		});
	}

	@Test
	public void testReadBadQuery() throws Throwable {
		runSystem(1, 2, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new ReadMessageRequestDTO(null, null, false)), ResponseType.BAD_QUERY);
				assertError(request(new ReadMessageRequestDTO(null, null, true)), ResponseType.BAD_QUERY);
			}
		});
	}

	/**
	 * One client send messages specifically to other client as well as non-specific messages.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testSendReadSpecificMessage() throws Throwable {
		runSystem(1, 12, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				for (int i = 0; i < 12; ++i) {
					// sends messages to null, 1 and 2, 4 times each with small and large messages
					assertOk(request(new SendMessageRequestDTO(i % 3 == 0 ? null : i % 3, 1, i % 2 == 0 ? smallMsgText
							: largeMsgText)));
				}
			}
		});

		runSystem(2, 9, 13, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				for (int i = 0; i < 12; ++i) {
					if (i % 3 == 1) {
						continue; // skip
					}
					assertRead((long) (i + 1), 1, i % 3 == 2 ? 2 : null, 1, i % 2 == 0 ? smallMsgText : largeMsgText,
							request(new ReadMessageRequestDTO(null, 1, false)));
				}
				assertError(request(new ReadMessageRequestDTO(null, 1, false)), ResponseType.QUEUE_IS_EMPTY);
			}
		});
	}

	@Test
	public void testFindQueuesFail() throws Throwable {
		try (Statement stmt = dbConnection.createStatement()) {
			stmt.execute("DROP TABLE message");
			stmt.execute("DROP TABLE queue");
		}
		runSystem(1, 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertError(request(new FindQueuesWithMessagesRequestDTO()), ResponseType.FAILURE_TO_FIND_QUEUES);
			}
		});
	}

	@Test
	public void testFindQueuesEmptyResult() throws Throwable {
		runSystem(1, 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertFindQueues(Arrays.<Long> asList(), request(new FindQueuesWithMessagesRequestDTO()));
			}
		});
	}

	@Test
	public void testFindQueues() throws Throwable {
		runSystem(1, 6, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new SendMessageRequestDTO(2, 1, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 2, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(3, 3, largeMsgText)));
				assertOk(request(new SendMessageRequestDTO(1, 4, largeMsgText)));
				assertOk(request(new SendMessageRequestDTO(4, 5, smallMsgText)));
				assertFindQueues(Arrays.<Long> asList(2L, 4L), request(new FindQueuesWithMessagesRequestDTO()));
			}
		});
	}

	@Test
	public void testFindMessageFromSpecificSenderNone() throws Throwable {
		runSystem(3, 12, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new SendMessageRequestDTO(1, 1, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(2, 2, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(3, 3, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 1, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 2, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 3, smallMsgText)));

				assertError(request(new ReadMessageRequestDTO(2, null, true)), ResponseType.NO_MESSAGE_MATCHING_QUERY);
				assertError(request(new ReadMessageRequestDTO(2, 1, true)), ResponseType.QUEUE_IS_EMPTY);
				assertError(request(new ReadMessageRequestDTO(2, 31, true)), ResponseType.QUEUE_DOES_NOT_EXIST);

				assertError(request(new ReadMessageRequestDTO(2, null, false)), ResponseType.NO_MESSAGE_MATCHING_QUERY);
				assertError(request(new ReadMessageRequestDTO(2, 1, false)), ResponseType.QUEUE_IS_EMPTY);
				assertError(request(new ReadMessageRequestDTO(2, 31, false)), ResponseType.QUEUE_DOES_NOT_EXIST);
			}
		});
	}

	@Test
	public void testFindMessageFromSpecificSender() throws Throwable {
		runSystem(1, 5, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new SendMessageRequestDTO(2, 1, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 2, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(3, 3, largeMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 4, largeMsgText)));
				assertOk(request(new SendMessageRequestDTO(4, 5, smallMsgText)));
			}
		});

		runSystem(2, 5, 5 + 1, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertOk(request(new SendMessageRequestDTO(2, 1, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 2, smallMsgText)));
				assertOk(request(new SendMessageRequestDTO(3, 3, largeMsgText)));
				assertOk(request(new SendMessageRequestDTO(null, 4, largeMsgText)));
				assertOk(request(new SendMessageRequestDTO(4, 5, smallMsgText)));
			}
		});

		runSystem(3, 6, 10 + 2, new TestWorkload() {
			@Override
			public void runTestWorkload() throws IOException {
				assertRead(7L, 2, null, 2, smallMsgText, request(new ReadMessageRequestDTO(2, null, true)));
				assertRead(8L, 2, 3, 3, largeMsgText, request(new ReadMessageRequestDTO(2, 3, true)));
				assertRead(8L, 2, 3, 3, largeMsgText, request(new ReadMessageRequestDTO(2, 3, false)));
				assertRead(7L, 2, null, 2, smallMsgText, request(new ReadMessageRequestDTO(2, null, true)));
				assertRead(7L, 2, null, 2, smallMsgText, request(new ReadMessageRequestDTO(2, null, false)));
				assertRead(9L, 2, null, 4, largeMsgText, request(new ReadMessageRequestDTO(2, null, true)));
			}
		});
	}

	@Test
	public void testWrongLogin() throws Throwable {
		try {
			runSystem(260, 0, new TestWorkload() {
				@Override
				public void runTestWorkload() throws IOException {
					// empty
				}
			});
		} catch (RuntimeException e) {
			Assert.assertEquals("java.lang.RuntimeException: Unable to log in!", e.getMessage());
		}
	}

	@Test
	public void testFailureLogin() throws Throwable {
		try (Statement stmt = dbConnection.createStatement()) {
			stmt.execute("DROP TABLE account");
		}
		try {
			runSystem(1, 0, new TestWorkload() {
				@Override
				public void runTestWorkload() throws IOException {
					// empty
				}
			});
		} catch (RuntimeException e) {
			Assert.assertEquals("java.lang.RuntimeException: Unable to log in!", e.getMessage());
		}
	}
}
