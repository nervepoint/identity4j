package com.identity4j.connector.office365.services;

import java.io.IOException;

import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.services.token.handler.AADJWTToken;
import com.identity4j.connector.office365.services.token.handler.DirectoryDataServiceAuthorizationHelper;

/**
 * 
 * Token Handler encapsulates current active token used by Services to make REST call.
 * 
 * @author gaurav
 *
 */
class TokenHolder{
	
	private AADJWTToken aadjwtToken;
	
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

	public AADJWTToken getAadjwtToken() {
		return aadjwtToken;
	}

	public void setAadjwtToken(AADJWTToken aadjwtToken) {
		this.aadjwtToken = aadjwtToken;
	}
	
	/**
	 * Fetches a valid token to authenticate REST Service calls.
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public void initToken(Office365Configuration configuration) throws IOException{
		aadjwtToken = DirectoryDataServiceAuthorizationHelper.
		getOAuthAccessTokenFromACS(configuration.getTenantDomainName(),
				configuration.getGraphPrincipalId(), 
				configuration.getStsUrl(), 
				configuration.getAppPrincipalId(),
				configuration.getSymmetricKey());
	}
}
