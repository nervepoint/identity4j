/* HEADER */
package com.identity4j.connector;

/*
 * #%L
 * Identity4J Connector
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.net.SocketFactory;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

/**
 * Implementation of the <a
 * href="http://en.wikipedia.org/wiki/Builder_pattern">builder pattern</a> which
 * creates <tt>Connector</tt> instances.
 */
public class ConnectorBuilder {
	/**
	 * The fully qualified name of the desired <code>Connector</code>
	 * <tt>Class</tt> implementation to create.
	 */
	public static final String CONNECTOR_CLASS = "i4jConnectorClass";
	/**
	 * The fully qualified name of the desired
	 * <code>ConnectorConfigurationParameters</code> <tt>Class</tt>
	 * implementation to create.
	 */
	public static final String CONFIGURATION_CLASS = "i4jConfigurationClass";
	
	private SocketFactory socketFactory;
	
	/**
	 * Get the {@link SocketFactory} to use for this connector (if the connector
	 * uses sockets and supports this feature).
	 * 
	 * @param socketFactory socket factory
	 */
	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	/**
	 * Set the {@link SocketFactory} to use for this connector (if the connector
	 * uses sockets and supports this feature).
	 * 
	 * @param socketFactory socket factory
	 */
	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	/**
	 * Creates a <tt>Connector</tt> instance from the supplied configuration
	 * parameters.
	 * 
	 * @param configurationParameters the configuration parameters used to build
	 *            the <tt>Connector</tt>
	 * @return a <tt>Connector</tt> built from the supplied configuration
	 *         parameters
	 * @throws ConnectorException on any exception
	 */
	public final Connector<?> buildConnector(Map<String, String[]> configurationParameters) throws ConnectorException {
		MultiMap multiMap = new MultiMap(configurationParameters);
		return buildConnector(multiMap);
	}

	/**
	 * Creates a <tt>ConnectorConfigurationParameters</tt> instance from the
	 * supplied configuration parameters.
	 * 
	 * @param configurationParameters the configuration parameters used to build
	 *            the <tt>Connector</tt>
	 * @return a <tt>configurationParameters</tt> built from the supplied
	 *         configuration parameters
	 * @throws ConnectorException on any exception
	 */
	public final <P extends ConnectorConfigurationParameters> P buildConfiguration(Map<String, String[]> configurationParameters)
			throws ConnectorException {
		MultiMap multiMap = new MultiMap(configurationParameters);
		return buildConfiguration(multiMap);
	}

	/**
	 * Creates a <tt>Connector</tt> instance from the supplied configuration
	 * parameters.
	 * 
	 * @param configurationParameters the configuration parameters used to build
	 *            the <tt>Connector</tt>
	 * @return a <tt>Connector</tt> built from the supplied configuration
	 *         parameters
	 * @throws ConnectorException on any exception
	 */
	@SuppressWarnings("unchecked")
	public final <P extends Connector<?>> P buildConnector(MultiMap configurationParameters) throws ConnectorException {
		return (P)buildConnector((ConnectorConfigurationParameters)buildConfiguration(configurationParameters));
	}

	/**
	 * Creates a <tt>Connector</tt> instance from the supplied configuration
	 * parameters.
	 * 
	 * @param configurationParameters the configuration parameters object
	 * @return a <tt>Connector</tt> built from the supplied configuration
	 *         parameters
	 * @throws ConnectorException on any exception
	 */
	@SuppressWarnings("unchecked")
	public final <P extends ConnectorConfigurationParameters> Connector<P> buildConnector(P configurationParameters) throws ConnectorException {
		String connectionClass = configurationParameters.getConfigurationParameters().getString(CONNECTOR_CLASS);
		Connector<P> connector;
		if(StringUtil.isNullOrEmpty(connectionClass)) {
			Class<? extends Connector<P>> clazz = (Class<? extends Connector<P>>) configurationParameters.getConnectorClass();
			if(clazz != null) {
				try {
					connector = clazz.newInstance();
				} catch (IllegalAccessException iae) {
					throw new ConnectorException("Permissions error creating connector", iae);
				} catch (IllegalArgumentException iae) {
					throw new ConnectorException("Invalid argument creating connector", iae);
				} catch (InstantiationException inse) {
					throw new ConnectorException("Instantiation error", inse);
				}
			}
			else
				throw new ConnectorException("Parameters do not specify a default connector class, nor is a custom class provided.");
		}
		else
			connector = (Connector<P>) createClassInstance(connectionClass, new Class[] {}, new Object[] {});
		if(socketFactory != null)
			connector.setSocketFactory(socketFactory);
		connector.open(configurationParameters);
		return connector;
	}

	@SuppressWarnings("unchecked")
	public <P extends ConnectorConfigurationParameters> P buildConfiguration(MultiMap configurationParameters) throws ConnectorException {
		String configurationClass = configurationParameters.getString(CONFIGURATION_CLASS);
		ConnectorConfigurationParameters connectorConfigurationParameters = null;
		if (!StringUtil.isNullOrEmpty(configurationClass)) {
			connectorConfigurationParameters = (ConnectorConfigurationParameters) createClassInstance(configurationClass,
				new Class[] { MultiMap.class }, new Object[] { configurationParameters });
		}
		else {
			connectorConfigurationParameters = new DefaultConnectorConfiguration(configurationParameters);
		}
		return (P)connectorConfigurationParameters;
	}

	/**
	 * Creates and initializes a new instance of the required class, with the
	 * specified initialization parameters. Individual parameters are
	 * automatically unwrapped to match primitive formal parameters, and both
	 * primitive and reference parameters are subject to method invocation
	 * conversions as necessary.
	 * 
	 * @param className the fully qualified name of the desired class.
	 * @param parameterTypes the parameter array
	 * @param args array of objects to be passed as arguments to the constructor
	 *            call; values of primitive types are wrapped in a wrapper
	 *            object of the appropriate type (e.g. a <tt>float</tt> in a
	 *            {@link java.lang.Float Float})
	 * @return a new object created by calling the constructor this object
	 *         represents
	 * @throws ConnectorException on any exception
	 */
	private Object createClassInstance(String className, Class<?>[] parameterTypes, Object[] args) throws ConnectorException {
		try {
			Class<?> clazz = getClass().getClassLoader().loadClass(className);
			Constructor<?> constructor = clazz.getConstructor(parameterTypes);
			Object o = constructor.newInstance(args);
			return o;
		} catch (ClassNotFoundException cnfe) {
			throw new ConnectorException("The connector class could not be found", cnfe);
		} catch (IllegalAccessException iae) {
			throw new ConnectorException("Permissions error creating connector", iae);
		} catch (IllegalArgumentException iae) {
			throw new ConnectorException("Invalid argument creating connector", iae);
		} catch (InstantiationException inse) {
			throw new ConnectorException("Instantiation error", inse);
		} catch (InvocationTargetException ite) {
			throw new ConnectorException(ite.getTargetException().getMessage(), ite.getTargetException());
		} catch (NoSuchMethodException nsme) {
			throw new ConnectorException("Connector method missing", nsme);
		}
	}
}