package shared;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import shared.dto.CreateQueueRequestDTO;
import shared.dto.CreateQueueResponseDTO;
import shared.dto.DeleteQueueRequestDTO;
import shared.dto.ErrorResponseDTO;
import shared.dto.FindQueuesWithMessagesRequestDTO;
import shared.dto.FindQueuesWithMessagesResponseDTO;
import shared.dto.LoginRequestDTO;
import shared.dto.LogoutRequestDTO;
import shared.dto.OKResponseDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.ReadMessageResponseDTO;
import shared.dto.RequestType;
import shared.dto.ResponseType;
import shared.dto.SendMessageRequestDTO;

public class MessageDeserializerTest {
	private MessageDeserializer md;

	@Before
	public void before() {
		md = new MessageDeserializer();
	}

	@Test
	public void testCreateQueueRequestDTO() {
		CreateQueueRequestDTO dto = new CreateQueueRequestDTO();
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof CreateQueueRequestDTO);
		CreateQueueRequestDTO actual = (CreateQueueRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testCreateQueueResponseDTO() {
		CreateQueueResponseDTO dto = new CreateQueueResponseDTO(123);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof CreateQueueResponseDTO);
		CreateQueueResponseDTO actual = (CreateQueueResponseDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testDeleteQueueRequestDTO() {
		DeleteQueueRequestDTO dto = new DeleteQueueRequestDTO(123);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof DeleteQueueRequestDTO);
		DeleteQueueRequestDTO actual = (DeleteQueueRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testErrorResponseDTO() {
		ErrorResponseDTO dto = new ErrorResponseDTO(ResponseType.UNEXPECTED_ERROR);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof ErrorResponseDTO);
		ErrorResponseDTO actual = (ErrorResponseDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testFindQueuesWithMessagesRequestDTO() {
		FindQueuesWithMessagesRequestDTO dto = new FindQueuesWithMessagesRequestDTO();
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof FindQueuesWithMessagesRequestDTO);
		FindQueuesWithMessagesRequestDTO actual = (FindQueuesWithMessagesRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testFindQueuesWithMessagesResponseDTO() {
		FindQueuesWithMessagesResponseDTO dto = new FindQueuesWithMessagesResponseDTO(Arrays.asList(1L, 2L, 3L));
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof FindQueuesWithMessagesResponseDTO);
		FindQueuesWithMessagesResponseDTO actual = (FindQueuesWithMessagesResponseDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testFindQueuesWithMessagesResponseDTOEmpty() {
		FindQueuesWithMessagesResponseDTO dto = new FindQueuesWithMessagesResponseDTO(Arrays.<Long> asList());
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof FindQueuesWithMessagesResponseDTO);
		FindQueuesWithMessagesResponseDTO actual = (FindQueuesWithMessagesResponseDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testLoginRequestDTO() {
		LoginRequestDTO dto = new LoginRequestDTO(123);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof LoginRequestDTO);
		LoginRequestDTO actual = (LoginRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testLogoutRequestDTO() {
		LogoutRequestDTO dto = new LogoutRequestDTO();
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof LogoutRequestDTO);
		LogoutRequestDTO actual = (LogoutRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testOKResponseDTO() {
		OKResponseDTO dto = new OKResponseDTO();
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof OKResponseDTO);
		OKResponseDTO actual = (OKResponseDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testReadMessageRequestDTONullQueue() {
		ReadMessageRequestDTO dto = new ReadMessageRequestDTO(123, null, true);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof ReadMessageRequestDTO);
		ReadMessageRequestDTO actual = (ReadMessageRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testReadMessageRequestDTONullSender() {
		ReadMessageRequestDTO dto = new ReadMessageRequestDTO(null, 123, true);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof ReadMessageRequestDTO);
		ReadMessageRequestDTO actual = (ReadMessageRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testReadMessageRequestDTO() {
		ReadMessageRequestDTO dto = new ReadMessageRequestDTO(123, 456, true);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof ReadMessageRequestDTO);
		ReadMessageRequestDTO actual = (ReadMessageRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testReadMessageResponseDTO() {
		ReadMessageResponseDTO dto = new ReadMessageResponseDTO(123L, 456, 789, 111, "te|xt", 999L);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof ReadMessageResponseDTO);
		ReadMessageResponseDTO actual = (ReadMessageResponseDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testReadMessageResponseDTONullRecipient() {
		ReadMessageResponseDTO dto = new ReadMessageResponseDTO(123L, 456, null, 111, "te|xt", 999L);
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof ReadMessageResponseDTO);
		ReadMessageResponseDTO actual = (ReadMessageResponseDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testSendMessageRequestDTO() {
		SendMessageRequestDTO dto = new SendMessageRequestDTO(123, 456, "te\nxt");
		Object obj = md.deserialize(dto.serialize());
		Assert.assertTrue(obj instanceof SendMessageRequestDTO);
		SendMessageRequestDTO actual = (SendMessageRequestDTO) obj;
		Assert.assertEquals(dto.serialize(), actual.serialize());
	}

	@Test
	public void testWithReaderAndWriter() throws Exception {
		SendMessageRequestDTO dto1 = new SendMessageRequestDTO(123, 456, "te\nxt");
		ReadMessageResponseDTO dto2 = new ReadMessageResponseDTO(123L, 45, 67, 89, "te|xt", 999L);
		ReadMessageRequestDTO dto3 = new ReadMessageRequestDTO(null, 123, true);

		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream();
		in.connect(out);
		try (MessageWriter writer = new MessageWriter(out, new MockThreadMonitor())) {
			writer.write(dto1);
			writer.write(dto2);
			writer.write(dto3);
		}

		try (MessageReader reader = new MessageReader(in, new MockThreadMonitor())) {
			Assert.assertEquals(dto1.serialize(), reader.read().serialize());
			Assert.assertEquals(dto2.serialize(), reader.read().serialize());
			Assert.assertEquals(dto3.serialize(), reader.read().serialize());
		}
	}

	private class MockThreadMonitor implements ThreadMonitor {

		@Override
		public void serializeStart() {
		}

		@Override
		public void deserializeEnd() {
		}

		@Override
		public void networkWriteStart() {
		}

		@Override
		public void networkReadEnd() {
		}

		@Override
		public void setRequestType(RequestType requestType) {
		}

		@Override
		public void setResponseType(ResponseType responseType) {
		}

	}

}
