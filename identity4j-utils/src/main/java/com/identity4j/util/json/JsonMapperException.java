package com.identity4j.util.json;

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
 * Exception thrown while mapping json data to java objects.
 * 
 * @author gaurav
 *
 */
public class JsonMapperException extends RuntimeException {

	private static final long serialVersionUID = -4722962382945960884L;
	
	private final String message;
	private final Exception exception;
	
	public JsonMapperException(String message, Exception exception) {
		this.message = message;
		this.exception = exception;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Exception getException() {
		return exception;
	}
}
