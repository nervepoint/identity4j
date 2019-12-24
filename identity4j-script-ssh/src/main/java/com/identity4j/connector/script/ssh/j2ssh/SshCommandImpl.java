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
import com.sshtools.ssh.ChannelOpenException;
import com.sshtools.ssh.PseudoTerminalModes;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshException;
import com.sshtools.ssh.SshIOException;
import com.sshtools.ssh.SshSession;
import com.sshtools.ssh2.Ssh2Session;

public class SshCommandImpl extends Expect implements SshCommand {

    final static Log LOG = LogFactory.getLog(SshCommandImpl.class);

    private boolean ok;
    private SshSession session;

    public SshCommandImpl(SshClient ssh, String command) throws IOException, SshException, ChannelOpenException,
        ExpectTimeoutException {
        this(ssh, command, null, null, null);
    }

    public SshCommandImpl(SshClient ssh, String command, String sudoCommand, String sudoPrompt, String sudoPassword)
        throws IOException, SshException, ChannelOpenException, ExpectTimeoutException {
        super(new RegularExpressionExpectMatcher());

        LOG.debug("Opening session channel");
        // session = ssh.openSessionChannel(10000);
        session = ssh.openSessionChannel();
        LOG.debug("Requesting terminal");
        PseudoTerminalModes pty = new PseudoTerminalModes(ssh);
        if (!session.requestPseudoTerminal("dumb", 0, 0, 0, 0, pty)) {
            throw new IOException("Could not start pseudo tty");
        }

        boolean useSudo = false;

        // If not running as root, we will need to sudo
        if (!ssh.getUsername().equals("root") && sudoCommand != null) {
            command = sudoCommand + " " + command;
            useSudo = true;
        }

        LOG.debug("Executing command " + command);

        ok = session.executeCommand(command);

        LOG.debug("Gettings streams");
        setIn(session.getInputStream());
        setOut(session.getOutputStream());

        if (useSudo && sudoPassword != null) {
            try {
                String actualPrompt = sudoPrompt.replaceAll("\\$\\{username\\}", ssh.getUsername());
                LOG.debug("Expecting sudo password prompt: " + actualPrompt);
                if (maybeExpect(actualPrompt, 5000)) {
                    typeAndReturn(sudoPassword);
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
        if (!session.isClosed()) {
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
        return ok ? session.exitCode() : 1;
    }

    @Override
    public boolean isOpen() {
        return !session.isClosed() && session.exitCode() == Ssh2Session.EXITCODE_NOT_RECEIVED && super.isOpen();
    }

    @Override
    public int read() throws IOException, ExpectTimeoutException {
        return read(30000);
    }

    @Override
    public boolean isRunning() throws IOException {
        return session.exitCode() == Ssh2Session.EXITCODE_NOT_RECEIVED || in.available() > 0;
    }

    @Override
    public int read(long timeout) throws IOException, ExpectTimeoutException {
        try {
            return super.read(timeout);
        } catch (SshIOException e) {
            if (e.getRealException().getReason() == SshException.MESSAGE_TIMEOUT) {
                // Ignore this exception as shell will throw its own
                // ExpectTimeoutException
                return Integer.MIN_VALUE;
            } else {
                IOException ioe = new IOException();
                ioe.initCause(e.getRealException());
                throw ioe;
            }
        }
    }
}
