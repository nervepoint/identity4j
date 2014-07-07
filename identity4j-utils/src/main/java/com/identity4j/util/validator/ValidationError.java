/* HEADER */
package com.identity4j.util.validator;

import java.io.Serializable;

/**
 * Represents a localised error that occurred during field validation.
 */
public class ValidationError implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Validator message bundle name
	 */
	public static String RESOURCE_BUNDLE_VALIDATOR = "validator";

	private final String bundleName;
	private final String messageKey;
	private final Object[] args;
	private final ValidationContext context;

	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param args arguments
	 */
	public ValidationError(String message, ValidationContext context, Object... args) {
		this(RESOURCE_BUNDLE_VALIDATOR, context, message, args);
	}

	/**
	 * Constructor.
	 * 
	 * @param bundleName bundle name
	 * @param messageKey message
	 * @param args
	 */
	public ValidationError(String bundleName, ValidationContext context, String messageKey, Object[] args) {
		this.context = context;
		this.bundleName = bundleName;
		this.messageKey = messageKey;
		this.args = args;
	}

	/**
	 * Get the context this validation error was created in.
	 * 
	 * @return field
	 */
	public final ValidationContext getContext() {
		return context;
	}

	/**
	 * Get the resource bundle name that contains the resources for this error.
	 * 
	 * @return resource bundle name
	 */
	public final String getBundle() {
		return bundleName;
	}

	/**
	 * Get the message key to use for this validation error.
	 * 
	 * @return message key
	 */
	public final String getMessage() {
		return messageKey;
	}

	/**
	 * Get the arguments to use in message replacement for this error.
	 * 
	 * @return arguments
	 */
	public final Object[] getArgs() {
		return args;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[bundle='").append(getBundle());
		builder.append("', message='").append(getMessage());
		builder.append("', args='").append(getArgs()).append("']");
		return builder.toString();
	}
}