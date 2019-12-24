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

@SuppressWarnings("serial")
public class HttpException extends RuntimeException {
	private HttpStatus status;

	public HttpException() {
		super();
	}

	public HttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

	public HttpException(HttpStatus status) {
		super();
		this.status = status;
	}

	public HttpException(HttpStatus status, String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.status = status;
	}

	public HttpException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public HttpException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpException(HttpStatus status, Throwable cause) {
		super(cause);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	@Override
	public String getMessage() {
		return super.getMessage() == null ? ( status == null ? null : status.toString() ) : super.getMessage() + ( status == null ? "" : ". " + status.toString());
	}

	@Override
	public String getLocalizedMessage() {
		return super.getLocalizedMessage() == null ? ( status == null ? null : status.toString() ) : super.getLocalizedMessage() + ( status == null ? "" : ". " + status.toString());
	}

}
