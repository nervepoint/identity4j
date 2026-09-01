package com.identity4j.remote.client;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.Connector;
import com.identity4j.util.MultiMap;

/**
 * Configuration for the remote HTTP bridge connector.
 */
public class RemoteConnectorConfiguration extends AbstractConnectorConfiguration {

    public static final String REMOTE_CONNECTOR_CLASS = "remote.connectorClass";

    public RemoteConnectorConfiguration(MultiMap configurationParameters) {
        super(configurationParameters);
    }

    @Override
    public String getUsernameHint() {
        return null;
    }

    @Override
    public String getHostnameHint() {
        return null;
    }

    @Override
    public Class<? extends Connector<?>> getConnectorClass() {
        return RemoteConnector.class;
    }

    public String getBaseUri() {
        return getConfigurationParameters().getString(KEY_URI);
    }

    public String getTargetConnectorClass() {
        return getConfigurationParameters().getString(REMOTE_CONNECTOR_CLASS);
    }
}
