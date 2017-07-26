package com.identity4j.connector.sap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class SAPDataProviderManager implements DestinationDataProvider {
	private Map<String, Properties> properties = new HashMap<String, Properties>();
	private DestinationDataEventListener listener;

	public void addDestination(String name, Properties properties) {
		this.properties.put(name, properties);
	}

	public void removeDestination(String name) {
		this.properties.remove(name);
	}

	@Override
	public Properties getDestinationProperties(String destination) {
		return this.properties.get(destination);
	}

	@Override
	public void setDestinationDataEventListener(DestinationDataEventListener listener) {
		this.listener = listener;

	}

	@Override
	public boolean supportsEvents() {
		return true;
	}

}
