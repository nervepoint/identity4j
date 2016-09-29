package com.identity4j.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface HttpResponse {
	byte[] content();

	String contentString();

	HttpStatus status();

	void release();

	List<HttpPair> headers();

	InputStream contentStream() throws IOException;
}
