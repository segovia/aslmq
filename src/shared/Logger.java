package shared;

import shared.dto.ResponseType;

public class Logger {

	public static void info(Object s) {
		System.out.println(s);
	}

	public static void debug(Object s) {
		// System.out.println(s);
	}

	public static void debug(ResponseType type, Exception e) {
		System.err.println("Error type: " + type);
		e.printStackTrace();
	}

	public static boolean isInfo() {
		return true;
	}

	public static void error(ResponseType type, Exception e) {
		System.err.println("Error type: " + type);
		e.printStackTrace();
	}

}
