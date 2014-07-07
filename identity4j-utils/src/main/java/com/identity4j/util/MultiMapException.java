/* HEADER */
package com.identity4j.util;

/**
 * Exception thrown when a {@link MultiMap} problem occurs.
 * 
 * As this class is a runtime exception, there is no need for user code to catch
 * it or subclasses if any error is to be considered fatal (the usual case).
 */
public class MultiMapException extends RuntimeException {
    private static final long serialVersionUID = -7157434556969163365L;

    /**
     * Constructor.
     *
     * @param message message
     */
    public MultiMapException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message message
     * @param cause cause
     */
    public MultiMapException(String message, Throwable cause) {
        super(message, cause);
    }
}