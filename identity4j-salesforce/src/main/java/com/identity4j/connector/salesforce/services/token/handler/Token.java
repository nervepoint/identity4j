package com.identity4j.connector.salesforce.services.token.handler;

/*
 * #%L
 * Identity4J Salesforce
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
import java.util.Date;


/**
 * 
 * Represents Salesforce session token.
 * 
 * @author gaurav
 *
 */
public class Token {

	/**
     * Prefix for bearer tokens.
     */
    private static final String bearerTokenPrefix = "Bearer ";
    
	private String sessionId;
	private String userId;
	private String userEmail;
	private Long validSeconds;
	private Date issuedAt;
	private String userName;
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public Long getValidSeconds() {
		return validSeconds;
	}
	public void setValidSeconds(Long validSeconds) {
		this.validSeconds = validSeconds;
	}
	public Date getIssuedAt() {
		return issuedAt;
	}
	public void setIssuedAt(Date issuedAt) {
		this.issuedAt = issuedAt;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Formatted access token prefixed with bearer.
	 * 
	 * @return
	 */
	public String getBearerAccessToken(){
		return String.format("%s%s", bearerTokenPrefix,sessionId);
	}
	
	/**
	 * Utility method to check in how many minutes will the current token expire.
	 * 
	 * @param minutes
	 * @return
	 */
	public boolean willExpireIn(int minutes){
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, minutes);
		long diff = now.getTime().getTime() - issuedAt.getTime();
		long diffSeconds = diff / 1000 % 60;
		return diffSeconds > validSeconds;
	}
	
	
}
