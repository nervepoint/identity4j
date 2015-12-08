package com.identity4j.connector.script.ssh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.ssh2.KBIPrompt;
import com.sshtools.ssh2.KBIRequestHandler;

public class SshKBIHandler implements KBIRequestHandler {

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
	public boolean showPrompts(String name, String instruction, KBIPrompt[] prompts) {
		boolean foundPassword = prompts.length == 0;
		for (int i = 0; i < prompts.length; i++) {
			if (prompts[i].getPrompt().toLowerCase().matches(passwordPattern)) {
				prompts[i].setResponse(new String(password));
				foundPassword = true;
			}
			if (prompts[i].getPrompt().toLowerCase().matches(newPasswordPattern)) {
				requiresPasswordChange = true;
			}
		}

		return foundPassword;
	}

}
