package com.identity4j.http;

import com.identity4j.util.http.HttpProvider;
import com.identity4j.util.http.HttpProviderClient;

/**
 * Quite a dumb {@link HttpProvider} based on Apache HTTP that doesn't do any
 * connection pooling or anything smart with clients.
 */
public class HttpProviderImpl implements HttpProvider {

	@Override
	public HttpProviderClient getClient(String urlStr) {
		return getClient(urlStr, null, null, null);
	}

	@Override
	public HttpProviderClient getClient(String urlStr, String username, char[] password, String realm) {
		return new HttpClientImpl(urlStr, username, password, realm);
	}

}