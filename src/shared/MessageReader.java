package shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import shared.dto.DataTransferObject;

public class MessageReader implements AutoCloseable {

	private BufferedReader reader;
	private MessageDeserializer deserializer;
	private ThreadMonitor monitor;

	public MessageReader(InputStream is, ThreadMonitor monitor) throws UnsupportedEncodingException {
		reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		deserializer = new MessageDeserializer();
		this.monitor = monitor;
	}

	public DataTransferObject read() throws IOException {
		String line = reader.readLine();
		if (line == null) {
			throw new IOException("Read null");
		}
		monitor.networkReadEnd();
		DataTransferObject dto = deserializer.deserialize(line);
		monitor.deserializeEnd();
		return dto;
	}

	@Override
	public void close() throws Exception {
		if (reader != null) {
			reader.close();
		}
	}

}
