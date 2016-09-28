package com.identity4j.util.http.request;

import java.net.URI;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.identity4j.util.http.HttpResponse;

public class HttpRequestHandlerTest {

	private static final HttpRequestHandler HTTP_REQUEST_HANDLER = new HttpRequestHandler();
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void itShouldFetchDataWithStatusCode200ForValidContent() throws Exception {
		HttpResponse httpResponse = HTTP_REQUEST_HANDLER.handleRequestGet(new URI("https://www.google.com"));
		try {
			Assert.assertEquals("Should be HTTP status OK", 200,httpResponse.status().getCode());
		}
		finally {
			httpResponse.release();
		}
	}
	
	@Test
	public void itShouldThrowHttpResponseExceptionWithStatusCode404ForNonExistingContent() throws Exception {
		HttpResponse httpResponse = HTTP_REQUEST_HANDLER.handleRequestGet(new URI("https://www.google.com/abc"));
		try {
			Assert.assertEquals("Should be HTTP status OK", 404,httpResponse.status().getCode());
		}
		finally {
			httpResponse.release();
		}
	}
}
