package com.identity4j.util.http.request;

import java.io.IOException;

public class HttpRequestException extends RuntimeException {

	private static final long serialVersionUID = -5058451768622653316L;

	public HttpRequestException(String message, IOException e) {
		super(message, e);
	}

}
