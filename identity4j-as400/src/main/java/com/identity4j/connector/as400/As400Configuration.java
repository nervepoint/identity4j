/* HEADER */
package com.identity4j.connector.as400;

import com.ibm.as400.access.AS400;
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
     */
    public final AS400 buildConnection() {
        if (StringUtil.isNullOrEmpty(getProxyServer())) {
            return buildConnection(getServiceAccountUsername(), getServiceAccountPassword());
        } else {
            return buildConnection(getServiceAccountUsername(), getServiceAccountPassword(), getProxyServer());
        }
    }

    /**
     * Connect via proxy server
     * 
     * @param username
     * @param password
     * @param proxy
     * @return
     */
    private AS400 buildConnection(String username, String password, String proxy) {
        return new AS400(getControllerHost(), username, password, proxy);
    }

    /**
     * Connect without proxy
     * 
     * @param username
     * @param password
     * @return
     */
    public final AS400 buildConnection(String username, String password) {
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