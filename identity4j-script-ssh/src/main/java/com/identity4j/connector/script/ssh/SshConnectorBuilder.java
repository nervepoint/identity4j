package com.identity4j.connector.script.ssh;

import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.util.MultiMap;

public class SshConnectorBuilder extends ConnectorBuilder {

	@Override
	public ConnectorConfigurationParameters buildConfiguration(
			MultiMap configurationParameters) throws ConnectorException {
		configurationParameters.set(CONFIGURATION_CLASS, SshConfiguration.class.getCanonicalName());
		configurationParameters.set(CONNECTOR_CLASS, SshConnector.class.getCanonicalName());
		return super.buildConfiguration(configurationParameters);
	}
}
