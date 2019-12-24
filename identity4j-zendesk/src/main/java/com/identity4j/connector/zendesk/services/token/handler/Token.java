package com.identity4j.connector.zendesk.services.token.handler;

/*
 * #%L
 * Identity4J Zendesk
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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


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
