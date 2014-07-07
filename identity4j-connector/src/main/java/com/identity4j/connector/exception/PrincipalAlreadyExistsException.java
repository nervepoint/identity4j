/* HEADER */
package com.identity4j.connector.exception;

import com.identity4j.connector.PrincipalType;

/**
 * Exception thrown when a {@link com.identity4j.principal.Principal} is already
 * present.
 */
public class PrincipalAlreadyExistsException extends ConnectorException {

	private static final long serialVersionUID = -4726068995287598383L;
	
	private PrincipalType principalType;

	public PrincipalAlreadyExistsException(String message) {
        super(message);
    }

    public PrincipalAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PrincipalAlreadyExistsException(String message, Throwable cause,PrincipalType principalType) {
        super(message, cause);
        this.principalType = principalType;
    }

	public PrincipalType getPrincipalType() {
		return principalType;
	}

}