package com.identity4j.connector.script.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.identity4j.util.expect.ExpectTimeoutException;
import com.sshtools.net.SocketTransport;
import com.sshtools.sftp.SftpClient;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshException;

public class SshClientWrapper {
	SshClient client;
	SshConfiguration config;
	
	public SshClientWrapper(SshClient client, SshConfiguration config) {
		this.client = client;
		this.config = config;
	}


	public SshCommand executeCommand(String cmd) throws IOException, SshException, ChannelOpenException, ExpectTimeoutException {
		return new SshCommand(client, cmd);
	}

	public SshCommand sudoCommand(String cmd) throws IOException, SshException, ChannelOpenException, ExpectTimeoutException {
		return new SshCommand(client, cmd, config.getSudoCommand(), config.getSudoPrompt(), config.getServiceAccountPassword());
	}

	public BufferedReader readFile(String path) throws IOException, SshException, ChannelOpenException, SftpStatusException {
		final SftpClient c = new SftpClient(client);
		return new BufferedReader(new InputStreamReader(c.getInputStream(path))) {
			@Override
			public void close() throws IOException {
				super.close();
				try {
					c.exit();
				} catch (SshException e) {
					throw new IOException(e);
				}
			}

		};
	}
	public static void main(String[] args) {
		
		try {
			
			com.sshtools.ssh.SshConnector con = com.sshtools.ssh.SshConnector.createInstance();
			
			SshClient ssh = con.connect(new SocketTransport("localhost", 22), "lee", true);
			
			PasswordAuthentication pwd = new PasswordAuthentication();
			pwd.setPassword("bluemars73");
			
			ssh.authenticate(pwd);
			
			
			SshClientWrapper w = new SshClientWrapper(ssh, null);
			
			SshCommand command = w.executeCommand("ls -l");
			
			command.maybeExpectNextLine("nothing", false, 30000);
			
			String line;
			while((line = command.readLine())!=null) {
				System.out.println(line);
			}
			System.out.println("Finished");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}