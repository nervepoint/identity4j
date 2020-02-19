package com.identity4j.remote.dbus;

import java.io.IOException;

import org.freedesktop.dbus.interfaces.DBusInterface;

public interface ConnectorDBus extends DBusInterface {
	DBusIdentity GetIdentityByName(String name);

	void Close() throws IOException;

	long CountIdentities();

	long CountRoles();

	long Id();

}
