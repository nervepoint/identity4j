package com.identity4j.remote.dbus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import com.identity4j.connector.Connector;
import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.util.MultiMap;

public class ConnectorBuilderDBusImpl implements ConnectorBuilderDBus {
	final static Log LOG = LogFactory.getLog(ConnectorBuilderDBusImpl.class);

	private Map<Long, Connector<?>> connectors = Collections.synchronizedMap(new HashMap<>());
	private AtomicLong id = new AtomicLong(1);
	private DBusConnection conn;

	public ConnectorBuilderDBusImpl(DBusConnection conn) {
		this.conn = conn;
	}

	public List<String> Connectors() {
		return connectors.keySet().stream().map(item -> item.toString()).collect(Collectors.toList());
	}

	public void Destroy(long id) throws IOException {
		synchronized (connectors) {
			LOG.info(String.format("Destroying connector %d", id));
			try {
				Connector<?> connector = connectors.get(id);
				if (connector != null) {
					connector.close();
				} else {
					connectors.remove(id);
				}
			} finally {
				LOG.info(String.format("Destroyed connector %s", id));
			}
		}
	}

	public long Create(Map<String, String> values) throws DBusException {
		long id = this.id.getAndIncrement();
		LOG.info(String.format("Creating connector %s", id));

		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		ConnectorConfigurationParameters connectorConfigurationParameters = createConnectorConfigurationParameters(
				connectorBuilder, MultiMap.fromMapSingle(values));
		Connector<?> connector = connectorBuilder.buildConnector(connectorConfigurationParameters);
		conn.exportObject("/Connector/" + id , new ConnectorDBusImpl(id, connector));
		connectors.put(id, connector);
		LOG.info(String.format("Created connector %s", id));

		return id;
	}

	@Override
	public boolean isRemote() {
		return true;
	}

	@Override
	public String getObjectPath() {
		return "/ConnectorBuilder";
	}

	protected ConnectorConfigurationParameters createConnectorConfigurationParameters(ConnectorBuilder connectorBuilder,
			MultiMap configurationParameters) {
		ConnectorConfigurationParameters connectorConfigurationParameters = connectorBuilder
				.buildConfiguration(configurationParameters);
		connectorConfigurationParameters.setIdentityAttributesToRetrieve(
				Arrays.asList(configurationParameters.getStringOrDefault("attributesToRetrieve", "").split(",")));
		return connectorConfigurationParameters;
	}
}
