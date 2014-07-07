package com.identity4j.connector.salesforce.services;

import java.io.IOException;

import com.identity4j.connector.salesforce.SalesforceConfiguration;
import com.identity4j.connector.salesforce.services.token.handler.SalesforceAuthorizationHelper;
import com.identity4j.connector.salesforce.services.token.handler.Token;

/**
 * 
 * Token Handler encapsulates current active token used by Services to make REST call.
 * 
 * @author gaurav
 *
 */
class TokenHolder{
	
	private Token token;
	
	private TokenHolder(){}

	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final TokenHolder INSTANCE = new TokenHolder();
	}
 
	public static TokenHolder getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	public Token getToken() {
		return token;
	}

	/**
	 * Fetches a valid token to authenticate REST Service calls.
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public void initToken(SalesforceConfiguration configuration) throws IOException{
		token = SalesforceAuthorizationHelper.getInstance().login(
				configuration.getAdminId(), configuration.getAdminPassword(),
				configuration.getAdminSecretKey());
	}

}
