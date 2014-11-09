package client;

import java.io.IOException;

import shared.SampleMessage;
import shared.dto.CreateQueueRequestDTO;
import shared.dto.CreateQueueResponseDTO;
import shared.dto.DeleteQueueRequestDTO;
import shared.dto.FindQueuesWithMessagesRequestDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.SendMessageRequestDTO;

/**
 * Generates the default workload for experiments
 * 
 * @author gustavo
 *
 */
public class DefaultWorkload extends ClientWorkload {

	private static final int TARGET_QUEUE = 1;

	private final String msg;

	public DefaultWorkload(String workloadName) {
		msg = "large".equals(workloadName) ? SampleMessage.LARGE_MSG : SampleMessage.SMALL_MSG;
	}

	@Override
	public void runWorkload() throws IOException {

		request(new SendMessageRequestDTO(null, TARGET_QUEUE, msg));
		request(new ReadMessageRequestDTO(null, TARGET_QUEUE, true));
		request(new ReadMessageRequestDTO(null, TARGET_QUEUE, false));

		request(new SendMessageRequestDTO(getClientId(), TARGET_QUEUE, msg));
		request(new ReadMessageRequestDTO(getClientId(), TARGET_QUEUE, true));
		request(new ReadMessageRequestDTO(getClientId(), TARGET_QUEUE, false));

		Integer createdQueueId = ((CreateQueueResponseDTO) request(new CreateQueueRequestDTO())).getQueueId();
		request(new SendMessageRequestDTO(null, createdQueueId, msg));
		request(new FindQueuesWithMessagesRequestDTO());
		request(new ReadMessageRequestDTO(getClientId(), null, true));
		request(new ReadMessageRequestDTO(null, createdQueueId, false));
		request(new DeleteQueueRequestDTO(createdQueueId));
	}

}
