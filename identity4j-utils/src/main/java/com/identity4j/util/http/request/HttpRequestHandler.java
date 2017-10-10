package com.identity4j.util.http.request;

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

import java.net.URI;

import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;

/**
 * This class provides all the methods to send http requests to the REST endpoint.
 * 
 * @author gaurav
 * 
 */

public class HttpRequestHandler {

	/**
	 * Performs HTTP GET request.
	 * 
	 * @param uri target for get request
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestGet(URI uri, HttpPair... header) {
		return getClient(uri).get(getPathAndQuerry(uri), header);
	}


	/**
	 * Performs HTTP POST request.
	 * 
	 * @param uri target for post request
	 * @param data post data
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestPost(URI uri,final String data, HttpPair... header) {
		return getClient(uri).post(getPath(uri), data, header);
	}
	
	/**
	 * Performs HTTP PATCH request.
	 * 
	 * @param uri target for patch request
	 * @param data patch data
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestPatch(URI uri,final String data, HttpPair...headers) {
		return getClient(uri).patch(getPathAndQuerry(uri), data, headers);
	}
	
	
	/**
	 * Performs HTTP PUT request.
	 * 
	 * @param uri target for patch request
	 * @param data patch data
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestPut(URI uri,final String data, HttpPair...headers) {
		return getClient(uri).put(getPathAndQuerry(uri), data, headers);
	}

	/**
	 * Performs HTTP DELETE request.
	 * 
	 * @param uri target for delete request
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestDelete(URI uri, HttpPair... headers)  {
		return getClient(uri).delete(getPathAndQuerry(uri), headers);
	}
	
	protected String getPath(URI uri) {
		return uri.getRawPath();
	}
	
	protected String getPathAndQuerry(URI uri) {
		return uri.getRawPath() + "?" + uri.getRawQuery();
	}

	protected HttpProviderClient getClient(URI uri) {
		HttpProviderClient client = Http.getProvider().getClient(uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort()), null, null, null);
		client.setConnectionRequestTimeout(90000);
		client.setConnectTimeout(90000);
		client.setSocketTimeout(90000);
		return client;
	}
}
