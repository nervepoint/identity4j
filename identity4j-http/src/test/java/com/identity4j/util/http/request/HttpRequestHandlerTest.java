package com.identity4j.util.http.request;

/*
 * #%L
 * Identity4J default HTTP implementation.
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
