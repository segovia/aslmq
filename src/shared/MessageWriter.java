package shared;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import shared.dto.DataTransferObject;

public class MessageWriter implements AutoCloseable {

	private PrintWriter writer;
	private ThreadMonitor monitor;

	public MessageWriter(OutputStream os, ThreadMonitor monitor) throws UnsupportedEncodingException {
		writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
		this.monitor = monitor;
	}

	public void write(DataTransferObject dto) {
		monitor.serializeStart();
		String line = dto.serialize();
		Logger.debug("MessageWriter writing: " + line);
		monitor.networkWriteStart();
		writer.println(line);
	}

	@Override
	public void close() throws Exception {
		if (writer != null) {
			writer.close();
		}
	}

	public void flush() {
		if (writer != null) {
			writer.flush();
		}
	}

}
