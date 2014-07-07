package com.identity4j.util.json;

/**
 * Exception thrown while mapping json data to java objects.
 * 
 * @author gaurav
 *
 */
public class JsonMapperException extends RuntimeException {

	private static final long serialVersionUID = -4722962382945960884L;
	
	private final String message;
	private final Exception exception;
	
	public JsonMapperException(String message, Exception exception) {
		this.message = message;
		this.exception = exception;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Exception getException() {
		return exception;
	}
}
