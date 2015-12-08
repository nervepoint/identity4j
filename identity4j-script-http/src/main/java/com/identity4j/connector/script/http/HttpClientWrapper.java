package com.identity4j.connector.script.http;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class HttpClientWrapper {
	final static Log LOG = LogFactory.getLog(HttpClientWrapper.class);

	HttpClient client;
	HttpConfiguration config;

	public HttpClientWrapper(HttpClient client, HttpConfiguration config) {
		this.client = client;
		this.config = config;
	}

	public HttpClientResponseWrapper get(String uri) throws URIException {
		final GetMethod method = new GetMethod(HttpUtil.concatenateUriParts(config.getUrl(), uri));
		LOG.info(String.format("HTTP GET %s", method.getURI().toString()));
		HttpClientResponseWrapper response = new HttpClientResponseWrapper() {
			@Override
			public void release() {
				method.releaseConnection();
			}
		};
		try {
			response.status = client.executeMethod(method);
			response.data = method.getResponseBody();
			return response;
		} catch (Exception e) {
			LOG.error("Failed to retrieve HTTP resource.", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public HttpClientResponseWrapper post(String uri, Object parms) throws URIException {
		final PostMethod method = new PostMethod(HttpUtil.concatenateUriParts(config.getUrl(), uri));
		LOG.info(String.format("HTTP POST %s", method.getURI().toString()));
		if (parms instanceof Map) {
			if (parms != null) {
				for (Map.Entry<Object, Object> en : ((Map<Object, Object>)parms).entrySet()) {
					method.addParameter(String.valueOf(en.getKey()), String.valueOf(en.getValue()));
				}
			}
		}
		HttpClientResponseWrapper response = new HttpClientResponseWrapper() {
			@Override
			public void release() {
				method.releaseConnection();
			}
		};
		try {
			response.status = client.executeMethod(method);
			response.data = method.getResponseBody();
			return response;
		} catch (Exception e) {
			LOG.error("Failed to retrieve HTTP resource.", e);
		}
		return null;
	}

	public abstract class HttpClientResponseWrapper {

		public int status;
		byte[] data;

		public JsonObject toJSON() throws JsonSyntaxException, UnsupportedEncodingException {
			String str = new String(data, "UTF-8");
			JsonElement parse = new JsonParser().parse(str);
			JsonObject obj = parse.getAsJsonObject();
			return obj;
		}

		public int status() {
			return status;
		}

		public abstract void release();
	}
}