package shared;

public class NullSafe {
	public static void quietClose(AutoCloseable obj) {
		try {
			if (obj != null) {
				obj.close();
			}
		} catch (Exception e) {
			// shhhh
		}
	}
}
