package com.identity4j.connector.zendesk.services;

import java.io.IOException;

import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.connector.zendesk.services.token.handler.Token;
import com.identity4j.connector.zendesk.services.token.handler.ZendeskAuthorizationHelper;

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
	public void initToken(ZendeskConfiguration configuration) throws IOException{
		token = ZendeskAuthorizationHelper.getInstance().getOAuthAccessToken(
				configuration.getAdminUserId(),
				configuration.getAdminUserPassword());
	}

}
