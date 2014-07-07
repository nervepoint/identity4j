/* HEADER */
package com.identity4j.connector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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
	public final Connector buildConnector(Map<String, String[]> configurationParameters) throws ConnectorException {
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
	public final ConnectorConfigurationParameters buildConfiguration(Map<String, String[]> configurationParameters)
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
	public final Connector buildConnector(MultiMap configurationParameters) throws ConnectorException {
		ConnectorConfigurationParameters connectorConfigurationParameters = buildConfiguration(configurationParameters);
		String connectionClass = configurationParameters.getStringOrFail(CONNECTOR_CLASS);
		Connector connector = (Connector) createClassInstance(connectionClass, new Class[] {}, new Object[] {});
		connector.open(connectorConfigurationParameters);
		return connector;
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
	public final Connector buildConnector(ConnectorConfigurationParameters configurationParameters) throws ConnectorException {
		String connectionClass = configurationParameters.getConfigurationParameters().getStringOrFail(CONNECTOR_CLASS);
		Connector connector = (Connector) createClassInstance(connectionClass, new Class[] {}, new Object[] {});
		connector.open(configurationParameters);
		return connector;
	}

	public ConnectorConfigurationParameters buildConfiguration(MultiMap configurationParameters) throws ConnectorException {
		String configurationClass = configurationParameters.getString(CONFIGURATION_CLASS);
		ConnectorConfigurationParameters connectorConfigurationParameters = null;
		if (!StringUtil.isNullOrEmpty(configurationClass)) {
			connectorConfigurationParameters = (ConnectorConfigurationParameters) createClassInstance(configurationClass,
				new Class[] { MultiMap.class }, new Object[] { configurationParameters });
		}
		else {
			connectorConfigurationParameters = new DefaultConnectorConfiguration(configurationParameters);
		}
		return connectorConfigurationParameters;
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
			throw new ConnectorException("failed to createClassInstance", cnfe);
		} catch (IllegalAccessException iae) {
			throw new ConnectorException("failed to createClassInstance", iae);
		} catch (IllegalArgumentException iae) {
			throw new ConnectorException("failed to createClassInstance", iae);
		} catch (InstantiationException inse) {
			throw new ConnectorException("failed to createClassInstance", inse);
		} catch (InvocationTargetException ite) {
			throw new ConnectorException("failed to createClassInstance", ite);
		} catch (NoSuchMethodException nsme) {
			throw new ConnectorException("failed to createClassInstance", nsme);
		}
	}
}