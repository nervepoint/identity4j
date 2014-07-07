/* HEADER */
package com.identity4j.util.expect;

/**
 * <p>
 * Exception thrown when an operation times out.
 * </p>
 * 
 * @author Lee David Painter
 */
public class ExpectTimeoutException extends Exception {

	private static final long serialVersionUID = -459552052479068641L;

	ExpectTimeoutException() {
		super("The expect operation timed out");
	}

	ExpectTimeoutException(String str) {
		super(str);
	}
}
