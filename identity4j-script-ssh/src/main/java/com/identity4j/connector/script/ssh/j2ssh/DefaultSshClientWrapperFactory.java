package com.identity4j.connector.script.ssh.j2ssh;

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
import com.sshtools.publickey.SshPublicKeyFile;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.HostKeyVerification;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.PublicKeyAuthentication;
import com.sshtools.ssh.SshAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshConnector;
import com.sshtools.ssh.SshException;
import com.sshtools.ssh.SshIOException;
import com.sshtools.ssh.SshTransport;
import com.sshtools.ssh.components.SshKeyPair;
import com.sshtools.ssh.components.SshPublicKey;

public class DefaultSshClientWrapperFactory implements SshClientWrapperFactory {

	private final static Log LOG = LogFactory.getLog(DefaultSshClientWrapperFactory.class);

	public DefaultSshClientWrapperFactory() {
	}

	@Override
	public SshClientWrapper createInstance(final SshConfiguration config) {

		SshClient client = null;
		try {
			SshConnector con = SshConnector.createInstance();
			if (config.getVerifier() != null)
				con.getContext().setHostKeyVerification(new HostKeyVerification() {

					@Override
					public boolean verifyHost(String host, SshPublicKey pk) throws SshException {
						try {
							SshPublicKeyFile pkf = SshPublicKeyFileFactory.create(pk, "IdentityJ",
									SshPublicKeyFileFactory.OPENSSH_FORMAT);
							return config.getVerifier().verifyKey(host, config.getPort(), pk.getAlgorithm(),
									pk.getBitLength(), pk.getEncoded(), pk.getFingerprint(), pkf.getFormattedKey());
						} catch (IOException ioe) {
							throw new SshException(ioe);
						}
					}
				});
			LOG.info("Making SSH connection to " + config.getHost() + ":" + config.getPort() + " for user "
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
						if (pkf.isPassphraseProtected()) {
							keyPair = pkf.toKeyPair(config.getServiceAccountPrivateKeyPassphrase());
						} else {
							keyPair = pkf.toKeyPair(null);
						}
						pka.setPrivateKey(keyPair.getPrivateKey());
						pka.setPublicKey(keyPair.getPublicKey());
						checkAuth(client, config, pka);
						return new SshClientWrapperImpl(client, config);
					} catch (IOException ex) {
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

	protected void checkAuth(SshClient client, ConnectorConfigurationParameters parameters, SshAuthentication pwd)
			throws SshException, SshIOException, ChannelOpenException, IOException {
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
