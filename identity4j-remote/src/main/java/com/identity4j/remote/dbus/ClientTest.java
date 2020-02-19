package com.identity4j.remote.dbus;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnection.DBusBusType;
import org.freedesktop.dbus.exceptions.DBusException;

import com.identity4j.connector.Connector;
import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.unix.UnixConfiguration;
import com.identity4j.remote.RemoteConnectorConfiguration;

public class ClientTest {

	private DBusConnection conn;

	public ClientTest() throws DBusException {
		conn = DBusConnection.getConnection(DBusBusType.SESSION);
	}

	public static void main(String[] args) throws Exception {
		ClientTest main = new ClientTest();

		// We want unix connector on the remote side
		UnixConfiguration uc = new UnixConfiguration();

		// Wrap it in remote connector
		RemoteConnectorConfiguration rc = new RemoteConnectorConfiguration(uc, main.conn);

		// Build connector
		try (Connector<RemoteConnectorConfiguration> con = new ConnectorBuilder().buildConnector(rc)) {

			// Open
			con.open(rc);

			// Query
			System.out.println("Identities: " + con.countIdentities());
			System.out.println("Roles: " + con.countRoles());
			System.out.println("Identity: " + con.getIdentityByName(System.getProperty("user.name")));
		}
	}
}
