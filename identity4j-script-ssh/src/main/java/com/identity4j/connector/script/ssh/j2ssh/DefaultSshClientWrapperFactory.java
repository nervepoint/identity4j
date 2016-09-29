package com.identity4j.connector.script.ssh.j2ssh;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.script.ssh.SshClientWrapper;
import com.identity4j.connector.script.ssh.SshClientWrapperFactory;
import com.identity4j.connector.script.ssh.SshConfiguration;
import com.identity4j.util.StringUtil;
import com.sshtools.net.SocketWrapper;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.PublicKeyAuthentication;
import com.sshtools.ssh.SshAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshConnector;
import com.sshtools.ssh.SshException;
import com.sshtools.ssh.SshIOException;
import com.sshtools.ssh.SshTransport;
import com.sshtools.ssh.components.SshKeyPair;

public class DefaultSshClientWrapperFactory implements SshClientWrapperFactory {

	private final static Log LOG = LogFactory.getLog(DefaultSshClientWrapperFactory.class);

	public DefaultSshClientWrapperFactory() {
	}

	@Override
	public SshClientWrapper createInstance(SshConfiguration config) {
		
		SshClient client = null;
		try {
			SshConnector con = SshConnector.createInstance();
			LOG.info("Making SSH to " + config.getHost() + ":" + config.getPort() + " for user "
				+ config.getServiceAccountUsername());
			Socket socket = new Socket();
			final SshTransport socketTransport = new SocketWrapper(socket); 
			
			socket.connect(new InetSocketAddress(config.getHost(), config.getPort()), config.getConnectTimeout());

			client = con.connect(socketTransport, config.getServiceAccountUsername(), true);

			InputStream in = config.getServiceAccountPrivateKey();
			if (in != null) {
				try {
					
					try {
						SshPrivateKeyFile pkf = SshPrivateKeyFileFactory.parse(in);
						PublicKeyAuthentication pka = new PublicKeyAuthentication();
						SshKeyPair keyPair;
						if(pkf.isPassphraseProtected()) {
							keyPair = pkf.toKeyPair(config.getServiceAccountPrivateKeyPassphrase());
						} else {
							keyPair = pkf.toKeyPair(null);
						}
						pka.setPrivateKey(keyPair.getPrivateKey());
						pka.setPublicKey(keyPair.getPublicKey());
						checkAuth(client, config, pka);
						return new SshClientWrapperImpl(client, config);
					}
					catch(IOException ex) {
						throw new ConnectorException(ex.getMessage());
					}
				} finally {
					in.close();
				}
			}
			PasswordAuthentication pwd = new PasswordAuthentication();
			pwd.setPassword(StringUtil.nonNull(config.getServiceAccountPassword()));
			checkAuth(client, config, pwd);
			return new SshClientWrapperImpl(client, config);
		} catch (SshException e) {
			disconnect(client);
			throw new ConnectorException("Failed to open SSH connection. ", e);
		} catch (IOException e) {
			disconnect(client);
			throw new ConnectorException("Failed to open SSH connection.", e);
		} catch (ChannelOpenException e) {
			disconnect(client);
			throw new ConnectorException("Failed to open SSH connection.", e);
		} catch (InvalidPassphraseException e) {
			disconnect(client);
			throw new ConnectorException("Failed to open SSH connection.", e);
		}
	}
	
	protected void checkAuth(SshClient client, ConnectorConfigurationParameters parameters, SshAuthentication pwd) throws SshException, SshIOException, ChannelOpenException, IOException {
		if (client.authenticate(pwd) == SshAuthentication.COMPLETE) {
			LOG.info("Authenticated OK");
		} else {
			LOG.info("Failed authentication.");
			disconnect(client);
			throw new ConnectorException("Failed to authenticate SSH connection.");
		}
	}

	private synchronized void disconnect(SshClient client) {
		if (client != null && client.isConnected()) {
			client.disconnect();
			client = null;
		}
	}
	

}
