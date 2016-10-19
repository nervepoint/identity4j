package com.identity4j.connector.office365;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.util.MultiMap;

/**
 * Configuration class provides access to properties configured
 * 
 * @author gaurav
 *
 */
public class Office365Configuration extends AbstractConnectorConfiguration{
	
	/**
	 * Active Directory related properties
	 */
	public static final String OFFICE365_REST_SERVICE_HOST = "office365RestServiceHost";
	public static final String OFFICE365_STS_URL = "office365StsUrl";
	public static final String OFFICE365_API_VERSION = "office365ApiVersion";
	public static final String OFFICE365_GRAPH_PRINCIPAL_ID = "office365GraphPrincipalId";
	
	/**
	 * Active Directory OAuth URL related properties
	 */
	public static final String OFFICE365O_AUTH_URL = "office365OAuthUrl";
	public static final String OFFICE365O_AUTH_URL_REDIRECT_URI = "office365OAuthUrlRedirectUri";
	
	/**
	 * Application related properties
	 */
	public static final String OFFICE365_TENANT_DOMAIN_NAME = "office365TenantDomainName";
	public static final String OFFICE365_APP_PRINCIPAL_ID = "office365AppPrincipalId";
	public static final String OFFICE365_APP_PRINCIPAL_OBJECT_ID = "office365AppPrincipalObjectId";
	public static final String OFFICE365_SYMMETRIC_KEY = "office365SymmetricKey";
	/**
	 * Other
	 */
	public static final String OFFICE365_REQUEST_SIZE_LIMIT = "office365RequestSizeLimit";
	
	/**
	 * Property for role having delete rights
	 */
	public static final String OFFICE365_APP_DELETE_PRINCIPAL_ROLE = "office365AppDeletePrincipalRole";


	/**
	 * Configuration property key for excludes
	 */
	public static final String OFFICE365_EXCLUDED_GROUPS = "office365ExcludedGroups";
	/**
	 * Configuration property key for excludes
	 */
	public static final String OFFICE365_INCLUDED_GROUPS = "office365IncludedGroups";
	
	public Office365Configuration(MultiMap configurationParameters) {
		super(configurationParameters);
	}
	
	/**
	 * The protocol that would be used to connect to the WAAD Rest service.
	 */
	public static final String PROTOCOL_NAME = "https";
	
	/**
	 * The authorization header name that would be added in the http request header.
	 */
	public static final String AUTHORIZATION_HEADER = "Authorization";
	

    /**
     * Error occured during generating access tokens.
     */
    public static final String ErrorGeneratingToken = "Could Not Generate Access Token";
    
    /**
     * Error occured during retrieving user details.
     */
    public static final String ErrorRetrievingUserDetails = "Could not retrieve user details";
	
    /**
     * The Error Message that would be shown to the User if ErrorConnectingRestService error is encountered.
     */
    public static final String ErrorGeneratingTokenMessage = "Sorry! Token generation was not successful. Please try again."; 
    /**
     * The Error Message that would be shown to the User if ErrorConnectingRestService error is encountered.
     */
    public static final String ErrorAuthenticatingForToken= "Unauthorized. Please check configration, in particular your key, tenant domain and client ID.";

    /**
     * HTTP header content type
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * HTTP header content type value for json data
     */
    public static final String contentTypeJSON = "application/json";

    
	/**
	 * @return the tenantDomainName
	 */
	public  String getTenantDomainName() {
		return configurationParameters.getString(OFFICE365_TENANT_DOMAIN_NAME);
	}
	
	/**
	 * @return the appPrincipalId
	 */
	public  String getAppPrincipalId() {
		return configurationParameters.getString(OFFICE365_APP_PRINCIPAL_ID);
	}
	
	/**
	 * @return the appPrincipalObjectId
	 */
	public String getAppPrincipalObjectId(){
		return configurationParameters.getString(OFFICE365_APP_PRINCIPAL_OBJECT_ID);
	}
	
	/**
	 * @return the symmetricKey
	 */
	public  String getSymmetricKey() {
		return configurationParameters.getString(OFFICE365_SYMMETRIC_KEY);
	}	
	
	/**
	 * @return the restServiceHost
	 */
	public  String getRestServiceHost() {
		return configurationParameters.getStringOrDefault(OFFICE365_REST_SERVICE_HOST, "graph.windows.net");
	}
	
	
	/**
	 * @return the stsUrl
	 */
	public  String getStsUrl() {
		return configurationParameters.getStringOrDefault(OFFICE365_STS_URL, "https://login.windows.net/%s/oauth2/token");
	}
	
	/**
	 * @return the apiVersion
	 */
	public  String getApiVersion() {
		return configurationParameters.getStringOrDefault(OFFICE365_API_VERSION, "api-version=2013-04-05");
	}
	
	/**
	 * @return the graphPrincipalId
	 */
	public String getGraphPrincipalId(){
		return configurationParameters.getStringOrDefault(OFFICE365_GRAPH_PRINCIPAL_ID, "https://graph.windows.net");
	}
	
	/**
	 * @return the oAuthUrl
	 */
	public String getOAuthUrl(){
		return configurationParameters.getStringOrDefault(OFFICE365O_AUTH_URL, "https://login.windows.net/common/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&resource=%s");
	}
	
	
	/**
	 * @return the office365OAuthUrlRedirectUri
	 */
	public String getOAuthUrlRedirectUri(){
		return configurationParameters.getStringOrDefault(OFFICE365O_AUTH_URL_REDIRECT_URI, "https://localhost");
	}
	
	/**
	 * @return the appDeletePrincipalRole
	 */
	public String getAppDeletePrincipalRole(){
		return configurationParameters.getStringOrDefault(OFFICE365_APP_DELETE_PRINCIPAL_ROLE, "User Account Administrator");
	}
	
	@Override
	public String getUsernameHint() {
		return null;
	}

	@Override
	public String getHostnameHint() {
		return null;
	}
	
	public Set<String> getIncludedGroups() {
		String[] vals = configurationParameters.getStringArrayOrDefault(OFFICE365_INCLUDED_GROUPS);
		return vals.length == 1 && vals[0].equals("") ? new HashSet<String>() : new HashSet<String>(Arrays.asList(vals));
	}
	
	public Set<String> getExcludedGroups() {
		String[] vals = configurationParameters.getStringArrayOrDefault(OFFICE365_EXCLUDED_GROUPS);
		return vals.length == 1 && vals[0].equals("") ? new HashSet<String>() : new HashSet<String>(Arrays.asList(vals));
	}

	/**
	 * @return the symmetricKey
	 */
	public  int getRequestSizeLimit() {
		return configurationParameters.getIntegerOrDefault(OFFICE365_REQUEST_SIZE_LIMIT, 500);
	}	
	
	/**
	 * Set a list of groups to which users must be belong to at least one of to be included in the search. 
	 * If not specified, all groups are included.
	 * 
	 * @param include groups to include
	 */
	public void setIncludedGroups(Set<String> includes) {
		configurationParameters.put(OFFICE365_INCLUDED_GROUPS, includes.toArray(new String[0]));
	}
	
	/**
	 * Set a list of groups to which users must NOT be belong to to be included in the search. 
	 * If not specified, all no groups are excluded.
	 * 
	 * @param exclude groups to exclude
	 */
	public void setExcludedGroups(Set<String> excludes) {
		configurationParameters.put(OFFICE365_EXCLUDED_GROUPS, excludes.toArray(new String[0]));
	}
}
