/* HEADER */
package com.identity4j.connector.script.http;

import java.net.URL;

import com.identity4j.connector.script.ScriptConfiguration;
import com.identity4j.util.MultiMap;

public class HttpConfiguration extends ScriptConfiguration {

	public static final String HTTP_PROXY_SERVER = "http.proxyServer";
	public static final String HTTP_URL = "http.url";
	public static final String HTTP_SERVICE_ACCOUNT_PASSWORD = "http.serviceAccountPassword";
	public static final String HTTP_SERVICE_ACCOUNT_USERNAME = "http.serviceAccountUsername";
	public static final String HTTP_SERVICE_ACCOUNT_REALM = "http.serviceAccountRealm";

	/**
	 * @param configurationParameters
	 */
	public HttpConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	/**
	 * <p>
	 * The URL to connect to
	 * </p>
	 * <p>
	 * Examples: <code>http://192.168.1.200</code> or
	 * <code>http://host.directory.com/some/path</code>
	 * </p>
	 * 
	 * @return controller host
	 */
	public final String getUrl() {
		return getConfigurationParameters().getStringOrDefault(HTTP_URL, "http://localhost");
	}

	public final String getServiceAccountUsername() {
		return getConfigurationParameters().getStringOrFail(HTTP_SERVICE_ACCOUNT_USERNAME);
	}

	public final String getServiceAccountRealm() {
		return getConfigurationParameters().getStringOrDefault(HTTP_SERVICE_ACCOUNT_REALM, "");
	}

	public final String getServiceAccountPassword() {
		return getConfigurationParameters().getStringOrDefault(HTTP_SERVICE_ACCOUNT_PASSWORD, "");
	}

	public final String getProxyServer() {
		return getConfigurationParameters().getString(HTTP_PROXY_SERVER);
	}
	
	public boolean isHTTPS() {
		try {
			URL url = new URL(getUrl());
			return url.getProtocol().equalsIgnoreCase("https");
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String getUsernameHint() {
		return getServiceAccountUsername();
	}

	@Override
	public String getHostnameHint() {
		try {
			URL url = new URL(getUrl());
			return url.getHost();
		} catch (Exception e) {
			return "unknown";
		}
	}

}