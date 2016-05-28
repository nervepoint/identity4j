/* HEADER */
package com.identity4j.connector.script.ssh;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.identity4j.connector.script.ScriptConfiguration;
import com.identity4j.connector.script.ssh.j2ssh.DefaultSshClientWrapperFactory;
import com.identity4j.util.IOUtil;
import com.identity4j.util.MultiMap;

public class SshConfiguration extends ScriptConfiguration {

	public static final String SSH_PROXY_SERVER = "ssh.proxyServer";
	public static final String SSH_OS = "ssh.os";
	public static final String SSH_PORT = "ssh.port";
	public static final String SSH_SERVICE_ACCOUNT_PASSWORD = "ssh.serviceAccountPassword";
	public static final String SSH_SERVICE_ACCOUNT_PRIVATE_KEY = "ssh.serviceAccountPrivateKey";
	public static final String SSH_SERVICE_ACCOUNT_PRIVATE_KEY_PASSPHRASE = "ssh.serviceAccountPrivateKeyPassphrase";
	public static final String SSH_SERVICE_ACCOUNT_USERNAME = "ssh.serviceAccountUsername";
	public static final String SSH_HOSTNAME = "ssh.hostname";
	public static final String SSH_SUDO_COMMAND = "ssh.sudoCommand";
	public static final String SSH_SUDO_PROMPT = "ssh.sudoPrompt";

	SshClientWrapperFactory clientFactory = new DefaultSshClientWrapperFactory();
	
	/**
	 * @param configurationParameters
	 */
	public SshConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}
	
	
	public SshClientWrapperFactory getClientFactory() {
		return clientFactory;
	}

	/**
	 * <p>
	 * The host name or IP address of the host to connect to. If an IP address
	 * is used this should be in dotted decimal notation. Otherwise the hostname
	 * should be specified in the standard dns format
	 * </p>
	 * <p>
	 * Examples: <code>192.168.1.200</code> or <code>host.directory.com</code>
	 * </p>
	 * 
	 * @return controller host
	 */
	public final String getHost() {
		return getConfigurationParameters().getStringOrDefault(SSH_HOSTNAME, "localhost");
	}

	/**
	 * The connector performs all operations on the AS400 using this account.
	 * 
	 * @return service account username
	 */
	public final String getServiceAccountUsername() {
		return getConfigurationParameters().getStringOrFail(SSH_SERVICE_ACCOUNT_USERNAME);
	}

	/**
	 * The password used for the service account
	 * 
	 * @return service account password
	 */
	public final String getServiceAccountPassword() {
		return getConfigurationParameters().getStringOrDefault(SSH_SERVICE_ACCOUNT_PASSWORD, "");
	}

	/**
	 * The private key file used for the service account
	 * 
	 * @return file containing private key
	 * @throws FileNotFoundException
	 */
	public final InputStream getServiceAccountPrivateKey() throws FileNotFoundException {
		String str = getConfigurationParameters().getStringOrNull(SSH_SERVICE_ACCOUNT_PRIVATE_KEY);
		return str == null ? null : new ByteArrayInputStream(str.getBytes());
	}

	/**
	 * Get proxy server
	 * 
	 * @return
	 */
	public final String getProxyServer() {
		return getConfigurationParameters().getString(SSH_PROXY_SERVER);
	}

	/**
	 * Get the port on which the SSH service is running.
	 * 
	 * @return SSH port
	 */
	public int getPort() {
		return getConfigurationParameters().getIntegerOrDefault(SSH_PORT, 22);
	}

	@Override
	public String getUsernameHint() {
		return getServiceAccountUsername();
	}

	@Override
	public String getHostnameHint() {
		return getHost();
	}

	public String getSudoCommand() {
		return getConfigurationParameters().getStringOrDefault(SSH_SUDO_COMMAND, "sudo -k");
	}

	public String getSudoPrompt() {
		return getConfigurationParameters().getStringOrDefault(SSH_SUDO_PROMPT, "[sudo] password for ${username}");
	}


	public String getServiceAccountPrivateKeyPassphrase() {
		return  getConfigurationParameters().getStringOrDefault(SSH_SERVICE_ACCOUNT_PRIVATE_KEY_PASSPHRASE, null);
	}
}