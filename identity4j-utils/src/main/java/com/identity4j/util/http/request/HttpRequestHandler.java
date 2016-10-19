package com.identity4j.util.http.request;

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
