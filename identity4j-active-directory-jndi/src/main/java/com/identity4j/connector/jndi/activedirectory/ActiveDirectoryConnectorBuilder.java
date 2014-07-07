package com.identity4j.connector.jndi.activedirectory;

import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.util.MultiMap;

public class ActiveDirectoryConnectorBuilder extends ConnectorBuilder {

	@Override
	public ConnectorConfigurationParameters buildConfiguration(
			MultiMap configurationParameters) throws ConnectorException {
		configurationParameters.set(CONFIGURATION_CLASS, ActiveDirectoryConfiguration.class.getCanonicalName());
		configurationParameters.set(CONNECTOR_CLASS, ActiveDirectoryConnector.class.getCanonicalName());
		return super.buildConfiguration(configurationParameters);
	}
}
