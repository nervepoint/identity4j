package com.identity4j.util.http;

import java.util.Collection;

public interface HttpProviderClient {

	HttpResponse get(String uri, HttpPair... header) throws HttpException;
	
	HttpResponse post(String uri, Collection<HttpPair> parameters, HttpPair... header) throws HttpException;

	HttpResponse post(String uri, String data, HttpPair... header);

	HttpResponse post(String uri, HttpData data, HttpPair... header);

	HttpResponse patch(String uri, String data, HttpPair... headers);

	HttpResponse put(String uri, String data, HttpPair... headers);

	HttpResponse put(String uri, HttpData data, HttpPair... headers);

	HttpResponse delete(String uri, HttpPair... headers);

	void setSocketTimeout(int ms);

	void setConnectTimeout(int ms);

	void setConnectionRequestTimeout(int ms);
	
}
