package com.identity4j.connector.google;

import java.io.IOException;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.User;
import com.identity4j.connector.AbstractOAuth2;
import com.identity4j.connector.exception.ConnectorException;

public class GoogleOAuth extends AbstractOAuth2<GoogleConfiguration> {

	private String state;
	private String username;

	public GoogleOAuth() {
	}

	@Override
	protected void onOpen(String returnTo) {
		redirectUri = returnTo;
		authorizeUrl = "https://accounts.google.com/o/oauth2/auth";
		clientId = configuration.getGoogleServiceAccountId();
		state = generateUID();
		scope = "https://www.googleapis.com/auth/plus.me";
	}

	@Override
	public ReturnStatus validate(Map<String, String[]> returnParameters)
			throws IOException {
		ReturnStatus s = super.validate(returnParameters);
		if (s == ReturnStatus.AUTHENTICATED
				&& !state.equals(returnParameters.get("state")[0])) {
			s = ReturnStatus.FAILED_TO_AUTHENTICATE;
		}

		if (s == ReturnStatus.AUTHENTICATED) {
			String code = returnParameters.get("code")[0];
			try {

				JacksonFactory jacksonFactory = JacksonFactory
						.getDefaultInstance();

				NetHttpTransport netHttpTransport = GoogleNetHttpTransport
						.newTrustedTransport();
				TokenResponse response = new AuthorizationCodeTokenRequest(
						netHttpTransport, jacksonFactory, new GenericUrl(
								"https://server.example.com/token"), code)
						.setRedirectUri(redirectUri)
						.setClientAuthentication(
								new ClientParametersAuthentication(
										configuration.getGoogleCustomerId(),
										configuration
												.getGooglePrivateKeyEncoded()))
						.execute();
				
				System.out
						.println("Access token: " + response.getAccessToken());

				Credential credential = createCredentialWithAccessTokenOnly(
						netHttpTransport, jacksonFactory, response);

				Directory directory = new Directory.Builder(netHttpTransport,
						jacksonFactory, credential).build();
				User user = directory.users().get("me").execute();
				username = user.getPrimaryEmail();

			} catch (Exception e) {
				throw new ConnectorException(
						"Failed to validate authentication.", e);
			}

		}

		return s;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public static Credential createCredentialWithAccessTokenOnly(
			HttpTransport transport, JsonFactory jsonFactory,
			TokenResponse tokenResponse) {
		return new Credential(BearerToken.authorizationHeaderAccessMethod())
				.setFromTokenResponse(tokenResponse);
	}

}
