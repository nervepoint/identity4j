package com.identity4j.connector.office365.services;

import java.io.IOException;

import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.services.token.handler.ADToken;
import com.identity4j.connector.office365.services.token.handler.DirectoryDataServiceAuthorizationHelper;

class TokenHolder{
	
	/**
	 * Get or refresh a valid token to authenticate REST Service calls.
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public static ADToken refreshToken(ADToken token, Office365Configuration configuration) throws IOException{
		ADToken aadjwtToken = DirectoryDataServiceAuthorizationHelper.
		getOAuthAccessTokenFromACS(configuration.getTenantDomainName(),
				configuration.getGraphPrincipalId(), 
				configuration.getStsUrl(), 
				configuration.getAppPrincipalId(),
				configuration.getSymmetricKey());
		if(token == null) {
			return aadjwtToken;
		}
		else {
			token.from(aadjwtToken);
			return token;
		}
	}
}
