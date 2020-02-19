package com.identity4j.remote.dbus;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;

public interface ConnectorBuilderDBus extends DBusInterface {
	
	List<String> Connectors();
	
	void Destroy(long uuid) throws IOException;
	
	long Create(Map<String, String> values) throws DBusException;
}
