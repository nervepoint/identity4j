package com.identity4j.connector.script.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import com.identity4j.util.expect.ExpectTimeoutException;

public interface SshClientWrapper {

	SshCommand executeCommand(String cmd)
			throws IOException, ExpectTimeoutException;

	SshCommand sudoCommand(String cmd)
			throws IOException, ExpectTimeoutException;

	BufferedReader readFile(String path) throws IOException;

	boolean isConnected();

	boolean isAuthenticated();

	void disconnect();

	void uploadFile(InputStream in, String filename) throws IOException;

	InputStream downloadFile(String filename) throws IOException;
	
}