/* HEADER */
package com.identity4j.connector.exception;

/*
 * #%L
 * Identity4J Connector
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


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
    
    public PrincipalNotFoundException(String message, PrincipalType principalType) {
        super(message);
        this.principalType = principalType;
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