package com.identity4j.util.http;

/*
 * #%L
 * Identity4J Utils
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

/**
 * Encapsulates HTTP status information
 * 
 * @author gaurav
 *
 */
public class HttpStatus {
	
	public final static HttpStatus DEFAULT = new HttpStatus(0, null, null);
	
    private final int code;
    private final String error;
    private String protocol;

    public HttpStatus(int code, String error, String protocol) {
		super();
		this.code = code;
		this.error = error;
	}

	public String getLine() {
		return protocol + " " + code + " " + error;
	}
	
	public String getProtocol() {
		return protocol;
	}

	public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HttpStatus other = (HttpStatus) obj;
		if (code != other.code)
			return false;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		return true;
	}

	@Override
    public String toString() {
        return "HTTP Error: " + code + (error == null ? "" : " (" + error + ")");
    }
    
}