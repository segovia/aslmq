package shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
	public static Properties load(String propertiesFile) {
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(propertiesFile);
			prop.load(in);
			in.close();
			return prop;
		} catch (IOException e) {
			throw new RuntimeException("Unable to load properties file: " + propertiesFile, e);
		}
	}
}
