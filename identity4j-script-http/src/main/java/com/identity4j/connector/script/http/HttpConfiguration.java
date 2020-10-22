/* HEADER */
package com.identity4j.connector.script.http;

/*
 * #%L
 * Identity4J Scripted HTTP Connector
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

import java.net.URL;
import com.identity4j.connector.Connector;
import com.identity4j.connector.script.AbstractScriptConfiguration;
import com.identity4j.util.MultiMap;

public class HttpConfiguration extends AbstractScriptConfiguration {

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

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return HttpConnector.class;
	}
}