/* HEADER */
package com.identity4j.connector.script.ssh;

import java.io.IOException;
import java.io.InputStream;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.script.ScriptConnector;
import com.identity4j.util.StringUtil;
import com.sshtools.net.SocketTransport;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.HostKeyVerification;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.PublicKeyAuthentication;
import com.sshtools.ssh.SshAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshException;
import com.sshtools.ssh.SshIOException;
import com.sshtools.ssh.components.SshKeyPair;
import com.sshtools.ssh.components.SshPublicKey;

public class SshConnector extends ScriptConnector {

	private final static Log LOG = LogFactory.getLog(SshConnector.class);

	private SshConfiguration sshConfiguration;
	private SshClient client;
	private com.sshtools.ssh.SshConnector sshProtocolConnector;

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
		try {
			sshProtocolConnector = com.sshtools.ssh.SshConnector.createInstance();
			LOG.info("Making SSH to " + sshConfiguration.getHost() + ":" + sshConfiguration.getPort() + " for user "
				+ sshConfiguration.getServiceAccountUsername());
			final SocketTransport socketTransport = new SocketTransport(sshConfiguration.getHost(), sshConfiguration.getPort());

			client = sshProtocolConnector.connect(socketTransport, sshConfiguration.getServiceAccountUsername(), true);

			InputStream in = sshConfiguration.getServiceAccountPrivateKey();
			if (in != null) {
				try {
					
					try {
						SshPrivateKeyFile pkf = SshPrivateKeyFileFactory.parse(in);
						PublicKeyAuthentication pka = new PublicKeyAuthentication();
						SshKeyPair keyPair = pkf.toKeyPair(null);
						pka.setPrivateKey(keyPair.getPrivateKey());
						pka.setPublicKey(keyPair.getPublicKey());
						doAuth(parameters, pka);
						return;
					}
					catch(IOException ex) {
						// Maverick throws an IOException if it cannot parse a key
						throw new ConnectorException(ex.getMessage());
					}
					catch(NullPointerException npe) {
						// TODO bit crap, can maverick not handle this better?
						// LDP Not sure what or why this is here??? why is NPE being 
						// caught, what did maverick do?
						throw new ConnectorException("Could not parse private key.");
					}
				} finally {
					in.close();
				}
			}
			PasswordAuthentication pwd = new PasswordAuthentication();
			pwd.setPassword(StringUtil.nonNull(sshConfiguration.getServiceAccountPassword()));
			doAuth(parameters, pwd);
		} catch (SshException e) {
			disconnect();
			throw new ConnectorException("Failed to open SSH connection. ", e);
		} catch (IOException e) {
			disconnect();
			throw new ConnectorException("Failed to open SSH connection.", e);
		} catch (ChannelOpenException e) {
			disconnect();
			throw new ConnectorException("Failed to open SSH connection.", e);
		} catch (InvalidPassphraseException e) {
			disconnect();
			throw new ConnectorException("Failed to open SSH connection.", e);
		}
	}

	protected void doAuth(ConnectorConfigurationParameters parameters, SshAuthentication pwd) throws SshException, SshIOException,
			ChannelOpenException, IOException {
		if (client.authenticate(pwd) == SshAuthentication.COMPLETE) {
			LOG.info("Authenticated OK");

			authenticated();

			try {
				((Invocable) getEngine()).invokeFunction("onOpen", parameters);
			} catch (ScriptException e) {
				throw new ConnectorException("Failed script execution.", e);
			} catch (NoSuchMethodException e) {
				// Not to worry
			}
		} else {
			LOG.info("Failed authentication.");
			disconnect();
			throw new ConnectorException("Failed to authenticate SSH connection.");
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

	private void authenticated() throws SshException, SshIOException, ChannelOpenException, IOException {
		getEngine().put("sshClient", new SshClientWrapper(client, (SshConfiguration) getConfiguration()));
		getEngine().put("sshProtocolConnector", sshProtocolConnector);
	}

	class DumbHostKeyVerification implements HostKeyVerification {

		public boolean verifyHost(String arg0, SshPublicKey arg1) throws SshException {
			// TODO implement
			return true;
		}

	}
}