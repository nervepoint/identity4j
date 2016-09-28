package com.identity4j.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class HttpPair {
	private String name;
	private String value;

	public HttpPair(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		try {
			return URLEncoder.encode(name, "UTF-8") + ( value == null? "" : "=" + URLEncoder.encode(value, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
