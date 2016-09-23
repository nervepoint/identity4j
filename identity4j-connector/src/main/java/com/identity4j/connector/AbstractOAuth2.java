package com.identity4j.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Map;

public abstract class AbstractOAuth2<T extends ConnectorConfigurationParameters>
		implements OAuth2<T> {
	private static SecureRandom random = new SecureRandom();

	protected String authorizeUrl;
	protected String clientId;
	protected String redirectUri;
	protected String responseType = "code";

	protected Status status = Status.STARTED;
	protected T configuration;
	protected String id;
	protected String scope;

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String open(T parameters, String returnTo) {
		if(status != Status.STARTED) {
			throw new IllegalStateException("Already used.");
		}
		
		id = generateUID();
		
		
		this.configuration = parameters;
		onOpen(returnTo);
		if (authorizeUrl == null) {
			throw new IllegalStateException("Authorize URL has not been set.");
		}
		try {
			String url = String.format(authorizeUrl);
			url += String.format("?response_type=%s", responseType);
			url += String.format("&client_id=%s",
					URLEncoder.encode(clientId, "UTF-8"));
			url += String.format("&redirect_uri=%s",
					URLEncoder.encode(returnTo, "UTF-8"));
			if(scope != null) {
				url += String.format("&scope=%s", URLEncoder.encode(scope, "UTF-8"));
			}
			String extra = getAdditionalAuthorizedParameters();
			if (extra != null && extra.length() > 0) {
				url += "&" + extra;
			}
			status = Status.OPENED;
			return url;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ReturnStatus validate(Map<String, String[]> returnParameters) throws IOException {
		try {
			if (returnParameters.containsKey("code")) {
				return ReturnStatus.AUTHENTICATED;
			}
			return ReturnStatus.FAILED_TO_AUTHENTICATE;
		} finally {
			status = Status.COMPLETE;
		}
	}

	@Override
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	protected void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}

	protected String getAdditionalAuthorizedParameters() {
		return null;
	}

	protected void onOpen(String returnTo) {
	}

	protected String generateUID() {
		return new BigInteger(130, random).toString(32);
	}
}
