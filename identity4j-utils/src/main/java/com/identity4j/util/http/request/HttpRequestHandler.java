package com.identity4j.util.http.request;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.identity4j.util.http.response.HttpResponse;

/**
 * This class provides all the methods to send http requests to the REST endpoint.
 * 
 * @author gaurav
 * 
 */

public class HttpRequestHandler {

	private CloseableHttpClient httpClient;
	

	public HttpRequestHandler() {
		//configure timeouts
		RequestConfig requestConfig = RequestConfig.custom().
			    setConnectionRequestTimeout(90000).
			    setConnectTimeout(90000).
			    setSocketTimeout(90000).
			    build();
		//construct instance of http client
		httpClient = HttpClientBuilder.create().
				setDefaultRequestConfig(requestConfig).
				build();
	}

	/**
	 * Performs HTTP GET request.
	 * 
	 * @param uri target for get request
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestGet(URI uri,HTTPHook hook) {
		HttpGet get = new HttpGet(uri);
		return httpRequestPerformer(get, hook, new HTTPPerform() {
			
			@Override
			public HttpResponse apply(HttpRequestBase httpRequestBase) throws IOException {
				return new SimpleHttpRequest(httpRequestBase).request(httpClient);
			}
		});
	}


	/**
	 * Performs HTTP POST request.
	 * 
	 * @param uri target for post request
	 * @param data post data
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestPost(URI uri,final String data, HTTPHook hook) {
		HttpPost post = new HttpPost(uri);
		return httpRequestPerformer(post, hook, new HTTPPerform() {
			
			@Override
			public HttpResponse apply(HttpRequestBase httpRequestBase) throws IOException {
				return new BodyHTTPRequest(httpRequestBase).request(httpClient,data);
			}
		});
	}
	
	/**
	 * Performs HTTP PATCH request.
	 * 
	 * @param uri target for patch request
	 * @param data patch data
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestPatch(URI uri,final String data, HTTPHook hook) {
		HttpPatch patch = new HttpPatch(uri);
		return httpRequestPerformer(patch, hook, new HTTPPerform() {
			
			@Override
			public HttpResponse apply(HttpRequestBase httpRequestBase) throws IOException {
				return new BodyHTTPRequest(httpRequestBase).request(httpClient,data);
			}
		});
	}
	
	
	/**
	 * Performs HTTP PUT request.
	 * 
	 * @param uri target for patch request
	 * @param data patch data
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestPut(URI uri,final String data, HTTPHook hook) {
		HttpPut put = new HttpPut(uri);
		return httpRequestPerformer(put, hook, new HTTPPerform() {
			
			@Override
			public HttpResponse apply(HttpRequestBase httpRequestBase) throws IOException {
				return new BodyHTTPRequest(httpRequestBase).request(httpClient,data);
			}
		});
	}

	/**
	 * Performs HTTP DELETE request.
	 * 
	 * @param uri target for delete request
	 * @param hook any custom http settings to be passed
	 * @return response data which contains data if any and http response codes.
	 */
	public HttpResponse handleRequestDelete(URI uri,HTTPHook hook)  {
		HttpDelete delete = new HttpDelete(uri);
		return httpRequestPerformer(delete, hook, new HTTPPerform() {
			
			@Override
			public HttpResponse apply(HttpRequestBase httpRequestBase) throws IOException {
				return new SimpleHttpRequest(httpRequestBase).request(httpClient);
			}
		});
	}
	
	/**
	 * Interface provides mechanism where by custom http settings can be passed to http client request
	 * 
	 * @author gaurav
	 *
	 */
	public interface HTTPHook{
		/**
		 * Empty hook where by nothing is to be customized
		 */
		public HTTPHook EMPTY_HOOK = new HTTPHook(){
			@Override
			public void apply(HttpRequestBase httpRequestBase) {}
		};
		
		/**
		 * Hook method where by client code can pass custom http settings
		 * <br />
		 * e.g. Passing http headers. <strong>Content-Type : application/json</strong>
		 * <br />
		 * <pre>
		 * public void apply(HttpRequestBase httpRequestBase) {
		 *	httpRequestBase.setHeader("Content-Type","application/json");				
		 * }
		 * </pre>
		 * 
		 * @param httpRequestBase
		 */
		public void apply(HttpRequestBase httpRequestBase);
	}
	
	/**
	 * Specification for making an HTTP request and returning response
	 * 
	 * @author gaurav
	 *
	 */
	private interface HTTPPerform{
		public HttpResponse apply(HttpRequestBase httpRequestBase) throws IOException;
	}
	
	/**
	 * Template method for making an HTTP request call.
	 * 
	 * <ol>
	 *  <li>First custom settings passed are applied via {@link HTTPHook} provided.</li>
	 *  <li>Request is performed via {@link HTTPPerform} and response returned</li>
	 * </ol>
	 * 
	 * @param httpRequestBase
	 * @param hook
	 * @param perform
	 * @return
	 */
	private HttpResponse httpRequestPerformer(final HttpRequestBase httpRequestBase, HTTPHook hook,HTTPPerform perform) {
		try {

			hook.apply(httpRequestBase);

			return perform.apply(httpRequestBase);

		} catch (IOException e) {
			throw new HttpRequestException(e.getMessage(), e);
		}
	}

}
