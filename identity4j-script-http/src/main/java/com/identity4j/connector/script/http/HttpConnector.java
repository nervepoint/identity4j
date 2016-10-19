/* HEADER */
package com.identity4j.connector.script.http;

import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.script.ScriptConnector;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpProviderClient;

public class HttpConnector extends ScriptConnector {

	private final static Log LOG = LogFactory.getLog(HttpConnector.class);

	private HttpConfiguration httpConfiguration;
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
		return httpConfiguration.getScriptContent();
	}
	protected void onOpen(ConnectorConfigurationParameters parameters) {
		httpConfiguration = (HttpConfiguration) parameters;
		super.onOpen(parameters);
	}

	@Override
	protected void onOpened(ConnectorConfigurationParameters parameters) {
		httpConfiguration = (HttpConfiguration) parameters;
		client = Http.getProvider().getClient(httpConfiguration.getUrl(),  
				httpConfiguration.getServiceAccountUsername(), 
				httpConfiguration.getServiceAccountPassword() == null ? null : httpConfiguration.getServiceAccountPassword().toCharArray(),
				httpConfiguration.getServiceAccountRealm());
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