/* HEADER */
package com.identity4j.connector.script.ssh;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.script.AbstractScriptConnector;
import com.identity4j.util.MultiMap;
import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;
import com.identity4j.util.expect.ExpectTimeoutException;
import com.identity4j.util.unix.UnixUtils;

public class SshConnector extends AbstractScriptConnector<SshConfiguration> {

	private final static Log LOG = LogFactory.getLog(SshConnector.class);

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
		return getConfiguration().getScriptContent();
	}

	@Override
	protected void onOpened(ConnectorConfigurationParameters parameters) {
		client = getConfiguration().getClientFactory().createInstance(getConfiguration());
		getEngine().put("sshFactory", getConfiguration().getClientFactory());
		getEngine().put("sshClient", client);
		super.onOpened(parameters);
	}

	@Override
	protected boolean defaultAreCredentialsValid(Identity identity, char[] password) throws IOException {
		switch (getConfiguration().getUserAuthenticationMethod()) {
		case ssh:
			return checkCredentialsUsingSSH(identity, password);
		case su:
			return checkCredentialsUsingCommand(identity, password);
		default:
			return checkCredentialsUsingPasswdFiles(identity, password);
		}
	}

	protected boolean checkCredentialsUsingPasswdFiles(Identity identity, char[] password) {
		/* Note this method does not currently support password expiry */
		
		String hash = null;

		/* Do we have a shadow passwd file? */
		try {
			InputStream in = client.downloadFile("/etc/shadow");
			hash = getHashFromPasswdStream(identity, in);
		} catch (IOException e) {
			// No shadow file, look at passwd
			try {
				InputStream in = client.downloadFile("/etc/passwd");
				hash = getHashFromPasswdStream(identity, in);
			} catch (IOException e2) {
				LOG.warn(String.format("No accessable /etc/shadow or /etc/passwd files. Authentication for %s failed.",
						identity.getPrincipalName()));
			}
		}

		if (hash == null) {
			// Not found
			return false;
		}

		// Find the encoder for the hash and match it
		try {
			Encoder enc = DefaultEncoderManager.getInstance().getEncoderForEncodedData(hash.getBytes("UTF-8"), "UTF-8");
			if (enc == null) {
				// Unknown encoding
				LOG.warn(String.format("Authentication for %s failed because the password appears to an unknown hash.",
						identity.getPrincipalName()));
				return false;
			}

			return enc.match(hash.getBytes("UTF-8"), new String(password).getBytes("UTF-8"), null, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

	private String getHashFromPasswdStream(Identity identity, InputStream in) throws IOException {
		String hash = null;
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try {
			while ((line = r.readLine()) != null) {
				line = line.trim();
				if (line.startsWith(identity.getPrincipalName() + ":")) {
					String[] l = line.split(":");
					if (l.length > 1) {
						hash = l[1];
						break;
					}
				}
			}
		} finally {
			in.close();
		}
		return hash;
	}

	protected boolean checkCredentialsUsingCommand(Identity identity, char[] password) throws IOException {
		try {
			SshCommand command = client.sudoCommand(
					String.format("su '%1$s' -c 'su \"%2$s\" -c echo OK'", UnixUtils.escapeSingle(identity.getPrincipalName()), UnixUtils.escapeDouble(identity.getPrincipalName())));
			
			/* We will get either "You are required to change your password immediately .." OR "Password: ". So, we
			 * match on 'assword' first
			 */

			boolean needsChange = false;
            if(command.expect("assword", false, 10000)) {
            	/* Once that matches, see if the next character is a space or ':', this will determine
            	 * if we will be requiring a password change
            	 */
            	needsChange = (char)command.read() != ':';
            	
            	if(needsChange) {
            		// Read the trailing text up to the "Current password:' prompt
                    if(command.expect("Current password: ", false, 10000)) {
                    	command.typeAndReturn(new String(password));
                    	if(command.expect("New password: ", false, 10000)) {
                    		/* We got this far, so the current password is OK. However, we cannot
                    		 * continue the password change chat here as the framework does not support
                    		 * such interactivity. Instead, throw the exception and assume the changePassword (or setPassword)) 
                    		 * will do the actual change and prevent this message on the next authentication.
                    		 * 
                    		 * We can't wait for exit in this case so just destroy the command 
                    		 * 
                    		 */
                    		command.ctrlD();
                    		command.drainAndWaitForExit();
                    		
                    		throw new PasswordChangeRequiredException();
                    	}
                    	else
                    		command.drainAndWaitForExit();
                    }
            	}
            	else {
                	command.typeAndReturn(new String(password));
                	if(command.drainAndWaitForExit() == 0)
                		return true;
            	}
            }
            else {
            	LOG.warn(String.format("Got unexpected output from credentials validating command while authenticating %s.", identity.getPrincipalName()));
            	return false;
            }

		} catch (ExpectTimeoutException ete) {
			throw new IOException(ete);
		}
		return false;
	}

	protected boolean checkCredentialsUsingSSH(Identity identity, char[] password) {

		SshConfiguration config = new SshConfiguration(new MultiMap(getConfiguration().getConfigurationParameters()));
		config.setVerifier(getConfiguration().getVerifier());
		config.setUseKBIForSSHAuthentication(true);
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