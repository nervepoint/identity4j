package com.identity4j.util.http;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Http {
	
	private static HttpProvider defaultProvider;
	private static ThreadLocal<HttpProvider> threadProvider = new ThreadLocal<HttpProvider>();
	
	public static void setProvider(HttpProvider provider) {
		Http.defaultProvider = provider;
	}
	
	public static void setThreadContextProvider(HttpProvider provider) {
		threadProvider.set(provider);
	}

	public static HttpProvider getProvider() {
		HttpProvider p = threadProvider.get();		
		if(p == null) {
			p = defaultProvider;
			if(p == null) {
				ServiceLoader<HttpProvider> l = ServiceLoader.load(HttpProvider.class);
				Iterator<HttpProvider> iterator = l.iterator();
				if(iterator.hasNext())
					p = iterator.next();
				else
					throw new RuntimeException("Could not find any HTTP providers on the classpath. Unless "
							+ "you intended to provide your own, you should add the identity4j-http module."
							);
			}
		}
		return p;
	}

	public static String getContentType(HttpResponse resp) {
		HttpPair header = getHeader(resp, "content-type");
		return header == null ? null : header.getValue();
	}

	public static String getMIMEType(HttpResponse resp) {
		return getContentType(resp).split(";")[0].trim();
	}

	public static String getCharset(HttpResponse resp) {
		String[] sp = getContentType(resp).split(";");
		for(int i=1;i<sp.length;i++) {
			String n = sp[i].trim();
			String v = null;
			int idx = n.indexOf('=');
			if(idx != -1) {
				v = n.substring(idx + 1);
				n = n.substring(0, idx);
			}
			if(n.equals("charset"))
				return v;
		}
		return null;
	}
	
	public static String getContentEncoding(HttpResponse resp) {
		HttpPair header = getHeader(resp, "content-encoding");
		return header == null ? null : header.getValue();
	}
	
	public static long getContentLength(HttpResponse resp) {
		HttpPair header = getHeader(resp, "content-length");
		return header == null ? -1 : Long.parseLong(header.getValue());
	}
	
	public static HttpPair getHeader(HttpResponse resp, String name) {
		for(HttpPair p : resp.headers()) {
			if(p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}

	public static String encodeParameters(HttpPair... params) {
		StringBuilder bui = new StringBuilder();
		for(HttpPair p : params) {
			if(bui.length() > 0)
				bui.append("&");
			bui.append(p.toString());
		}
		return bui.toString();
	}
}
