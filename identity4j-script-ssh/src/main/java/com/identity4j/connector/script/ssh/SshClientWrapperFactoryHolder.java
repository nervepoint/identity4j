package com.identity4j.connector.script.ssh;

/*
 * #%L
 * Identity4J Scripted SSH Connector
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

public class SshClientWrapperFactoryHolder {

	// CWE-567: volatile ensures cross-thread visibility; getClientFactory uses DCL
	static volatile SshClientWrapperFactory clientFactory;
	
	public static synchronized void setClientFactory(SshClientWrapperFactory clientFactory) {
		SshClientWrapperFactoryHolder.clientFactory = clientFactory;
	}
	
	public static SshClientWrapperFactory getClientFactory() {
		SshClientWrapperFactory f = clientFactory;
		if (f == null) {
			synchronized (SshClientWrapperFactoryHolder.class) {
				f = clientFactory;
				if (f == null) {
					try {
						f = (SshClientWrapperFactory) 
								Class.forName(System.getProperty(
										"identity4j.ssh.clientFactory", 
										"com.identity4j.connector.script.ssh.j2ssh.DefaultSshClientWrapperFactory")).newInstance();
						clientFactory = f;
					} catch (Throwable e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				}
			}
		}
		return f;
	}
	
	public static boolean hasClientFactory() {
		return clientFactory != null;
	}
	
}
