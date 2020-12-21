/* HEADER */
package com.identity4j.connector.script.http;

/*
 * #%L
 * Identity4J Scripted HTTP Connector
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

import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.script.AbstractScriptConnector;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpProviderClient;

public class HttpConnector extends AbstractScriptConnector<HttpConfiguration> {

	private final static Log LOG = LogFactory.getLog(HttpConnector.class);

	private HttpProviderClient client;

	public HttpConnector() {
		super();
	}

	@Override
	public boolean isOpen() {
		return client != null;
	}

	@Override
	protected String getScriptContent() throws IOException {
		return getConfiguration().getScriptContent();
	}

	@Override
	protected void onOpened(ConnectorConfigurationParameters parameters) {
		client = Http.getProvider().getClient(getConfiguration().getUrl(),  
				getConfiguration().getServiceAccountUsername(), 
				getConfiguration().getServiceAccountPassword() == null ? null : getConfiguration().getServiceAccountPassword().toCharArray(),
						getConfiguration().getServiceAccountRealm());
		client.setCertificateRequirements(getConfiguration().getCertificateRequirements());
		getEngine().put("httpClient", new HttpClientWrapper(client, (HttpConfiguration) getConfiguration()));
		getEngine().put("httpProvider", Http.getProvider());
		super.onOpened(parameters);

	}

	@Override
	protected void onClose() {
		try {
			((Invocable) getEngine()).invokeFunction("onClose");
		} catch (ScriptException e) {
			throw new ConnectorException("Failed script execution.", e);
		} catch (NoSuchMethodException e) {
			// Not to worry
		}
		super.onClose();
		disconnect();
	}

	private synchronized void disconnect() {
		if (client != null) {
			LOG.info("Disconnecting");
			client = null;
		}
	}

}