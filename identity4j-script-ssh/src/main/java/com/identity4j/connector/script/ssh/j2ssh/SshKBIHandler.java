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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;

public class SshKBIHandler implements SshKeyboardInteractiveAuthenticator {

	final static Log LOG = LogFactory.getLog(SshKBIHandler.class);

	private boolean requiresPasswordChange;
	private char[] password;
	private String passwordPattern = "password.*:.*";
	private String newPasswordPattern = "new password.*:.*";

	public SshKBIHandler(char[] password) {
		this.password = password;
	}

	public SshKBIHandler(String password) {
		this(password.toCharArray());
	}

	public boolean isRequiresPasswordChange() {
		return requiresPasswordChange;
	}

	public String getPasswordPattern() {
		return passwordPattern;
	}

	public void setPasswordPattern(String passwordPattern) {
		this.passwordPattern = passwordPattern;
	}

	public String getNewPasswordPattern() {
		return newPasswordPattern;
	}

	public void setNewPasswordPattern(String newPasswordPattern) {
		this.newPasswordPattern = newPasswordPattern;
	}

	@Override
	public String[] challenge(String name, String instruction, String[] prompt, boolean[] echo) {
		boolean foundPassword = prompt.length == 0;
		String[] answers = new String[prompt.length];
		for (int i = 0; i < prompt.length; i++) {
			if (prompt[i].toLowerCase().matches(passwordPattern)) {
				answers[i] = new String(password);
				foundPassword = true;
			}
			if (prompt[i].toLowerCase().matches(newPasswordPattern)) {
				requiresPasswordChange = true;
			}
		}

		return foundPassword ? answers : null;
	}

}
