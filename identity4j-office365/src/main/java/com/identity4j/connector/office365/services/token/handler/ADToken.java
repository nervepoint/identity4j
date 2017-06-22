package com.identity4j.connector.office365.services.token.handler;

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
