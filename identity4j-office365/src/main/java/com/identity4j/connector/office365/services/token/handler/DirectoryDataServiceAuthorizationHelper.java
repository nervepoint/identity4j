package com.identity4j.connector.office365.services.token.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.Office365Configuration;
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
		BufferedReader rd = null;
		try {
			stsUrl = String.format(stsUrl, tenantName);
			URL url = null;

			String data = null;

			data = "grant_type=client_credentials";
			data += "&resource=" + URLEncoder.encode(graphPrincipalId, "UTF-8");
			data += "&client_id=" + URLEncoder.encode(principalId, "UTF-8");
			data += "&client_secret=" + URLEncoder.encode(clientKey, "UTF-8");

			url = new URL(stsUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			conn.setDoOutput(true);

			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));

			String line, response = "";

			while ((line = rd.readLine()) != null) {
				response += line;
			}

			return JsonMapperService.getInstance().getObject(ADToken.class,
					response);

		} catch (Exception e) {
			throw new ConnectorException(
					Office365Configuration.ErrorGeneratingToken
							+ ":"
							+ Office365Configuration.ErrorGeneratingTokenMessage,
					e);
		} finally {
			if (wr != null)
				wr.close();
			if (rd != null)
				rd.close();
		}
	}

	public static void sleep() {
		try {
			Thread.sleep(1000 * 5);
		} catch (Exception e) {/* ignore */
		}
	}
}
