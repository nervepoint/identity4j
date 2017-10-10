/* HEADER */
package com.identity4j.connector.as400;

/*
 * #%L
 * Identity4J AS400
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

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

public class As400Configuration extends AbstractConnectorConfiguration {

    public As400Configuration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	/**
     * <p>
     * The host name or IP address of the AS400 to connect to. If an IP address
     * is used this should be in dotted decimal notation. Otherwise the hostname
     * should be specified in the standard dns format
     * </p>
     * <p>
     * Examples: <code>192.168.1.200</code> or <code>host.directory.com</code>
     * </p>
     * 
     * @return controller host
     */
    public final String getControllerHost() {
        return configurationParameters.getStringOrDefault("as400.hostname", "localhost");
    }

    /**
     * The connector performs all operations on the AS400 using this account.
     * 
     * @return service account username
     */
    public final String getServiceAccountUsername() {
        return configurationParameters.getStringOrFail("as400.serviceAccountUsername");
    }

    /**
     * The password used for the service account
     * 
     * @return service account password
     */
    public final String getServiceAccountPassword() {
        return configurationParameters.getStringOrFail("as400.serviceAccountPassword");
    }

    /**
     * Get proxy server
     * 
     * @return
     */
    public final String getProxyServer() {
        return configurationParameters.getString("as400.proxyServer");
    }

    /**
     * Create connection to remote system.
     * 
     * @return
     * @throws IOException 
     * @throws AS400SecurityException 
     */
    public final AS400 buildConnection() throws IOException {
        try {
			if (StringUtil.isNullOrEmpty(getProxyServer())) {
			    return buildConnection(getServiceAccountUsername(), getServiceAccountPassword());
			} else {
			    return buildConnection(getServiceAccountUsername(), getServiceAccountPassword(), getProxyServer());
			}
		} catch (AS400SecurityException e) {
			throw new IOException(e.getMessage(), e);
		}
    }

    /**
     * Connect via proxy server
     * 
     * @param username
     * @param password
     * @param proxy
     * @return
     * @throws IOException 
     * @throws AS400SecurityException 
     */
    private AS400 buildConnection(String username, String password, String proxy) throws AS400SecurityException, IOException {
       return new AS400(getControllerHost(), username, password, proxy);
    }

    /**
     * Connect without proxy
     * 
     * @param username
     * @param password
     * @return
     * @throws IOException 
     * @throws AS400SecurityException 
     */
    public final AS400 buildConnection(String username, String password) throws AS400SecurityException, IOException {
       return new AS400(getControllerHost(), username, password);
    }

	@Override
	public String getUsernameHint() {
		return getServiceAccountUsername();
	}

	@Override
	public String getHostnameHint() {
		return getControllerHost();
	}
}