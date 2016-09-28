package com.identity4j.connector.script.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;

public class HttpClientWrapper {
	final static Log LOG = LogFactory.getLog(HttpClientWrapper.class);

	HttpProviderClient client;
	HttpConfiguration config;

	public HttpClientWrapper(HttpProviderClient client, HttpConfiguration config) {
		this.client = client;
		this.config = config;
	}

	public HttpClientResponseWrapper get(String uri) throws IOException {
		return new HttpClientResponseWrapper(client.get(uri));
	}

	@SuppressWarnings("unchecked")
	public HttpClientResponseWrapper post(String uri, Object parms) throws IOException {
		List<HttpPair> p = new ArrayList<HttpPair>();
		if (parms instanceof Map) {
			if (parms != null) {
				for (Map.Entry<Object, Object> en : ((Map<Object, Object>) parms).entrySet()) {
					p.add(new HttpPair(String.valueOf(en.getKey()), String.valueOf(en.getValue())));
				}
			}
		}
		return new HttpClientResponseWrapper(client.post(uri, p));
	}

	public class HttpClientResponseWrapper {

		private HttpResponse resp;

		public HttpClientResponseWrapper(HttpResponse resp) {
			this.resp = resp;
		}

		public JsonObject toJSON() throws JsonSyntaxException, UnsupportedEncodingException {
			String str = new String(resp.content(), "UTF-8");
			JsonElement parse = new JsonParser().parse(str);
			JsonObject obj = parse.getAsJsonObject();
			return obj;
		}

		public int status() {
			return resp.status().getCode();
		}

		public void release() {
			resp.release();
		}
	}
}