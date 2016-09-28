package com.identity4j.util.http;

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
