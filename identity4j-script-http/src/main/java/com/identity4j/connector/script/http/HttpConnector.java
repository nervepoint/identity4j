/* HEADER */
package com.identity4j.connector.script.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.script.ScriptConnector;

public class HttpConnector extends ScriptConnector {

	private final static Log LOG = LogFactory.getLog(HttpConnector.class);

	private HttpConfiguration httpConfiguration;
	private HttpClient client;

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

	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) {
		httpConfiguration = (HttpConfiguration) parameters;
		super.onOpen(parameters);
		client = new HttpClient();
		try {
			URL url = new URL(httpConfiguration.getUrl());
			if (httpConfiguration.getServiceAccountUsername().length() > 0) {
				Credentials defaultcreds = new UsernamePasswordCredentials(
						httpConfiguration.getServiceAccountUsername(), httpConfiguration.getServiceAccountPassword());
				String realm = httpConfiguration.getServiceAccountRealm();
				client.getState()
						.setCredentials(new AuthScope(url.getHost(),
								url.getPort() == -1 ? (httpConfiguration.isHTTPS() ? 443 : 80) : url.getPort(),
								realm.length() == 0 ? AuthScope.ANY_REALM : realm), defaultcreds);
			}
			getEngine().put("httpClient", new HttpClientWrapper(client, (HttpConfiguration) getConfiguration()));
		} catch (MalformedURLException mrle) {
			throw new IllegalArgumentException(mrle);
		}

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