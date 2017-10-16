package com.identity4j.connector.util;

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


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class DummySSLSocketFactory extends SSLSocketFactory {
	private SSLSocketFactory factory;
	final static Log LOG = LogFactory.getLog(DummySSLSocketFactory.class);

	private static String[] includeCipherSuites;
	private static String[] excludeCipherSuites;
	private static boolean enableSSLv2ClientHello;

	public DummySSLSocketFactory() {
		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
			factory = sslcontext.getSocketFactory();
		} catch (KeyManagementException kme) {
			throw new IllegalArgumentException("Failed to create socket factory", kme);
		} catch (NoSuchAlgorithmException nsae) {
			throw new IllegalArgumentException("Failed to create socket factory", nsae);
		}
	}

	public static String[] getIncludeCipherSuites() {
		return includeCipherSuites;
	}

	public static void setIncludeCipherSuites(String[] includeCipherSuites) {
		DummySSLSocketFactory.includeCipherSuites = includeCipherSuites;
	}

	public static String[] getExcludeCipherSuites() {
		return excludeCipherSuites;
	}

	public static void setExcludeCipherSuites(String[] excludeCipherSuites) {
		DummySSLSocketFactory.excludeCipherSuites = excludeCipherSuites;
	}

	public static boolean isEnableSSLv2ClientHello() {
		return enableSSLv2ClientHello;
	}

	public static void setEnableSSLv2ClientHello(boolean enableSSLv2ClientHello) {
		DummySSLSocketFactory.enableSSLv2ClientHello = enableSSLv2ClientHello;
	}

	public static SocketFactory getDefault() {
		return new DummySSLSocketFactory();
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return factory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		// Include cipher suites.
		if ((getIncludeCipherSuites() != null) && (getIncludeCipherSuites().length > 0)) {
			String[] enabledCipherSuites = factory.getSupportedCipherSuites();
			List<String> enabledCSList = new ArrayList<String>(Arrays.asList(enabledCipherSuites));
			List<String> includedCSList = new ArrayList<String>();

			boolean hasValid = false;
			for (String cipherName : getIncludeCipherSuites()) {
				if (enabledCSList.contains(cipherName)) {
					includedCSList.add(cipherName);
					hasValid = true;
				} else {
					LOG.debug("cipher suite is not enabled: " + cipherName);
				}
			}
			enabledCipherSuites = (String[]) includedCSList.toArray(new String[includedCSList.size()]);
			if (hasValid) {
				return enabledCipherSuites;
			}
		}

		// Exclude cipher suites.
		if ((getExcludeCipherSuites() != null) && (getExcludeCipherSuites().length > 0)) {
			List<String> excludedCSList = Arrays.asList(getExcludeCipherSuites());
			String[] enabledCipherSuites = factory.getSupportedCipherSuites();
			List<String> enabledCSList = new ArrayList<String>(Arrays.asList(enabledCipherSuites));
			Iterator<String> exIter = excludedCSList.iterator();
			while (exIter.hasNext()) {
				String cipherName = (String) exIter.next();
				if (enabledCSList.contains(cipherName)) {
					enabledCSList.remove(cipherName);
				}
			}
			return (String[]) enabledCSList.toArray(new String[enabledCSList.size()]);
		}

		return factory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
		return configureSocket((SSLSocket) factory.createSocket(socket, host, port, autoClose));
	}

	@Override
	public Socket createSocket() throws IOException {
		return configureSocket((SSLSocket) factory.createSocket());
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return configureSocket((SSLSocket) factory.createSocket(address, port, localAddress, localPort));
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return configureSocket((SSLSocket) factory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
			UnknownHostException {
		return configureSocket((SSLSocket) factory.createSocket(host, port, localHost, localPort));
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return configureSocket((SSLSocket) factory.createSocket(host, port));
	}

	private Socket configureSocket(SSLSocket socket) {
		if (enableSSLv2ClientHello) {
			socket.setEnabledProtocols(new String[] { "SSLv2Hello", "SSLv3", "TLSv1" });
		} else {
			socket.setEnabledProtocols(new String[] { "SSLv3", "TLSv1" });
		}
		socket.setEnabledCipherSuites(getSupportedCipherSuites());
		return socket;
	}
}