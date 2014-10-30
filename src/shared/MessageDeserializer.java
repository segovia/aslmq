package shared;

import java.util.HashMap;
import java.util.Map;

import shared.dto.CreateQueueRequestDTO;
import shared.dto.CreateQueueResponseDTO;
import shared.dto.DataTransferObject;
import shared.dto.DeleteQueueRequestDTO;
import shared.dto.ErrorResponseDTO;
import shared.dto.FindQueuesWithMessagesRequestDTO;
import shared.dto.FindQueuesWithMessagesResponseDTO;
import shared.dto.LoginRequestDTO;
import shared.dto.LogoutRequestDTO;
import shared.dto.OKResponseDTO;
import shared.dto.ReadMessageRequestDTO;
import shared.dto.ReadMessageResponseDTO;
import shared.dto.SendMessageRequestDTO;

/**
 * Serializes messages to an array of bytes. The message format in string is {className[,attribute:value]}. This format
 * is inspired by JSON. It does not allow nesting of objects, just top level.
 *
 * @author Gustavo
 *
 */
public class MessageDeserializer {

	private Map<String, Class<? extends DataTransferObject>> classMap = new HashMap<>();

	public MessageDeserializer() {
		registerClass(CreateQueueRequestDTO.class);
		registerClass(CreateQueueResponseDTO.class);
		registerClass(DeleteQueueRequestDTO.class);
		registerClass(ErrorResponseDTO.class);
		registerClass(FindQueuesWithMessagesRequestDTO.class);
		registerClass(FindQueuesWithMessagesResponseDTO.class);
		registerClass(LoginRequestDTO.class);
		registerClass(LogoutRequestDTO.class);
		registerClass(OKResponseDTO.class);
		registerClass(ReadMessageRequestDTO.class);
		registerClass(ReadMessageResponseDTO.class);
		registerClass(SendMessageRequestDTO.class);
	}

	private void registerClass(Class<? extends DataTransferObject> clazz) {
		classMap.put(clazz.getSimpleName(), clazz);
	}

	public DataTransferObject deserialize(String s) {
		String tokens[] = s.split("\\|");
		String className = tokens[0];
		Class<? extends DataTransferObject> dtoClass = classMap.get(className);
		if (dtoClass == null) {
			throw new RuntimeException("Class not supported for deserialization " + className);
		}
		try {
			Object[] arg = { tokens };
			return dtoClass.getConstructor(String[].class).newInstance(arg);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
