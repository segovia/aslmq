package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import shared.dto.RequestDTO;
import shared.dto.RequestType;
import shared.dto.ResponseDTO;
import shared.dto.ResponseType;

public abstract class TestWorkload extends ClientWorkload {
	private boolean alreadyExecuted = false;
	protected List<Throwable> caughtThrowables = new ArrayList<>();
	protected List<RequestType> requestTypes = new ArrayList<>();
	protected List<ResponseType> responseTypes = new ArrayList<>();

	@Override
	protected ResponseDTO request(RequestDTO requestDTO) throws IOException {
		requestTypes.add(requestDTO.getRequestType());
		ResponseDTO responseDTO = getClientConnection().writeRequestAndReadResponse(requestDTO);
		responseTypes.add(responseDTO.getResponseType());
		return responseDTO;
	}

	@Override
	public void runWorkload() throws IOException {
		if (Thread.interrupted()) {
			throw new IOException("Test thread interrupted.");
		}
		if (alreadyExecuted) {
			return;
		}
		alreadyExecuted = true;
		try {
			runTestWorkload();
		} catch (Throwable t) {
			caughtThrowables.add(t);
		}
	}

	public List<Throwable> getCaughtThrowables() {
		return caughtThrowables;
	}

	public abstract void runTestWorkload() throws IOException;
}
