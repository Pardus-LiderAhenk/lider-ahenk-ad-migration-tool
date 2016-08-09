package tr.org.liderahenk.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.contants.Constants;

public class PropertyReader {

	private static final Logger logger = LoggerFactory.getLogger(PropertyReader.class);

	private static PropertyReader instance = null;
	private static Properties prop = null;

	private PropertyReader() {
	}

	public static PropertyReader getInstance() {
		if (instance == null) {
			instance = new PropertyReader();
			loadProperties();
		}
		return instance;
	}

	private static void loadProperties() {

		logger.info("Trying to load config.properties file.");

		prop = new Properties();
		InputStream inp = null;

		try {
			prop.load(PropertyReader.class.getClassLoader().getResourceAsStream(Constants.FILES.PROPERTIES_FILE));
			logger.info("Properties loaded.");
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			if (inp != null) {
				try {
					inp.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public String get(String key) {
		return prop != null && key != null ? prop.getProperty(key) : null;
	}

	public int getInt(String key) {
		return Integer.parseInt(get(key));
	}

	public double getDouble(String key) {
		return Double.parseDouble(get(key));
	}

	public long getLong(String key) {
		return Long.parseLong(get(key));
	}

	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(get(key));
	}

	public String[] getStringArr(String key) {
		return get(key).split(",");
	}

	public List<String> getStringList(String key) {
		return Arrays.asList(get(key).split(","));
	}

}
