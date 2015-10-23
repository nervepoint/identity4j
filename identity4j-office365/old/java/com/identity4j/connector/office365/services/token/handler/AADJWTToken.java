package com.identity4j.connector.office365.services.token.handler;

import java.util.Calendar;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents Azure Active Directory JSON Web Token.
 * 
 * @author gaurav
 *
 */
public class AADJWTToken {
	
	/**
     * Prefix for bearer tokens.
     */
    private static final String bearerTokenPrefix = "Bearer ";

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("not_before")
	private Long notBefore;

	@JsonProperty("expires_on")
	private Long expiresOn;

	@JsonProperty("expires_in")
	private Long expiresIn;

	@JsonProperty("resource")
	private String resource;

	@JsonProperty("scope")
	private String scope;

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Long getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Long notBefore) {
		this.notBefore = notBefore;
	}

	public Long getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(Long expiresOn) {
		this.expiresOn = expiresOn;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

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
	public String getBearerAccessToken(){
		return String.format("%s%s", bearerTokenPrefix,accessToken);
	}
	
	/**
	 * Utility method to check in how many minutes will the current token expire.
	 * 
	 * @param minutes
	 * @return
	 */
	public boolean willExpireIn(int minutes){
		Calendar target = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		target.add(Calendar.MINUTE, minutes);
		Long targetMillis = target.getTimeInMillis();
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));      
	    cal.set(1970, 00, 01);
	    Long epochMillis = cal.getTimeInMillis();
		
		return ((targetMillis - epochMillis)/1000) > expiresOn; 
	}

	@Override
	public String toString() {
		return "AADJWTToken [tokenType=" + tokenType + ", accessToken="
				+ accessToken + ", notBefore=" + notBefore + ", expiresOn="
				+ expiresOn + ", expiresIn=" + expiresIn + ", resource="
				+ resource + "]";
	}
	
	
	
}
