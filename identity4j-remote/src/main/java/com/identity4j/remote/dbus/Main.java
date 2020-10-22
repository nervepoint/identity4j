package com.identity4j.remote.dbus;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnection.DBusBusType;
import org.freedesktop.dbus.exceptions.DBusException;

import com.identity4j.util.crypt.nss.DefaultNssTokenDatabase;

public class Main {

	private DBusConnection conn;

	public Main() throws DBusException {
		conn = DBusConnection.getConnection(DBusBusType.SESSION);
		conn.requestBusName(ConnectorBuilderDBus.class.getPackage().getName());
		conn.exportObject("/ConnectorBuilder", new ConnectorBuilderDBusImpl(conn));
	}

	public static void main(String[] args) throws Exception {
		new DefaultNssTokenDatabase();
		Main main = new Main();
		Thread.sleep(Long.MAX_VALUE);
	}
}
