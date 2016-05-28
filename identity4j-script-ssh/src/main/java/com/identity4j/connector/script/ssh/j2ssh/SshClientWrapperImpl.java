package com.identity4j.connector.script.ssh.j2ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.identity4j.connector.script.ssh.SshClientWrapper;
import com.identity4j.connector.script.ssh.SshCommand;
import com.identity4j.connector.script.ssh.SshConfiguration;
import com.identity4j.util.expect.ExpectTimeoutException;
import com.sshtools.net.SocketTransport;
import com.sshtools.sftp.SftpClient;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshException;

public class SshClientWrapperImpl implements SshClientWrapper {
	SshClient client;
	SshConfiguration config;
	
	public SshClientWrapperImpl(SshClient client, SshConfiguration config) {
		this.client = client;
		this.config = config;
	}


	/* (non-Javadoc)
	 * @see com.identity4j.connector.script.ssh.SshClientWrapper#executeCommand(java.lang.String)
	 */
	@Override
	public SshCommand executeCommand(String cmd) throws IOException, ExpectTimeoutException {
		try {
			return new SshCommandImpl(client, cmd);
		} catch (SshException e) { 
			throw new IOException(e);
		} catch(ChannelOpenException e) {
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.identity4j.connector.script.ssh.SshClientWrapper#sudoCommand(java.lang.String)
	 */
	@Override
	public SshCommand sudoCommand(String cmd) throws IOException, ExpectTimeoutException {
		try {
			return new SshCommandImpl(client, cmd, config.getSudoCommand(), config.getSudoPrompt(), config.getServiceAccountPassword());
		} catch (SshException e) { 
			throw new IOException(e);
		} catch(ChannelOpenException e) {
			throw new IOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.identity4j.connector.script.ssh.SshClientWrapper#readFile(java.lang.String)
	 */
	@Override
	public BufferedReader readFile(String path) throws IOException {
		try {
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
		} catch (SshException e) { 
			throw new IOException(e);
		} catch (SftpStatusException e) { 
			throw new IOException(e);
		} catch(ChannelOpenException e) {
			throw new IOException(e);
		}
	}


	@Override
	public boolean isConnected() {
		return client.isConnected();
	}


	@Override
	public boolean isAuthenticated() {
		return client.isAuthenticated();
	}


	@Override
	public void disconnect() {
		if(client.isConnected()) {
			client.disconnect();
		}
	}
}