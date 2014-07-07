/* HEADER */
package com.identity4j.connector.exception;

/**
 * Root of the hierarchy of connector exceptions. This exception hierarchy aims
 * to let user code find and handle the kind of error encountered without
 * knowing the details of the particular API in use (e.g. JNDI, JDBC, etc).
 * 
 * As this class is a runtime exception, there is no need for user code to catch
 * it or subclasses if any error is to be considered fatal (the usual case).
 */
public class ConnectorException extends RuntimeException {
    private static final long serialVersionUID = 7876665688800229534L;

    public ConnectorException() {
        super();
    }

    public ConnectorException(Throwable cause) {
        super(cause);
    }

    public ConnectorException(String message) {
        super(message);
    }

    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}