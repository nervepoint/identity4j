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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.salesforce.SalesforceConfiguration;
import com.identity4j.util.StringUtil;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.xml.XMLDataExtractor;
import com.identity4j.util.xml.XMLDataExtractor.Node;

/**
 * This class is responsible for handling authentication/authorization related activities.
 * 
 * @author gaurav
 *
 */
public class SalesforceAuthorizationHelper {

	private SalesforceAuthorizationHelper(){}
	
	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final SalesforceAuthorizationHelper INSTANCE = new SalesforceAuthorizationHelper();
	}
 
	public static SalesforceAuthorizationHelper getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	private Boolean ipRangeOrAppIpLessRestrictive;
	private String loginSoapEnvelopTemplate;
	private String loginSoapUrl;
	private String version;
	
	/**
	 * <p>
	 * Calls SOAP Login API to extract Session Token, which is used in subsequent calls for authorization.
	 * <br/>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_asynch/Content/asynch_api_quickstart_login.htm">Salesforce SOAP Login API</a> for more details.
	 * </p> 
	 * <p>
	 * This call assumes IP of server running application is in trusted list in Salesforce Admin Console and hence
	 * does not takes secret key as the parameter.
	 * </p>
	 * 
	 * @param userId
	 * @param userPassword
	 * @return Token
	 * @throws IOException
	 */
	public Token login(String userId,String userPassword) throws IOException{
		URL url = new URL(String.format(loginSoapUrl, version));
		String data = String.format(loginSoapEnvelopTemplate, userId,userPassword);
		return tokenFetcher(url, data);
	}
	
	/**
	 * <p>
	 * Calls SOAP Login API to extract Session Token, which is used in subsequent calls for authorization.
	 * <br/>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_asynch/Content/asynch_api_quickstart_login.htm">Salesforce SOAP Login API</a> for more details.
	 * </p> 
	 * <p>
	 * This call does not assumes IP of server running application is in trusted list in Salesforce Admin Console and hence
	 * expects secret key as the parameter.
	 * </p>
	 * 
	 * @param userId
	 * @param userPassword
	 * @param userSecretKey
	 * @return Token instance
	 * @throws IOException
	 */
	public Token login(String userId,String userPassword,String userSecretKey) throws IOException{
		URL url = new URL(String.format(loginSoapUrl, version));
		String data = String.format(loginSoapEnvelopTemplate, userId,probeFinalPassword(userPassword, userSecretKey));
		return tokenFetcher(url, data);
	}
	
	/**
	 * Helper utility function that makes SOAP LOgin API call with XML data and parses response XML to return Token instance.
	 * 
	 * @param url
	 * @param data
	 * @return Token instance
	 * @throws IOException
	 */
	private Token tokenFetcher(URL url,String data) throws IOException{
		try {
			HttpProviderClient client = Http.getProvider().getClient(url.toExternalForm(), null, null, null);
			client.setConnectTimeout(60000);
			HttpResponse resp = client.post(null,
					Arrays.asList(new HttpPair("grant_type", "authorization_code")),
					new HttpPair(SalesforceConfiguration.CONTENT_TYPE, SalesforceConfiguration.contentTypeXML),
					new HttpPair(SalesforceConfiguration.SOAP_ACTION, SalesforceConfiguration.soapActionLogin));
			try {
				Token token = new Token();
				Map<String, Node> tokenNodes = XMLDataExtractor.getInstance()
						.extract(
								resp.contentString(),
								new HashSet<String>(Arrays.asList("sessionId","userName",
										"userEmail", "userId", "sessionSecondsValid")));
				if(tokenNodes.get("sessionId") == null){
					throw new IllegalStateException("Session id not found");
				}
				token.setIssuedAt(new Date());
				token.setUserName(tokenNodes.get("userName").getNodeValue());
				token.setSessionId(tokenNodes.get("sessionId").getNodeValue());
				token.setUserEmail(tokenNodes.get("userEmail").getNodeValue());
				token.setUserId(tokenNodes.get("userId").getNodeValue());
				token.setValidSeconds(Long.parseLong(tokenNodes.get("sessionSecondsValid").getNodeValue()));
				
				return token;
			} finally {
				resp.release();
			}
			

		} catch (Exception e) {
			throw new ConnectorException("Error generating token.", e);
		} 
	}
	
	/**
	 * <p>
	 * Salesforce API requires to append secret key along with password if request is made from an
	 * IP which is not in the trusted range list set in setup console.
	 * <br />
	 * Here we check if in configuration <b>salesforceIpRangeOrAppIpLessRestrictive</b> hint is given whether
	 * our application machine IP is in trusted range list or not, accordingly we append the secret key. 
	 * </p>
	 * @return appended password and secret key according to IP range setting in console.
	 * @throws UnsupportedEncodingException
	 * @throws IllegalStateException if IP range hint not set or IP is not in range list and secret key is not provided.
	 */
	private String probeFinalPassword(String adminPassword,String adminSecretKey) throws UnsupportedEncodingException {
		if(ipRangeOrAppIpLessRestrictive == null || (!ipRangeOrAppIpLessRestrictive && StringUtil.isNullOrEmpty(adminSecretKey)))
			throw new IllegalStateException("IP range hint not set or IP is not in range list and secret key is not provided, call will fail.");
		return ipRangeOrAppIpLessRestrictive ? 
				adminPassword : 
				adminPassword + adminSecretKey; 
	}
	
	public Boolean getIpRangeOrAppIpLessRestrictive() {
		return ipRangeOrAppIpLessRestrictive;
	}


	public SalesforceAuthorizationHelper setIpRangeOrAppIpLessRestrictive(
			Boolean ipRangeOrAppIpLessRestrictive) {
		this.ipRangeOrAppIpLessRestrictive = ipRangeOrAppIpLessRestrictive;
		return this;
	}

	public String getLoginSoapEnvelopTemplate() {
		return loginSoapEnvelopTemplate;
	}

	public SalesforceAuthorizationHelper setLoginSoapEnvelopTemplate(String loginSoapEnvelopTemplate) {
		this.loginSoapEnvelopTemplate = loginSoapEnvelopTemplate;
		return this;
	}

	public String getLoginSoapUrl() {
		return loginSoapUrl;
	}

	public SalesforceAuthorizationHelper setLoginSoapUrl(String loginSoapUrl) {
		this.loginSoapUrl = loginSoapUrl;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public SalesforceAuthorizationHelper setVersion(String version) {
		this.version = version;
		return this;
	}
}
