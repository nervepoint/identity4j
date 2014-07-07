package com.identity4j.connector.zendesk.services.token.handler;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * 
 * Represents Zendesk session token.
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Token {

	/**
     * Prefix for bearer tokens.
     */
    private static final String bearerTokenPrefix = "Bearer ";
    
    @JsonProperty("access_token")
	private String accessToken;
    @JsonProperty("token_type")
	private String tokenType;
	private String scope;
	/**
	 * There is no information of creation and expiration sent by Zendesk,
	 * On creation we note the time of creation and latter compute expiration
	 */
	@JsonIgnore
	private Date issuedAt;
	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public Date getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(Date issuedAt) {
		this.issuedAt = issuedAt;
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
	 * Utility method to check in how many minutes have passed since the creation of token,
	 * accordingly decision can be made to re fetch fresh token.
	 * 
	 * @param minutes
	 * @return
	 */
	public boolean hasPassed(int minutes){
		Date now = new Date();
		long diff = now.getTime() - issuedAt.getTime();
		long diffMinutes = (diff / 1000 % 60)/60;
		return diffMinutes > minutes;
	}
}
