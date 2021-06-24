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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.script.ssh.SshCommand;
import com.identity4j.util.expect.Expect;
import com.identity4j.util.expect.ExpectTimeoutException;
import com.identity4j.util.expect.RegularExpressionExpectMatcher;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;

public class SshCommandImpl extends Expect implements SshCommand {

    final static Log LOG = LogFactory.getLog(SshCommandImpl.class);

    private net.sf.sshapi.SshCommand session;

    public SshCommandImpl(SshClient ssh, String command) throws IOException, SshException,
        ExpectTimeoutException {
        this(ssh, command, null, null, null);
    }

    public SshCommandImpl(SshClient ssh, String command, String sudoCommand, String sudoPrompt, String sudoPassword)
        throws IOException, SshException, ExpectTimeoutException {
        super(new RegularExpressionExpectMatcher());

        boolean useSudo = false;

        // If not running as root, we will need to sudo
        if (!ssh.getUsername().equals("root") && sudoCommand != null) {
            command = sudoCommand + " " + command;
            useSudo = true;
        }

        LOG.debug("Executing command " + command);

        session = ssh.command(command, "dumb", 0,0,0,0, null);

        LOG.debug("Gettings streams");
        setIn(session.getInputStream());
        setOut(session.getOutputStream());

        if (useSudo && sudoPassword != null) {
            try {
                String actualPrompt = sudoPrompt.replaceAll("\\$\\{username\\}", ssh.getUsername());
                LOG.debug("Expecting sudo password prompt: " + actualPrompt);
                if (maybeExpect(actualPrompt, 5000)) {
                    typeAndReturn(sudoPassword);
                    
                    /* Need to read echo'd asterisks and backspaces */
                    readLine(3000);
                } else {
                    LOG.debug("Expect did not match");
                }
            } catch (ExpectTimeoutException e) {
                LOG.debug("Waiting for sudo password prompt timed out");
            }
        }
    }

    @Override
    public int closeAndExit() throws IOException {
        session.close();
        return getExitCode();
    }

    @Override
    public int drainAndWaitForExit() throws IOException {
        if (session.isOpen()) {
            while (session.getInputStream().read() > -1)
                ;
            session.close();
        }
        return getExitCode();
    }

    @Override
    public void ctrlD() throws IOException {
        session.getOutputStream().write(4);
        session.getOutputStream().flush();
    }

    @Override
    public int getExitCode() {
        try {
			return session != null ? session.exitCode() : 1;
		} catch (IOException e) {
			throw new IllegalStateException();
		}
    }

    @Override
    public boolean isOpen() {
        try {
			return session.isOpen() && session.exitCode() == net.sf.sshapi.SshCommand.EXIT_CODE_NOT_RECEIVED && super.isOpen();
		} catch (IOException e) {
			return false;
		}
    }

    @Override
    public int read() throws IOException, ExpectTimeoutException {
        return read(30000);
    }

    @Override
    public boolean isRunning() throws IOException {
        return session.exitCode() == net.sf.sshapi.SshCommand.EXIT_CODE_NOT_RECEIVED || in.available() > 0;
    }

    @Override
    public int read(long timeout) throws IOException, ExpectTimeoutException {
    	// TODO not sure how this will work with SSHAPI, may need special
    	// support for it in each provider
//        try {
            return super.read(timeout);
//        } catch (SshException e) {
//            if (e.getRealException().getReason() == SshException.MESSAGE_TIMEOUT) {
//                // Ignore this exception as shell will throw its own
//                // ExpectTimeoutException
//                return Integer.MIN_VALUE;
//            } else {
//                IOException ioe = new IOException();
//                ioe.initCause(e.getRealException());
//                throw ioe;
//            }
//        }
    }
}
