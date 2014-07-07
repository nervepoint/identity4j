package com.identity4j.connector.zendesk;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.util.MultiMap;

public class ZendeskConfiguration extends AbstractConnectorConfiguration{
	
	/**
	 * Zendesk Client Application
	 */
	private static final String ZENDESK_CLIENT_ID = "zendeskClientId";
	private static final String ZENDESK_CLIENT_SECRET = "zendeskClientSecret";
	
	/**
	 * Zendesk Admin User
	 */
	private static final String ZENDESK_ADMIN_USER_ID = "zendeskAdminUserId";
	private static final String ZENDESK_ADMIN_PASSWORD = "zendeskAdminUserPassword";
	
	/**
	 * Zendesk oAuth
	 */
	private static final String ZENDESK_OAUTH_URL = "zendeskOAuthUrl";
	private static final String ZENDESK_SCOPE = "zendeskScope";
	private static final String ZENDESK_OAUTH_PASSWORD_ACCESS_JSON = "zendeskOAuthPasswordAccessJSON";
	private static final String ZENDESK_OAUTH_TOKEN_VALID_MINUTES = "zendeskOAuthTokenValidMinutes";
	
	/**
	 * Zendesk REST API related properties.
	 */
	private static final String ZENDESK_REST_PATH = "zendeskRestPath";
	private static final String ZENDESK_REST_HOST = "zendeskRestHost";
	private static final String ZENDESK_REST_API_VERSION = "zendeskRestApiVersion";
	
	/**
	 * Zendesk Sub Domain
	 */
	private static final String ZENDESK_SUB_DOMAIN = "zendeskSubDomain";
	
	/**
	 * The authorization header name that would be added in the http request header.
	 */
	public static final String AUTHORIZATION_HEADER = "Authorization";
	
	
	/**
	 * The authorization header name that would be added in the http request header.
	 */
	public static final String PROTOCOL_NAME = "https";
	
	/**
     * HTTP header content type
     */
    public static final String CONTENT_TYPE = "Content-Type";
    
    /**
     * HTTP header content type
     */
    public static final String ACCEPT = "Accept";
    
    /**
     * HTTP header content type value for json data
     */
    public static final String contentTypeJSON = "application/json";
    
    
    /**
     * HTTP method POST
     */
    public static final String POST = "POST";
    

	public ZendeskConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	@Override
	public String getUsernameHint() {
		return null;
	}

	@Override
	public String getHostnameHint() {
		return null;
	}
	
	/**
	 * @return the zendeskClientId
	 */
	public String getClientId(){
		return configurationParameters.getString(ZENDESK_CLIENT_ID);
	}
	
	/**
	 * @return the zendeskClientSecret
	 */
	public String getClientSecret(){
		return configurationParameters.getString(ZENDESK_CLIENT_SECRET);
	}
	
	/**
	 * @return the zendeskAdminUserId
	 */
	public String getAdminUserId(){
		return configurationParameters.getString(ZENDESK_ADMIN_USER_ID);
	}
	
	/**
	 * @return the zendeskAdminUserPassword
	 */
	public String getAdminUserPassword(){
		return configurationParameters.getString(ZENDESK_ADMIN_PASSWORD);
	}
	
	/**
	 * @return the zendeskOAuthUrl
	 */
	public String getOAuthUrl(){
		return configurationParameters.getStringOrDefault(ZENDESK_OAUTH_URL,"https://%s.zendesk.com/oauth/tokens");
	}
	
	/**
	 * @return the zendeskScope
	 */
	public String getoAuthScope(){
		return configurationParameters.getStringOrDefault(ZENDESK_SCOPE,"read%20write");
	}
	
	/**
	 * @return the zendeskSubDomain
	 */
	public String getSubDomain(){
		return configurationParameters.getString(ZENDESK_SUB_DOMAIN);
	}
	
	/**
	 * @return the zendeskOAuthPasswordAccessJSON
	 */
	public String getOAuthPasswordAccessJSON(){
		return configurationParameters.getStringOrDefault(ZENDESK_OAUTH_PASSWORD_ACCESS_JSON,
				"{\"grant_type\": \"password\", \"client_id\": \"%s\",\"client_secret\": \"%s\","
				+ " \"scope\": \"%s\",\"username\": \"%s\", \"password\": \"%s\"}");
	}
	
	/**
	 * @return the zendeskOAuthTokenValidMinutes
	 */
	public String getOAuthTokenValidMinutes(){
		return configurationParameters.getStringOrDefault(ZENDESK_OAUTH_TOKEN_VALID_MINUTES,"90");
	}
	
	/**
	 * @return the zendeskRestPath
	 */
	public String getRestPath(){
		return configurationParameters.getStringOrDefault(ZENDESK_REST_PATH,"/api/v2/%s");
	}
	
	/**
	 * @return the zendeskRestHost
	 */
	public String getRestHost(){
		return configurationParameters.getStringOrDefault(ZENDESK_REST_HOST,"%s.zendesk.com");
	}
	
	/**
	 * @return the zendeskRestApiVersion
	 */
	public String getRestApiVersion(){
		return configurationParameters.getString(ZENDESK_REST_API_VERSION);
	}
	
}
