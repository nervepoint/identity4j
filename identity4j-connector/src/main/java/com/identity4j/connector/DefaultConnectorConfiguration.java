package com.identity4j.connector;

import com.identity4j.util.MultiMap;

public class DefaultConnectorConfiguration extends AbstractConnectorConfiguration {

	public DefaultConnectorConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	@Override
	public String getUsernameHint() {
		return null;
	}

	@Override
	public String getHostnameHint() {
		return null;
	}

}
