package com.identity4j.connector.script.ssh;

import java.io.BufferedReader;
import java.io.IOException;

import com.identity4j.util.expect.ExpectTimeoutException;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.SshException;

public interface SshClientWrapper {

	SshCommand executeCommand(String cmd)
			throws IOException, ExpectTimeoutException;

	SshCommand sudoCommand(String cmd)
			throws IOException, ExpectTimeoutException;

	BufferedReader readFile(String path) throws IOException;

	boolean isConnected();

	boolean isAuthenticated();

	void disconnect();

}