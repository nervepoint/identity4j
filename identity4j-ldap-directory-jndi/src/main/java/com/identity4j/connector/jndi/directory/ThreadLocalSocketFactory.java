package com.identity4j.connector.jndi.directory;

import javax.net.SocketFactory;

public abstract class ThreadLocalSocketFactory extends SocketFactory {

	static ThreadLocal<SocketFactory> local = new ThreadLocal<SocketFactory>();

	public static SocketFactory getDefault() {
		SocketFactory result = local.get();
		if (result == null)
			throw new IllegalStateException();
		return result;
	}

	public static void set(SocketFactory factory) {
		local.set(factory);
	}

	public static void remove() {
		local.remove();
	}

}