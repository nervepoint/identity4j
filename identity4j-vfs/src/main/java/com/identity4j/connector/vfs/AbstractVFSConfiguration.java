/* HEADER */
package com.identity4j.connector.vfs;

import java.net.URI;
import java.net.URISyntaxException;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.util.MultiMap;

public abstract class AbstractVFSConfiguration extends AbstractConnectorConfiguration {

	public static final String KEY_URI = "uri";
    public AbstractVFSConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

    public abstract String getUri();

	@Override
	public String getUsernameHint() {
		try {
			URI uri = new URI(getUri());
			return uri.getUserInfo();
		} catch (URISyntaxException e) {
			return null;
		}
	}

	@Override
	public String getHostnameHint() {
		try {
			URI uri = new URI(getUri());
			return uri.getHost();
		} catch (URISyntaxException e) {
			return null;
		}
	}
}