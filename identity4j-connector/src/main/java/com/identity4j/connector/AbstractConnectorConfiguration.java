/* HEADER */
package com.identity4j.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import com.identity4j.util.MultiMap;

public abstract class AbstractConnectorConfiguration implements ConnectorConfigurationParameters {
	public static final String KEY_URI = "uri";

	protected final MultiMap configurationParameters;

	private Collection<String> identityAttributesToRetrieve;

	@Override
	public InputStream getAdditionalIdentityAttributes() throws IOException {
		return null;
	}

	@Override
	public ResourceBundle getAdditionalResources(Locale locale) throws IOException {
		return null;
	}

	public AbstractConnectorConfiguration(MultiMap configurationParameters) {
		this.configurationParameters = configurationParameters;
	}

	public void setIdentityAttributesToRetrieve(Collection<String> identityAttributesToRetrieve) {
		this.identityAttributesToRetrieve = identityAttributesToRetrieve;
	}

	@Override
	public Collection<String> getIdentityAttributesToRetrieve() {
		return identityAttributesToRetrieve;
	}

	@Override
	public final MultiMap getConfigurationParameters() {
		return configurationParameters;
	}

}