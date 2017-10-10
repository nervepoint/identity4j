package com.identity4j.connector.script.ssh;

/*
 * #%L
 * Identity4J Scripted SSH Connector
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
