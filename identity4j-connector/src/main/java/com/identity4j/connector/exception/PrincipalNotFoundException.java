/* HEADER */
package com.identity4j.connector.exception;

import com.identity4j.connector.PrincipalType;

/**
 * Exception thrown when a {@link com.identity4j.principal.Principal} is not
 * found.
 */
public class PrincipalNotFoundException extends ConnectorException {
    private static final long serialVersionUID = 7876665688800229534L;
    
    private PrincipalType principalType;

    public PrincipalNotFoundException(String message) {
        super(message);
    }

    public PrincipalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PrincipalNotFoundException(String message, Throwable cause,PrincipalType principalType) {
        super(message, cause);
        this.principalType = principalType;
    }

	public PrincipalType getPrincipalType() {
		return principalType;
	}

}