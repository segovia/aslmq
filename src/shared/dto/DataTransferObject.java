package shared.dto;

public abstract class DataTransferObject {

	public final static String NEWLINE_ESCAPE = "<<[nl]>>";
	public final static String PIPE_ESCAPE = "<<[pi]>>";

	public abstract String serialize();

	protected static String escape(String s) {
		String cur = s.replace("|", PIPE_ESCAPE);
		cur = cur.replace("\n", NEWLINE_ESCAPE);
		return cur;
	}

	protected static String unescape(String s) {
		String cur = s.replace(PIPE_ESCAPE, "|");
		cur = cur.replace(NEWLINE_ESCAPE, "\n");
		return cur;
	}
}
