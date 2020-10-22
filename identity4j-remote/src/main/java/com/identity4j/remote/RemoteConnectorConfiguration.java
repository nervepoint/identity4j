package com.identity4j.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import org.freedesktop.dbus.connections.impl.DBusConnection;

import com.identity4j.connector.Connector;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.util.MultiMap;

public class RemoteConnectorConfiguration implements ConnectorConfigurationParameters {
	
	private ConnectorConfigurationParameters delegate;
	private DBusConnection dbus;

	public RemoteConnectorConfiguration(ConnectorConfigurationParameters delegate, DBusConnection dbus) {
		this.delegate = delegate;
		this.dbus = dbus;
	}
	
	public ConnectorConfigurationParameters getDelegate() {
		return delegate;
	}
	
	public DBusConnection getDBus() {
		return dbus;
	}

	@Override
	public InputStream getAdditionalIdentityAttributes() throws IOException {
		return delegate.getAdditionalIdentityAttributes();
	}

	@Override
	public ResourceBundle getAdditionalResources(Locale locale) throws IOException {
		return delegate.getAdditionalResources(locale);
	}

	@Override
	public String getUsernameHint() {
		return delegate.getUsernameHint();
	}

	@Override
	public String getHostnameHint() {
		return delegate.getHostnameHint();
	}

	@Override
	public MultiMap getConfigurationParameters() {
		// TODO merge in remote host name / other parms required for remote location/connection
		return delegate.getConfigurationParameters();
	}

	@Override
	public Collection<String> getIdentityAttributesToRetrieve() {
		return delegate.getIdentityAttributesToRetrieve();
	}

	@Override
	public void setIdentityAttributesToRetrieve(Collection<String> identityAttributesToRetrieve) {
		delegate.setIdentityAttributesToRetrieve(identityAttributesToRetrieve);
	}

	@Override
	public String getProvisionAttributeForPrincipalName() {
		return delegate.getProvisionAttributeForPrincipalName();
	}

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return RemoteConnector.class;
	}

}
