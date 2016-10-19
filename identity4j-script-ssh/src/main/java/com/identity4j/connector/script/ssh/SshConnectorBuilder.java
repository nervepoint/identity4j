package com.identity4j.connector.script.ssh;

import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.util.MultiMap;

public class SshConnectorBuilder extends ConnectorBuilder {
	
	private SshKeyVerifier verifier;

	public SshKeyVerifier getVerifier() {
		return verifier;
	}

	public SshConnectorBuilder setVerifier(SshKeyVerifier verifier) {
		this.verifier = verifier;
		return this;
	}

	@Override
	public ConnectorConfigurationParameters buildConfiguration(
			MultiMap configurationParameters) throws ConnectorException {
			
		configurationParameters.set(CONFIGURATION_CLASS, SshConfiguration.class.getCanonicalName());
		configurationParameters.set(CONNECTOR_CLASS, SshConnector.class.getCanonicalName());
		SshConfiguration cfg = (SshConfiguration)super.buildConfiguration(configurationParameters);
		if(verifier != null)
			cfg.setVerifier(verifier);
		return cfg;
	}
}
