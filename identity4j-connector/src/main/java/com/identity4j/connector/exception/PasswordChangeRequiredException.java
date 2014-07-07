/* HEADER */
package com.identity4j.connector.exception;

/**
 * Exception thrown when a {@link com.identity4j.principal.Principal} is
 * valid, a correct password was supplied, but that password must now be 
 * changed to continue.
 */
public class PasswordChangeRequiredException extends ConnectorException {
    private static final long serialVersionUID = 7876665688800229534L;

    public PasswordChangeRequiredException() {
        super();
    }
    public PasswordChangeRequiredException(String message) {
        super(message);
    }
}