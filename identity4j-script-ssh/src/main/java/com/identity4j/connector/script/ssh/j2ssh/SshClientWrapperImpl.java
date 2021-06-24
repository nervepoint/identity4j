package com.identity4j.connector.script.ssh.j2ssh;

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

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.identity4j.connector.script.ssh.SshClientWrapper;
import com.identity4j.connector.script.ssh.SshCommand;
import com.identity4j.connector.script.ssh.SshConfiguration;
import com.identity4j.util.expect.ExpectTimeoutException;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.SftpClient;

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
		return sudoCommand(cmd);
	}

	/* (non-Javadoc)
	 * @see com.identity4j.connector.script.ssh.SshClientWrapper#sudoCommand(java.lang.String)
	 */
	@Override
	public SshCommand sudoCommand(String cmd) throws IOException, ExpectTimeoutException {
		try {
			return new SshCommandImpl(client, 
					cmd, 
					config.getSudoCommand(), 
					config.getSudoPrompt(), 
					config.getServiceAccountPassword());
		} catch (SshException e) { 
			throw new IOException(e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.identity4j.connector.script.ssh.SshClientWrapper#readFile(java.lang.String)
	 */
	@Override
	public BufferedReader readFile(String path) throws IOException {
		try {
			return new BufferedReader(new InputStreamReader(downloadFile(path)));
		} catch (SshException e) { 
			throw new IOException(e.getMessage(), e);
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
			try {
				client.close();
			} catch (IOException e) {
			}
		}
	}


	@Override
	public void uploadFile(InputStream in, String filename) throws IOException {
		
		SftpClient scp = client.sftp();
		try {
			scp.put(filename, in);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
			try {
				scp.close();
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	public InputStream downloadFile(String filename) throws IOException {
		SftpClient scp = client.sftp();
		try {
			return new FilterInputStream(scp.get(filename)) {
				@Override
				public void close() throws IOException {
					try {
						scp.close();
					}
					finally {
						super.close();
					}
				}
			};
		} catch(Exception e) {
			throw new IOException(e.getMessage(), e);
		} 
	}
}