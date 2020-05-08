package com.identity4j.remote.dbus;

import org.freedesktop.dbus.interfaces.DBusInterface;

import com.identity4j.connector.principal.Identity;

public class DBusIdentity implements DBusInterface {
	
	private ConnectorDBus connector;
	private Identity identity;
	private String path;
	private String principalName;

	public DBusIdentity() {
	}
	
	public DBusIdentity(ConnectorDBus connector, Identity identity) {
		path = connector + "/" + identity.getGuid();
	}
	
	public String PrincipalName() {
		return principalName;
	}

	@Override
	public boolean isRemote() {
		return true;
	}

	@Override
	public String getObjectPath() {
		return path;
	}

}
