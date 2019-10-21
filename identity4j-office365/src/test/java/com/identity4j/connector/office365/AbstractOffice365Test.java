package com.identity4j.connector.office365;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.identity4j.util.MultiMap;

public class AbstractOffice365Test {
	protected static MultiMap configurationParameters;

	static {
		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));
		configurationParameters = loadConfigurationParameters("/office365-connector.properties");
	}

	private static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = Office365ConnectorTest.class.getResourceAsStream(propertiesFile);
			if (resourceAsStream == null) {
				throw new FileNotFoundException(
						"Properties resource " + propertiesFile + " not found. Check it is on your classpath");
			}
			Properties properties = new Properties();
			properties.load(resourceAsStream);
			return MultiMap.toMultiMap(properties);
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to load configuration parameters", ioe);
		}
	}
}
