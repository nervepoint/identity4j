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