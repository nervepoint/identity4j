package com.identity4j.connector.zendesk.services.token.handler;

import java.io.IOException;
import java.util.Date;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.json.JsonMapperService;


/**
 * This class is responsible for handling authentication/authorization related activities.
 * 
 * @author gaurav
 *
 */
public class ZendeskAuthorizationHelper {

	private String clientId;
	private String clientSecret;
	private String oAuthUrl;
	private String subDomain;
	private String passwordAccessJSON;
	private String scope;
	
	private ZendeskAuthorizationHelper(){}
	
	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final ZendeskAuthorizationHelper INSTANCE = new ZendeskAuthorizationHelper();
	}
 
	public static ZendeskAuthorizationHelper getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	/**
	 * Retrieves Json Token which is used for authorization of REST API calls.
	 * 
	 * @return Token
	 * 
	 * @throws IOException
	 */
	public Token getOAuthAccessToken(String userName,String password) throws IOException  {
		
		try {
			HttpProviderClient client = Http.getProvider().getClient(String.format(oAuthUrl, subDomain), null, null, null);
			client.setConnectTimeout(60000);
			HttpResponse resp = client.post(null,
					String.format(passwordAccessJSON, clientId,clientSecret,scope,userName,password),
					new HttpPair(ZendeskConfiguration.CONTENT_TYPE, ZendeskConfiguration.contentTypeJSON));
			try {
				Token token =  JsonMapperService.getInstance().getObject(Token.class, resp.contentString());
				token.setIssuedAt(new Date());
				return token;
			} finally {
				resp.release();
			}
			

		} catch (Exception e) {
			throw new ConnectorException("Error generating token.", e);
		} 
			
	}

	public String getClientId() {
		return clientId;
	}

	public ZendeskAuthorizationHelper setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public ZendeskAuthorizationHelper setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		return this;
	}

	public String getoAuthUrl() {
		return oAuthUrl;
	}

	public ZendeskAuthorizationHelper setoAuthUrl(String oAuthUrl) {
		this.oAuthUrl = oAuthUrl;
		return this;
	}

	public String getSubDomain() {
		return subDomain;
	}

	public ZendeskAuthorizationHelper setSubDomain(String subDomain) {
		this.subDomain = subDomain;
		return this;
	}

	public String getPasswordAccessJSON() {
		return passwordAccessJSON;
	}

	public ZendeskAuthorizationHelper setPasswordAccessJSON(String passwordAccessJSON) {
		this.passwordAccessJSON = passwordAccessJSON;
		return this;
	}

	public String getScope() {
		return scope;
	}

	public ZendeskAuthorizationHelper setScope(String scope) {
		this.scope = scope;
		return this;
	}
}
