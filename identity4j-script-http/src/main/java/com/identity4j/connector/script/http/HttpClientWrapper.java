package com.identity4j.connector.script.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
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

	public String toJSON(Object o) {
		return toJSON(o, new StringBuilder()).toString();
	}
	
	@SuppressWarnings("unchecked")
	private StringBuilder toJSON(Object o, StringBuilder b) {
		int i = 0;
		if(o.getClass().getName().equals("jdk.nashorn.api.scripting.ScriptObjectMirror")) {
			try {
				Class<?> clz = Class.forName("jdk.nashorn.api.scripting.ScriptObjectMirror", false, getClass().getClassLoader());
				if((Boolean)clz.getDeclaredMethod("isArray").invoke(o)) {
					o = clz.getDeclaredMethod("values").invoke(o);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		if(o instanceof Map) {
			b.append("{");
			for (Map.Entry<Object, Object> en : ((Map<Object, Object>) o).entrySet()) {
				String key = String.valueOf(en.getKey());
				Object val = en.getValue();
				if(i > 0)
					b.append(",");
				b.append("\"");
				b.append(key);
				b.append("\" : ");
				toJSON(val, b);
				i++;
			}
			b.append("}");
		}
		else if(o instanceof Collection) {
			b.append("[");
			for(Object c : (Collection<Object>)o) {
				if(i > 0)
					b.append(",");
				toJSON(c, b);
			}
			b.append("]");
		}
		else if(o instanceof Boolean) {
			b.append((Boolean)o);
		}
		else if(o instanceof Number) {
			b.append((Number)o);
		}
		else if(o == null) {
			b.append("null");
		} else {
			b.append("\"");
			b.append(String.valueOf(o));
			b.append("\"");
		}
		return b;
	}

	@SuppressWarnings("unchecked")
	public HttpClientResponseWrapper post(String uri, Object parms, Object headers) throws IOException {
		String contentType = "application/x-www-form-urlencoded";

		List<HttpPair> h = new ArrayList<HttpPair>();
		if (headers instanceof Map) {
			for (Map.Entry<Object, Object> en : ((Map<Object, Object>) headers).entrySet()) {
				String key = String.valueOf(en.getKey());
				String val = String.valueOf(en.getValue());
				if (key.equalsIgnoreCase("content-type")) {
					contentType = val;
				}
				h.add(new HttpPair(key, val));
			}
		} else if (headers != null) {
			throw new IllegalArgumentException("Headers when supplied must be java.uti.Map or Javascript object.");
		}
		int idx = contentType.indexOf(";");
		if (idx != -1)
			contentType = contentType.substring(0, idx).trim();

		String content = null;

		List<HttpPair> p = new ArrayList<HttpPair>();
		if (parms instanceof Map) {
			if (contentType.equalsIgnoreCase("application/json") || contentType.equalsIgnoreCase("text/json")) {
				//content = new Gson().toJson(parms);
				content = toJSON(parms);
			} else if (contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
				for (Map.Entry<Object, Object> en : ((Map<Object, Object>) parms).entrySet()) {
					p.add(new HttpPair(String.valueOf(en.getKey()), String.valueOf(en.getValue())));
				}
			} else {
				throw new IllegalArgumentException(
						"jaav.util.Map / Javascript only supported for application/x-www-form-urlencoded and application/json (or text/json) content types.");
			}
		} else {
			content = parms == null ? null : String.valueOf(parms);
		}

		if (content == null)
			return new HttpClientResponseWrapper(client.post(uri, p, h.toArray(new HttpPair[0])));
		else
			return new HttpClientResponseWrapper(client.post(uri, content, h.toArray(new HttpPair[0])));
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
		
		public String statusError() {
			return resp.status().getError();
		}

		public void release() {
			resp.release();
		}
	}
}