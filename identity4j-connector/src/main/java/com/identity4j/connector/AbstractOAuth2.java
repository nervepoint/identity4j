package com.identity4j.connector;

/*
 * #%L
 * Identity4J Connector
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Map;

public abstract class AbstractOAuth2
		implements OAuth2 {
	private static SecureRandom random = new SecureRandom();

	protected String authorizeUrl;
	protected String clientId;
	protected String redirectUri;
	protected String responseType = "code";

	protected Status status = Status.STARTED;
	protected String id;
	protected String scope;
	protected long created = System.currentTimeMillis();

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getCreated() {
		return created;
	}

	@Override
	public String open(String returnTo) {
		if(status != Status.STARTED) {
			throw new IllegalStateException("Already used.");
		}
		
		id = generateUID();
		
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
