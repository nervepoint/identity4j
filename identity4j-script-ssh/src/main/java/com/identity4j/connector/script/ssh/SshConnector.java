/* HEADER */
package com.identity4j.connector.script.ssh;

import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.script.ScriptConnector;
import com.identity4j.util.MultiMap;

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
		getEngine().put("sshFactory", sshConfiguration.getClientFactory());
		getEngine().put("sshClient", client);
		super.onOpened(parameters);
	}

	@Override
	protected boolean defaultAreCredentialsValid(Identity identity, char[] password) {

		/*
		 * TODO need to do this instead to get password changes. This has been
		 * moved out of all the scripts rather than fix (extend) the SSH
		 * abstraction to allow new authentications to be created. The number of
		 * scripts is growing fast too and this avoids a bit of duplication of a
		 * particularly complex bit of the script API.
		 * 
		 * Not entirely sure if this is the RIGHT choice, it's just the easier
		 * for now
		 */

		/*
		 * 
		 * var transport = new SocketTransport(config.getHost(),
		 * config.getPort()); var clientTest =
		 * sshProtocolConnector.connect(transport, identity .getPrincipalName(),
		 * true); try { var kbi = new SshKBIHandler(password);
		 * kbi.setPasswordPattern(SSH_AUTHENTICATION_PASSWORD_PATTERN);
		 * kbi.setNewPasswordPattern(SSH_AUTHENTICATION_NEW_PASSWORD_PATTERN);
		 * 
		 * var pwd = new KBIAuthentication(); pwd.setKBIRequestHandler(kbi); var
		 * result = clientTest.authenticate(pwd);
		 * if(kbi.isRequiresPasswordChange()) { // We return an exception
		 * instead of throwing it because of a JDK6 bug. return new
		 * PasswordChangeRequiredException(); } if (result ==
		 * SshAuthentication.COMPLETE) { return true; } else { log.info(
		 * "SSH authentication returned : " + result); return false; } } finally
		 * { clientTest.disconnect(); }
		 */

		SshConfiguration config = new SshConfiguration(new MultiMap(sshConfiguration.getConfigurationParameters()));
		config.setVerifier(sshConfiguration.getVerifier());
		config.setServiceAccountUsername(identity.getPrincipalName());
		config.setServiceAccountPassword(new String(password));
		config.setServiceAccountPrivateKey(null);
		config.setServiceAccountPrivateKeyPassphrase(null);
		try {
			SshClientWrapper clientTest = config.getClientFactory().createInstance(config);
			clientTest.disconnect();
			return true;
		} catch (ConnectorException ce) {
			if (ce.getMessage().contains("Failed to authenticate")) {
				return false;
			}
			throw ce;
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
		if (client != null && client.isConnected()) {
			LOG.info("Disconnecting");
			client.disconnect();
			client = null;
		}
	}
}