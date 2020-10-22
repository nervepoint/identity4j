package com.identity4j.remote.dbus;

import java.io.IOException;

import com.identity4j.connector.Connector;

public class ConnectorDBusImpl implements ConnectorDBus {

	private Connector<?> connector;
	private long id;

	public ConnectorDBusImpl(long id, Connector<?> connector) {
		this.connector = connector;
		this.id = id;
	}

	@Override
	public long CountIdentities() {
		return this.connector.countIdentities();
	}

	@Override
	public long CountRoles() {
		return this.connector.countRoles();
	}
	
	@Override
	public void Close() throws IOException {
		this.connector.close();
	}
	
	@Override
	public long Id() {
		return id;
	}
	
	@Override
	public boolean isRemote() {
		return true;
	}

	@Override
	public String getObjectPath() {
		return "/Connector/" + id;
	}

	@Override
	public DBusIdentity GetIdentityByName(String name) {
		return new DBusIdentity(this, connector.getIdentityByName(name));
	}
}
