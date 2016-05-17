package com.identity4j.connector.script.ssh;

public interface SshClientWrapperFactory {

	SshClientWrapper createInstance(SshConfiguration config);
}
