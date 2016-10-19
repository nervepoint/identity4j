/* HEADER */
package com.identity4j.connector.script.ssh;

import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.script.ScriptConnector;
import com.identity4j.connector.script.ssh.j2ssh.DefaultSshClientWrapperFactory;

public class SshConnector extends ScriptConnector {

	private final static Log LOG = LogFactory.getLog(SshConnector.class);

	private SshConfiguration sshConfiguration;
	private SshClientWrapper client;

	public static final String FULL_NAME = "fullName";
	public static final String HOME_DIR = "home";
	public static final String SHELL = "shell";
	public static final String BUILDING = "building";
	public static final String OFFICE_NUMBER = "officeNumber";
	public static final String OTHER_CONTACT = "otherContact";
	
	public SshConnector() {
		super();
	}

	@Override
	public boolean isOpen() {
		return client != null && client.isConnected() && client.isAuthenticated();
	}

	@Override
	protected String getScriptContent() throws IOException {	
		return sshConfiguration.getScriptContent();
	}
	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) {
		sshConfiguration = (SshConfiguration) parameters;
		super.onOpen(parameters);
	}

	@Override
	protected void onOpened(ConnectorConfigurationParameters parameters) {
		client = sshConfiguration.getClientFactory().createInstance(sshConfiguration);
		getEngine().put("sshClient",client);
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
		if (client != null && client.isConnected()) {
			LOG.info("Disconnecting");
			client.disconnect();
			client = null;
		}
	}
}