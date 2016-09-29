package com.identity4j.connector.office365.services.token.handler;

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

				return JsonMapperService.getInstance().getObject(ADToken.class, resp.contentString());
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
