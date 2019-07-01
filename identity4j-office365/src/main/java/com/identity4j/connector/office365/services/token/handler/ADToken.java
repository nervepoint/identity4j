package com.identity4j.connector.office365.services.token.handler;

/*
 * #%L
 * Identity4J OFFICE 365
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

import java.util.Calendar;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents Azure Active Directory JSON Web Token.
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ADToken {

	/**
	 * Prefix for bearer tokens.
	 */
	private static final String bearerTokenPrefix = "Bearer ";


    @JsonProperty("token_type")
	private String tokenType;
	private String accessToken;
	private Long notBefore;
	private Long expiresOn;
	private Long expiresIn;
	private String resource;
	private String scope;
	private String refreshToken;
	private String idToken;

	public void from(ADToken aadjwtToken) {
		scope = aadjwtToken.scope;
		refreshToken = aadjwtToken.refreshToken;
		idToken = aadjwtToken.idToken;
		tokenType = aadjwtToken.tokenType;
		expiresOn = aadjwtToken.expiresOn;
		expiresIn = aadjwtToken.expiresIn;
		resource = aadjwtToken.resource;
		scope = aadjwtToken.scope;
		notBefore = aadjwtToken.notBefore;
	}
	
	public void recalcExpiresOn() {
		/* Ensure the expires on is local time by basing it on the current local time
		 * and the expires in value
		 */
		expiresOn = ( System.currentTimeMillis() / 1000 ) + expiresIn;
	}

    @JsonProperty("scope")
	public String getScope() {
		return scope;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

    @JsonProperty("refresh_token")
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

    @JsonProperty("id_token")
	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

    @JsonProperty("token_type")
	public String getTokenType() {
		return tokenType;
	}


    @JsonProperty("token_type")
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

    @JsonProperty("access_token")
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

    @JsonProperty("not_before")
	public Long getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Long notBefore) {
		this.notBefore = notBefore;
	}

    @JsonProperty("expires_on")
	public Long getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(Long expiresOn) {
		this.expiresOn = expiresOn;
	}

    @JsonProperty("expires_in")
	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

    @JsonProperty("resource")
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * Formatted access token prefixed with bearer.
	 * 
	 * @return
	 */
	public String getBearerAccessToken() {
		return String.format("%s%s", bearerTokenPrefix, accessToken);
	}

	/**
	 * Utility method to check in how many minutes will the current token
	 * expire.
	 * 
	 * @param minutes
	 * @return
	 */
	public boolean willExpireIn(int minutes) {
		Calendar target = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		target.add(Calendar.MINUTE, minutes);
		Long targetMillis = target.getTimeInMillis();

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(1970, 00, 01);
		Long epochMillis = cal.getTimeInMillis();

		return ((targetMillis - epochMillis) / 1000) > expiresOn;
	}

	@Override
	public String toString() {
		return "AADJWTToken [tokenType=" + tokenType + ", accessToken="
				+ accessToken + ", notBefore=" + notBefore + ", expiresOn="
				+ expiresOn + ", expiresIn=" + expiresIn + ", resource="
				+ resource + ", scope=" + scope + "]";
	}
}
