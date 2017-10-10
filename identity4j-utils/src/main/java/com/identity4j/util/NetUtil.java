package com.identity4j.util;

/*
 * #%L
 * Identity4J Utils
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

import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class NetUtil {


	public static String getRPCURL() throws SocketException {
		final String ifaces = System.getProperty("nervepoint.interfaces");
		StringTokenizer t = new StringTokenizer(ifaces, ",");
		while (t.hasMoreTokens()) {
			String addrType = t.nextToken();
			if (addrType.startsWith("https")) {
				int port = Integer.parseInt(System.getProperty("nervepoint." + addrType));
				String hostname = System.getProperty("nervepoint." + addrType + ".addr");
				if (hostname == null) {
					hostname = "0.0.0.0";
				}
				InetSocketAddress addr = new InetSocketAddress(hostname, port);
				if (addr.getAddress().isAnyLocalAddress()) {
					addr = NetUtil.getBestAddress(addr);
				}
				return "https://" + addr.getHostName() + ":" + port + "/";
			}
		}
		return null;
	}
	
	public static InetSocketAddress getBestAddress(InetSocketAddress addr) throws SocketException {
		Enumeration<NetworkInterface> networkIfs = NetworkInterface.getNetworkInterfaces();
		while (networkIfs.hasMoreElements()) {
			NetworkInterface networkIf = networkIfs.nextElement();
			if (!networkIf.isLoopback() && networkIf.isUp()) {
				boolean found = false;
				for (InterfaceAddress a : networkIf.getInterfaceAddresses()) {
					if (a.getAddress().getClass().equals(addr.getAddress().getClass())) {
						addr = new InetSocketAddress(a.getAddress(), addr.getPort());
						found = true;
					}
				}
				if (found) {
					break;
				}
			}
		}
		return addr;
	}
}
