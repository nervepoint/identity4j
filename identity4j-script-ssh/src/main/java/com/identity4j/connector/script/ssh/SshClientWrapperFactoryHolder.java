package com.identity4j.connector.script.ssh;

public class SshClientWrapperFactoryHolder {

	static SshClientWrapperFactory clientFactory;
	
	public static void setClientFactory(SshClientWrapperFactory clientFactory) {
		SshClientWrapperFactoryHolder.clientFactory = clientFactory;
	}
	
	public static SshClientWrapperFactory getClientFactory() {
		if(!hasClientFactory()) {
			try {
				clientFactory = (SshClientWrapperFactory) 
						Class.forName(System.getProperty(
								"identity4j.ssh.clientFactory", 
								"com.identity4j.connector.script.ssh.j2ssh.DefaultSshClientWrapperFactory")).newInstance();
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		}
		return clientFactory;
	}
	
	public static boolean hasClientFactory() {
		return clientFactory!=null;
	}
	
}
