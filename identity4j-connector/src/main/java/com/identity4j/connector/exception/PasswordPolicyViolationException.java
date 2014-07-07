package com.identity4j.connector.exception;

/**
 * Exception thrown when the connector is given a password that passed Access
 * Managers own rules, but fails when used by the connector. This may occur when
 * an overridden password policy is weaker than that actually enforced by the
 * connector target at time of password change/reset.
 */
@SuppressWarnings("serial")
public class PasswordPolicyViolationException extends ConnectorException {

	public PasswordPolicyViolationException() {
	}

	public PasswordPolicyViolationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordPolicyViolationException(String message) {
		super(message);
	}

	public PasswordPolicyViolationException(Throwable cause) {
		super(cause);
	}

}
