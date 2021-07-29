package com.identity4j.connector.office365.services.token.handler;

/*
 * #%L
 * Identity4J OFFICE 365
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
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.Office365Configuration;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;

/**
 * This class is responsible for handling oAuth related activities. It provides
 * helper methods to
 * 
 * <ul>
 * <li>Get oAuth token for performing REST API calls</li>
 * <li>Verify credentials simulating oAuth credentials verifying flow.</li>
 * </ul>
 * 
 * @author gaurav
 *
 */
public class DirectoryDataServiceAuthorizationHelper {
	
	private static final Log log = LogFactory.getLog(DirectoryDataServiceAuthorizationHelper.class);

	/**
	 * Retrieves Json Web Token which is used for authorization of REST API
	 * calls.
	 * 
	 * @param tenantName
	 * @param graphPrincipalId
	 * @param stsUrl
	 * @param principalId
	 * @param clientKey
	 * @return Json Web Token
	 * 
	 * @throws IOException
	 */
	public static ADToken getOAuthAccessTokenFromACS(String tenantName,
			String graphPrincipalId, String authorityURI, String principalId,
			String clientKey, String...scopes) throws IOException {

		
		try {
			
			log.info(String.format("Getting new client_credentials access token for %s (resource %s), princ %s",
					tenantName, graphPrincipalId, principalId ));
			
			ConfidentialClientApplication app = ConfidentialClientApplication
				.builder(principalId, ClientCredentialFactory.createFromSecret(clientKey))
				.authority(String.format(authorityURI,tenantName))
				.connectTimeoutForDefaultHttpClient(60000)
				.build();
			
			ClientCredentialParameters clientCredentialParameters = ClientCredentialParameters.builder(new HashSet<String>(Arrays.asList(scopes))).build();
			
			IAuthenticationResult token = app.acquireToken(clientCredentialParameters).get();
			
			log.info(token);
			
			ADToken adToken = new ADToken();
			adToken.setAccessToken(token.accessToken());
			adToken.setExpiresOn(token.expiresOnDate().getTime());
			
			return adToken;

		} catch (Exception e) {
			throw new ConnectorException(Office365Configuration.ErrorGeneratingToken + ":"
					+ Office365Configuration.ErrorGeneratingTokenMessage, e);
		} 
	}

	public static void sleep() {
		try {
			Thread.sleep(1000 * 5);
		} catch (Exception e) {/* ignore */
		}
	}
}
