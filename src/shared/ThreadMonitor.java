package shared;

import shared.dto.RequestType;
import shared.dto.ResponseType;

public interface ThreadMonitor {

	public void serializeStart();

	public void deserializeEnd();

	/**
	 * Indicates end of serialization and start of network writing
	 */
	public void networkWriteStart();

	/**
	 * Indicates end of network reading and start of deserialization
	 */
	public void networkReadEnd();

	public void setRequestType(RequestType requestType);

	public void setResponseType(ResponseType responseType);
}
