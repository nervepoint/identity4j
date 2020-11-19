package com.identity4j.connector.script.hypersocket;

import com.identity4j.connector.script.http.HttpConfiguration;
import com.identity4j.util.MultiMap;

public class HypersocketConfiguration extends HttpConfiguration {

	public static final String HTTP_LOGON_SCHEME_URI = "hypersocket.logonSchemeUri";

	public HypersocketConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	public final String getLogonSchemeUri() {
		return getConfigurationParameters().getStringOrDefault(HTTP_LOGON_SCHEME_URI, "api/logon/basic");
	}
}
