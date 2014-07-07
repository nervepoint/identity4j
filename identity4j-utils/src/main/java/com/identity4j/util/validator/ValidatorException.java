/* HEADER */
package com.identity4j.util.validator;

/**
 * Exception thrown when a {@link Validator} problem occurs. This is not
 * intended to be thrown from a validator when validation fails, but instead is
 * to be used to instead signify a problem with the validator itself.
 * 
 * As this class is a runtime exception, there is no need for user code to catch
 * it or subclasses if any error is to be considered fatal (the usual case).
 */
public class ValidatorException extends RuntimeException {
    private static final long serialVersionUID = -7157434556969163365L;

    /**
     * Constructor.
     *
     * @param message message
     */
    public ValidatorException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message message
     * @param cause cause
     */
    public ValidatorException(String message, Throwable cause) {
        super(message, cause);
    }
}