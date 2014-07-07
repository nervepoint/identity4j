package com.identity4j.connector.zendesk.services.token.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
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
			
			OutputStreamWriter wr = null;
			BufferedReader rd = null;
			try {
				URL url = null;
				
				String data = String.format(passwordAccessJSON, clientId,clientSecret,scope,userName,password);
		            
				url = new URL(String.format(oAuthUrl, subDomain));
				
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(60000);
				conn.setRequestMethod(ZendeskConfiguration.POST);
				conn.setRequestProperty(ZendeskConfiguration.CONTENT_TYPE,ZendeskConfiguration.contentTypeJSON);
				
				conn.setDoOutput(true);
				
				wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.flush();
				
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String line, response = "";
				
				while((line=rd.readLine()) != null){
					response += line;
				}
				
				Token token =  JsonMapperService.getInstance().getObject(Token.class, response);
				token.setIssuedAt(new Date());
				return token;
				
			} catch (Exception e) {
				throw new ConnectorException("Error generating token.", e);
			} finally{
				if(wr != null) wr.close();
				if(rd != null) rd.close();
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
