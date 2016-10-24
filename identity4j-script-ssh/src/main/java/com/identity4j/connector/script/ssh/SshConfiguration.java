/* HEADER */
package com.identity4j.connector.script.ssh;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.identity4j.connector.script.ScriptConfiguration;
import com.identity4j.util.MultiMap;

public class SshConfiguration extends ScriptConfiguration {
	
	public enum AuthenticationMethod {
		passwd,ssh,su
	}

	public static final String SSH_PROXY_SERVER = "ssh.proxyServer";
	public static final String SSH_OS = "ssh.os";
	public static final String SSH_CONNECT_TIMEOUT = "ssh.connectTimeout";
	public static final String SSH_PORT = "ssh.port";
	public static final String SSH_SERVICE_ACCOUNT_PASSWORD = "ssh.serviceAccountPassword";
	public static final String SSH_SERVICE_ACCOUNT_PRIVATE_KEY = "ssh.serviceAccountPrivateKey";
	public static final String SSH_SERVICE_ACCOUNT_PRIVATE_KEY_PASSPHRASE = "ssh.serviceAccountPrivateKeyPassphrase";
	public static final String SSH_SERVICE_ACCOUNT_USERNAME = "ssh.serviceAccountUsername";
	public static final String SSH_USER_AUTHENTICATION_METHOD = "ssh.userAuthenticationMethod";
	public static final String SSH_HOSTNAME = "ssh.hostname";
	public static final String SSH_SUDO_COMMAND = "ssh.sudoCommand";
	public static final String SSH_SUDO_PROMPT = "ssh.sudoPrompt";
	public static final String SSH_USE_KBI_FOR_SSH_AUTHENTICATION = "ssh.useKBIForSSHAuthentication";
	public static final String SSH_AUTHENTICATION_PASSWORD_PATTERN = "ssh.authenticationPasswordPattern";
	public static final String SSH_AUTHENTICATION_NEW_PASSWORD_PATTERN = "ssh.authenticationNewPasswordPattern";

	SshClientWrapperFactory clientFactory;
	SshKeyVerifier verifier;

	/**
	 * @param configurationParameters
	 */
	public SshConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
		clientFactory = SshClientWrapperFactoryHolder.getClientFactory();
	}

	public SshKeyVerifier getVerifier() {
		return verifier;
	}

	public void setVerifier(SshKeyVerifier verifier) {
		this.verifier = verifier;
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
	 * 
	 * 
	 * @return service account username
	 */
	public final String getServiceAccountUsername() {
		return getConfigurationParameters().getStringOrFail(SSH_SERVICE_ACCOUNT_USERNAME);
	}

	/**
	 * Get the authentication method to use. If the sccript supplies an areCredentialsValid
	 * function that will be used instead.
	 * 
	 * @return user authentication method
	 */
	public final AuthenticationMethod getUserAuthenticationMethod() {
		return AuthenticationMethod.valueOf(getConfigurationParameters().getStringOrDefault(SSH_USER_AUTHENTICATION_METHOD, AuthenticationMethod.passwd.name()));
	}

	/**
	 * 
	 * 
	 * @return service account username
	 */
	public final void setServiceAccountUsername(String username) {
		if (username == null)
			getConfigurationParameters().remove(SSH_SERVICE_ACCOUNT_USERNAME);
		else
			getConfigurationParameters().set(SSH_SERVICE_ACCOUNT_USERNAME, username);
	}
	
	public final String getAuthenticationPasswordPattern() {
		return getConfigurationParameters().getStringOrDefault(SSH_AUTHENTICATION_PASSWORD_PATTERN, "password.*:.*");
	}
	
	public final String getAuthenticationNewPasswordPattern() {
		return getConfigurationParameters().getStringOrDefault(SSH_AUTHENTICATION_NEW_PASSWORD_PATTERN, "new password.*:.*");
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
	 * Set The password used for the service account
	 * 
	 * @param service
	 *            account password
	 */
	public final void setServiceAccountPassword(String password) {
		if (password == null)
			getConfigurationParameters().remove(SSH_SERVICE_ACCOUNT_PASSWORD);
		else
			getConfigurationParameters().set(SSH_SERVICE_ACCOUNT_PASSWORD, password);
	}

	/**
	 * The private key file used for the service account
	 * 
	 * @return file containing private key
	 * @throws FileNotFoundException
	 */
	public final InputStream getServiceAccountPrivateKey() throws FileNotFoundException {
		String str = getConfigurationParameters().getStringOrNull(SSH_SERVICE_ACCOUNT_PRIVATE_KEY);
		return StringUtils.isBlank(str) ? null : new ByteArrayInputStream(str.getBytes());
	}

	/**
	 * Set he private key file used for the service account
	 * 
	 * @param key file path containing private key
	 */
	public final void setServiceAccountPrivateKey(String key) {
		if (key == null)
			getConfigurationParameters().remove(SSH_SERVICE_ACCOUNT_PRIVATE_KEY);
		else
			getConfigurationParameters().set(SSH_SERVICE_ACCOUNT_PRIVATE_KEY, key);
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
		return getConfigurationParameters().getStringOrDefault(SSH_SERVICE_ACCOUNT_PRIVATE_KEY_PASSPHRASE, null);
	}

	public boolean isUseKBIForSSHAuthentication() {
		return getConfigurationParameters().getBooleanOrDefault(SSH_USE_KBI_FOR_SSH_AUTHENTICATION, false);
	}

	public void setUseKBIForSSHAuthentication(boolean useKBI) {
		getConfigurationParameters().set(SSH_USE_KBI_FOR_SSH_AUTHENTICATION, String.valueOf(useKBI));
	}

	public void setServiceAccountPrivateKeyPassphrase(String passphrase) {
		if (passphrase == null)
			getConfigurationParameters().remove(SSH_SERVICE_ACCOUNT_PRIVATE_KEY_PASSPHRASE);
		else
			getConfigurationParameters().set(SSH_SERVICE_ACCOUNT_PRIVATE_KEY_PASSPHRASE, passphrase);
	}

	public int getConnectTimeout() {
		return getConfigurationParameters().getIntegerOrDefault(SSH_CONNECT_TIMEOUT, 5000);
	}
}