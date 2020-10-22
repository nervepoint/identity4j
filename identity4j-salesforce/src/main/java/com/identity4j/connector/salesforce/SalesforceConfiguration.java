package com.identity4j.connector.salesforce;

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

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.Connector;
import com.identity4j.util.MultiMap;

/**
 * Configuration class provides access to properties configured
 * 
 * @author gaurav
 *
 */
public class SalesforceConfiguration extends AbstractConnectorConfiguration{

	/**
	 * Salesforce SOQL queries
	 */
	private static final String SALESFORCE_GET_GROUP_MEMBERS_FOR_USER = "salesforceGetGroupMembersForUser";
	private static final String SALESFORCE_GET_GROUP_MEMBERS_FOR_USER_AND_GROUP = "salesforceGetGroupMembersForUserAndGroup";
	private static final String SALESFORCE_GET_ALL_GROUPS = "salesforceGetAllGroups";
	private static final String SALESFORCE_GET_BY_NAME_GROUP_QUERY = "salesforceGetByNameGroupQuery";
	private static final String SALESFORCE_GET_ALL_USERS = "salesforceGetAllUsers";
	private static final String SALESFORCE_GET_BY_NAME_USER_QUERY = "salesforceGetByNameUserQuery";
	
	/**
	 * User mandatory fields values, is used by each user entry
	 */
	private static final String SALESFORCE_PROFILE_ID = "salesforceProfileId";
	private static final String SALESFORCE_LANGUAGE_LOCALE_KEY = "salesforceLanguageLocaleKey";
	private static final String SALESFORCE_EMAIL_ENCODING_KEY = "salesforceEmailEncodingKey";
	private static final String SALESFORCE_LOCALE_SID_KEY = "salesforceLocaleSidKey";
	private static final String SALESFORCE_TIME_ZONE_SID_KEY = "salesforceTimeZoneSidKey";
	
	
	/**
	 * REST API related properties.
	 */
	private static final String SALESFORCE_REST_SOQL_QUERY_PATH = "salesforceRestSOQLQueryPath";
	private static final String SALESFORCE_REST_PATH = "salesforceRestPath";
	private static final String SALESFORCE_REST_HOST = "salesforceRestHost";
	private static final String SALESFORCE_REST_API_VERSION = "salesforceRestApiVersion";
	
	/**
	 * IP range property, is application IP listed in white list IP range in Salesforce console.
	 */
	private static final String SALESFORCE_IP_RANGE_OR_APP_IP_LESS_RESTRICTIVE = "salesforceIpRangeOrAppIpLessRestrictive";
	
	/**
	 * Admin user related properties
	 */
	private static final String SALESFORCE_ADMIN_SECRET_KEY = "salesforceAdminSecretKey";
	private static final String SALESFORCE_ADMIN_PASSWORD = "salesforceAdminPassword";
	private static final String SALESFORCE_ADMIN_ID = "salesforceAdminId";
	
	/**
	 * Salesforce SOAP properties
	 */
	private static final String SALESFORCE_LOGIN_SOAP_ENVELOP_TEMPLATE = "salesforceLoginSoapEnvelopTemplate";
	private static final String SALESFORCE_LOGIN_URL = "salesforceLoginSoapUrl";
	
	
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
     * HTTP header salesforce soap action
     */
    public static final String SOAP_ACTION = "SOAPAction";

    /**
     * HTTP header content type value for json data
     */
    public static final String contentTypeJSON = "application/json";
    
    /**
     * HTTP header content type value for xml data
     */
    public static final String contentTypeXML = "text/xml; charset=UTF-8";
    
    /**
     * HTTP header salesforce soap action value
     */
    public static final String soapActionLogin = "login";
    
    /**
     * Soap envelop for login
     */
    private static final String SOAP_ENVELOP_XML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
										    		+ "<env:Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"    "
										    		+ 	"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"    "
										    		+ 	"xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">"
										    		+ 	"<env:Body>"
										    		+ 		"<n1:login xmlns:n1=\"urn:partner.soap.sforce.com\">"
										    		+ 			"<n1:username>%s</n1:username>"
										    		+ 			"<n1:password>%s</n1:password>"
										    		+ 		"</n1:login>"
										    		+ 	"</env:Body>"
										    		+ "</env:Envelope>";    
    	
