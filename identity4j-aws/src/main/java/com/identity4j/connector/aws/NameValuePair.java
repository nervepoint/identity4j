package com.identity4j.connector.aws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import software.amazon.awssdk.utils.StringUtils;

public class NameValuePair {

	private String name;
	private String value;

	public NameValuePair() {

	}
	
	public NameValuePair(String pair) {
		this.name = getNamePairKey(pair);
		this.value = getNamePairValue(pair);
	}

	public NameValuePair(String name, String value) {
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
		return name + "=" + value;
	}

	public static String getNamePairKey(String element) {	
		int idx = element.indexOf('=');
		if(idx > -1) {
			return element.substring(0, idx);
		}
		return element;
	}
	
	public static String getNamePairValue(String element) {	
		int idx = element.indexOf('=');
		if(idx > -1) {
			try {
				return URLDecoder.decode(element.substring(idx+1), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("Unsupported UTF-8 encoding?!?!");
			}
		}
		return "";
	}
	
	public static String implodeNamePairs(Collection<NameValuePair> pairs) {
		StringBuilder buf = new StringBuilder();
		for(NameValuePair pair : pairs) {
			if(buf.length() > 0) {
				buf.append("]|[");
			}
			buf.append(pair.getName());
			buf.append("=");
			buf.append(urlEncode(pair.getValue()));
		}
		return buf.toString();
	}
	
	public static List<NameValuePair> explodeNamePairs(String values) {
		
		String[] pairs = explodeValues(values);
		List<NameValuePair> result = new ArrayList<>();
		for(String pair : pairs) {
			result.add(new NameValuePair(pair));
		}
		return result;
	}
	
	public static String[] explodeValues(String values) {
		if(StringUtils.isBlank(values)) {
			return new String[] { };
		}
		List<String> ret = new ArrayList<>();

		String[] v = values.split("\\]\\|\\[");
		
		for(String val : v){
			StringTokenizer t2 = new StringTokenizer(val, "\r\n");
			while(t2.hasMoreTokens()) {
				ret.add(t2.nextToken());
			}
		}
	
		return ret.toArray(new String[0]);
	}
	
	public static String urlEncode(String message) {
		try {
			return URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}
}
