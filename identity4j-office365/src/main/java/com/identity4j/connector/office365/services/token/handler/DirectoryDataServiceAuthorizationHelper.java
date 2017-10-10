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
import java.io.OutputStreamWriter;
import java.util.Arrays;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.json.JsonMapperService;

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

	/**
	 * Retrieves Json Web Token which is used for authorization of REST API
	 * calls.
	 * 
	 * @param tenantName
	 * @param graphPrincipalId
	 * @param stsUrl
	 * @param principalId
	 * @param clientKey
	 * 
	 * @return Json Web Token
	 * 
	 * @throws IOException
	 */
	public static ADToken getOAuthAccessTokenFromACS(String tenantName,
			String graphPrincipalId, String stsUrl, String principalId,
			String clientKey) throws IOException {

		OutputStreamWriter wr = null;
		try {
			stsUrl = String.format(stsUrl, tenantName);
			HttpProviderClient client = Http.getProvider().getClient(stsUrl, null, null, null);
			client.setConnectTimeout(60000);
			HttpResponse resp = client.post(null,
					Arrays.asList(
							new HttpPair("grant_type", "client_credentials"),
							new HttpPair("resource", graphPrincipalId), 
							new HttpPair("client_id", principalId),
							new HttpPair("client_secret", clientKey)),
					new HttpPair("Content-Type", "application/x-www-form-urlencoded"));
			try {
				int res = resp.status().getCode();
				if(res == 200) {
                    String contentString = resp.contentString();
                    return JsonMapperService.getInstance().getObject(ADToken.class, contentString);
                } else if(res == 401)
					throw new ConnectorException(Office365Configuration.ErrorGeneratingToken + ":"
							+ Office365Configuration.ErrorAuthenticatingForToken);
				else
					throw new ConnectorException(Office365Configuration.ErrorGeneratingToken + ":"
							+ Office365Configuration.ErrorGeneratingTokenMessage + ". Response code " + res);
			} finally {
				resp.release();
			}

		} catch (Exception e) {
			throw new ConnectorException(Office365Configuration.ErrorGeneratingToken + ":"
					+ Office365Configuration.ErrorGeneratingTokenMessage, e);
		} finally {
			if (wr != null)
				wr.close();
		}
	}

	public static void sleep() {
		try {
			Thread.sleep(1000 * 5);
		} catch (Exception e) {/* ignore */
		}
	}
}
