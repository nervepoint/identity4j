package com.identity4j.util.http;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
