/* HEADER */
package com.identity4j.connector.exception;

/**
 * Exception thrown when the supplied login credentials are invalid.
 */
public class InvalidLoginCredentialsException extends ConnectorException {
    private static final long serialVersionUID = 5808360621254715960L;

    public InvalidLoginCredentialsException() {
        // For scripts
        super("Invalid login credentials");
    }
    
    public InvalidLoginCredentialsException(String message) {
        super(message);
    }
}