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
import java.util.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.script.ssh.SshClientWrapper;
import com.identity4j.connector.script.ssh.SshClientWrapperFactory;
import com.identity4j.connector.script.ssh.SshConfiguration;
import com.identity4j.util.StringUtil;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

public class DefaultSshClientWrapperFactory implements SshClientWrapperFactory {

	private final static Log LOG = LogFactory.getLog(DefaultSshClientWrapperFactory.class);

	public DefaultSshClientWrapperFactory() {
	}

	@Override
	public SshClientWrapper createInstance(final SshConfiguration config) {
		SshClient client = null;
		try {
			net.sf.sshapi.SshConfiguration sshapiConfiguration = new net.sf.sshapi.SshConfiguration();

			if (config.getVerifier() != null)
				sshapiConfiguration.setHostKeyValidator(new SshHostKeyValidator() {
					@Override
					public int verifyHost(SshHostKey hostKey) throws SshException {
						boolean ok = config.getVerifier().verifyKey(hostKey.getHost(), config.getPort(), hostKey.getType(),
								hostKey.getBits(), hostKey.getKey(), hostKey.getFingerprint(), Base64.getEncoder().encode(hostKey.getKey()));
						return ok ? SshHostKeyValidator.STATUS_HOST_KEY_VALID : SshHostKeyValidator.STATUS_HOST_KEY_UNKNOWN;
					}
				});
			
			LOG.info("Making SSH connection to " + config.getHost() + ":" + config.getPort() + " for user "
					+ config.getServiceAccountUsername());
			
			client = sshapiConfiguration.createClient();
			if(client.getProvider().getCapabilities().contains(Capability.IO_TIMEOUTS))
				client.setTimeout(config.getConnectTimeout());
			client.connect(config.getServiceAccountUsername(), config.getHost(), config.getPort());

			InputStream in = config.getServiceAccountPrivateKey();
			if (in != null) {
				try {

					try {
						SshPublicKeyAuthenticator pkf = new DefaultPublicKeyAuthenticator(new SimplePasswordAuthenticator(config.getServiceAccountPassword()), in);
						checkAuth(client, config, pkf);
						return new SshClientWrapperImpl(client, config);
					} catch (IOException ex) {
						throw new ConnectorException(ex.getMessage());
					}
				} finally {
					in.close();
				}
			}
			SshPasswordAuthenticator pwd = new SimplePasswordAuthenticator(StringUtil.nonNull(config.getServiceAccountPassword()));
			checkAuth(client, config, pwd);
			return new SshClientWrapperImpl(client, config);
		} catch (SshException e) {
			disconnect(client);
			throw new ConnectorException("Failed to open SSH connection. ", e);
		} catch (IOException e) {
			disconnect(client);
			throw new ConnectorException("Failed to open SSH connection.", e);
		} 
	}

	protected void checkAuth(SshClient client, ConnectorConfigurationParameters parameters, SshAuthenticator pwd)
			throws SshException, IOException {
		if (client.authenticate(pwd)) {
			LOG.info("Authenticated OK");
		} else {
			LOG.info("Failed authentication.");
			disconnect(client);
			throw new ConnectorException("Failed to authenticate SSH connection.");
		}
	}

	private synchronized void disconnect(SshClient client) {
		if (client != null && client.isConnected()) {
			try {
				client.close();
			} catch (IOException e) {
			}
			client = null;
		}
	}

}
