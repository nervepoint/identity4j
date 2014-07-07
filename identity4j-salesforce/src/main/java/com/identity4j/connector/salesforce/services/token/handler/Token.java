package com.identity4j.connector.salesforce.services.token.handler;

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
