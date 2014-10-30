package shared.dto;

import java.util.ArrayList;
import java.util.List;

public class FindQueuesWithMessagesResponseDTO extends ResponseDTO {

	private List<Long> queueIds;

	public FindQueuesWithMessagesResponseDTO(String[] tokens) {
		super(ResponseType.OK);
		queueIds = new ArrayList<>();
		if (tokens.length >= 2) {
			String[] queueIdArray = tokens[1].split(",");
			for (int i = 0; i < queueIdArray.length; i++) {
				queueIds.add(Long.parseLong(queueIdArray[i]));
			}
		}
	}

	public FindQueuesWithMessagesResponseDTO(List<Long> queueIds) {
		super(ResponseType.OK);
		this.queueIds = queueIds;
	}

	public List<Long> getQueueIds() {
		return queueIds;
	}

	@Override
	public String serialize() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < queueIds.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(queueIds.get(i));
		}
		return FindQueuesWithMessagesResponseDTO.class.getSimpleName() + "|" + sb;
	}
}