    public SalesforceConfiguration(MultiMap configurationParameters) {
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
	 * @return the salesforceAdminId
	 */
	public String getAdminId(){
		return configurationParameters.getString(SALESFORCE_ADMIN_ID);
	}
	
	
	/**
	 * @return the salesforceAdminPassword
	 */
	public String getAdminPassword(){
		return configurationParameters.getString(SALESFORCE_ADMIN_PASSWORD);
	}
	
	
	/**
	 * @return the salesforceAdminPassword
	 */
	public String getAdminSecretKey(){
		return configurationParameters.getString(SALESFORCE_ADMIN_SECRET_KEY);
	}
	
	
	/**
	 * @return the salesforceIpRangeOrAppIpLessRestrictive
	 */
	public Boolean getIpRangeOrAppIpLessRestrictive(){
		return configurationParameters.getString(SALESFORCE_IP_RANGE_OR_APP_IP_LESS_RESTRICTIVE) == null ? false : Boolean.valueOf(configurationParameters.getString(SALESFORCE_IP_RANGE_OR_APP_IP_LESS_RESTRICTIVE));
	}
	
	
	/**
	 * @return the salesforceRestApiVersion
	 */
	public String getRestApiVersion(){
		return configurationParameters.getString(SALESFORCE_REST_API_VERSION);
	}
	
	/**
	 * @return the salesforceRestHost
	 */
	public String getRestHost(){
		return configurationParameters.getString(SALESFORCE_REST_HOST);
	}
	
	/**
	 * @return the salesforceRestPath
	 */
	public String getRestPath(){
		return configurationParameters.getStringOrDefault(SALESFORCE_REST_PATH,"/services/data/v%s/sobjects/%s");
	}
	
	/**
	 * @return the salesforceRestSOQLQueryPath
	 */
	public String getRestSOQLQueryPath(){
		return configurationParameters.getStringOrDefault(SALESFORCE_REST_SOQL_QUERY_PATH,"/services/data/v%s/query");
	}
	
	/**
	 * @return the salesforceTimeZoneSidKey
	 */
	public String getTimeZoneSidKey(){
		return configurationParameters.getStringOrDefault(SALESFORCE_TIME_ZONE_SID_KEY,"America/Los_Angeles");
	}
	
	/**
	 * @return the salesforceLocaleSidKey
	 */
	public String getLocaleSidKey(){
		return configurationParameters.getStringOrDefault(SALESFORCE_LOCALE_SID_KEY,"en_US");
	}
	
	/**
	 * @return the salesforceEmailEncodingKey
	 */
	public String getEmailEncodingKey(){
		return configurationParameters.getStringOrDefault(SALESFORCE_EMAIL_ENCODING_KEY,"ISO-8859-1");
	}
	
	/**
	 * @return the salesforceLanguageLocaleKey
	 */
	public String getLanguageLocaleKey(){
		return configurationParameters.getStringOrDefault(SALESFORCE_LANGUAGE_LOCALE_KEY,"en_US");
	}
	
	/**
	 * @return the salesforceProfileId
	 */
	public String getProfileId(){
		return configurationParameters.getString(SALESFORCE_PROFILE_ID);
	}
	
	/**
	 * @return the salesforceLoginSoapEnvelopTemplate
	 */
	public String getLoginSoapEnvelopTemplate(){
		return configurationParameters.getStringOrDefault(SALESFORCE_LOGIN_SOAP_ENVELOP_TEMPLATE,SOAP_ENVELOP_XML);
	}
	
	/**
	 * @return the salesforceLoginSoapUrl
	 */
	public String getLoginSoapUrl(){
		return configurationParameters.getStringOrDefault(SALESFORCE_LOGIN_URL,"https://login.salesforce.com/services/Soap/u/%s");
	}
	
	
	/**
	 * @return the salesforceGetByNameUserQuery
	 */
	public String getGetByNameUserQuery(){
		return configurationParameters.getStringOrDefault(SALESFORCE_GET_BY_NAME_USER_QUERY,"q=SELECT %s from User where UserName = '%s'");
	}
	
	/**
	 * @return the salesforceGetAllUsers
	 */
	public String getGetAllUsers(){
		return configurationParameters.getStringOrDefault(SALESFORCE_GET_ALL_USERS,"q=SELECT %s from User");
	}
	
	/**
	 * @return the salesforceGetByNameGroupQuery
	 */
	public String getGetByNameGroupQuery(){
		return configurationParameters.getStringOrDefault(SALESFORCE_GET_BY_NAME_GROUP_QUERY,"q=SELECT %s from Group where Name = '%s'");
	}
	
	/**
	 * @return the salesforceGetAllGroups
	 */
	public String getGetAllGroups(){
		return configurationParameters.getStringOrDefault(SALESFORCE_GET_ALL_GROUPS,"q=SELECT %s from Group");
	}
	
	/**
	 * @return the salesforceGetGroupMembersForUserAndGroup
	 */
	public String getGetGroupMembersForUserAndGroup(){
		return configurationParameters.getStringOrDefault(SALESFORCE_GET_GROUP_MEMBERS_FOR_USER_AND_GROUP,"q=SELECT %s from GroupMember where UserOrGroupId = '%s' and GroupId = '%s'");
	}
	
	/**
	 * @return the salesforceGetGroupMembersForUser
	 */
	public String getGetGroupMembersForUser(){
		return configurationParameters.getStringOrDefault(SALESFORCE_GET_GROUP_MEMBERS_FOR_USER,"q=SELECT %s from GroupMember where UserOrGroupId = '%s'");
	}

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return SalesforceConnector.class;
	}
}
