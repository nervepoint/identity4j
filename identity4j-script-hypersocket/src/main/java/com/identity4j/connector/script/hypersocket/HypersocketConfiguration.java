package com.identity4j.connector.script.hypersocket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.identity4j.connector.script.http.HttpConfiguration;
import com.identity4j.util.MultiMap;

public class HypersocketConfiguration extends HttpConfiguration {

	public static final String HTTP_USE_LOCAL_API = "hypersocket.useLocalApi";
	public static final String HTTP_LOCAL_API_COOKIE = "hypersocket.localApiCookie";
	public static final String HTTP_LOCAL_API_COOKIE_FILE = "hypersocket.localApiCookieFile";

	public HypersocketConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	public final String getLogonSchemeUri() {
		if(isUseLocalApi())
			return "api/logon/localApi?authkey=" + getAuth();
		else
			return "api/logon/basic";
	}

	public final boolean isUseLocalApi() {
		return getConfigurationParameters().getBooleanOrDefault(HTTP_USE_LOCAL_API, false);
	}

	public final String getLocalApiCookie() {
		return getConfigurationParameters().getStringOrDefault(HTTP_LOCAL_API_COOKIE, null);
	}

	public final String getLocalApiCookieFile() {
		return getConfigurationParameters().getStringOrDefault(HTTP_LOCAL_API_COOKIE_FILE, null);
	}
	
	protected String getAuth() {
		String cookie = getLocalApiCookie();
		if(cookie == null) {
			String file = getLocalApiCookieFile();
			if(file == null || file.length() == 0)
				throw new IllegalStateException(String.format("If %s is set, then either %s or %s must be set too.",HTTP_USE_LOCAL_API, HTTP_LOCAL_API_COOKIE, HTTP_LOCAL_API_COOKIE_FILE));
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				try {
					String line = reader.readLine().trim();
					if(line == null || line.length() == 0)
						throw new IllegalStateException(String.format("ocal api cookie file %s exists, but contains no content. ", file));
					return line;
				}
				finally {
					reader.close();
				}
			}
			catch(IOException ioe) {
				throw new IllegalStateException(String.format("Failed to read local api cookie file %s. ", file), ioe);
			}
		}
		else
			return cookie;
			
	}
}
