package com.identity4j.connector.google;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
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
		clientId = configuration.getGoogleOAuthClientId();
		state = generateUID();
		scope = "https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/userinfo.email";
	}

	@Override
	public ReturnStatus validate(Map<String, String[]> returnParameters) throws IOException {
		ReturnStatus s = super.validate(returnParameters);
		if (s == ReturnStatus.AUTHENTICATED
				&& (!returnParameters.containsKey("state") || !state.equals(returnParameters.get("state")[0]))) {
			s = ReturnStatus.FAILED_TO_AUTHENTICATE;
		}

		if (s == ReturnStatus.AUTHENTICATED) {
			String code = returnParameters.get("code")[0];
			try {
//				GoogleCredential credential = new GoogleCredential().setAccessToken(code);
				JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
				HttpTransport netHttpTransport = GoogleConnector.createTransport();
				
				TokenResponse response = new AuthorizationCodeTokenRequest(netHttpTransport, jacksonFactory,
						new GenericUrl("https://accounts.google.com/o/oauth2/token"), code)
								.setRedirectUri(redirectUri)
								.setClientAuthentication(
										new ClientParametersAuthentication(configuration.getGoogleOAuthClientId(),
												configuration.getGoogleOAuthClientSecret()))
								.execute();

				Credential credential = createCredentialWithAccessTokenOnly(netHttpTransport, jacksonFactory, response);
				
				
				Oauth2 service = new Oauth2.Builder(netHttpTransport, jacksonFactory, credential)
						.setApplicationName("Identity4J").build();
				Userinfoplus userInfo = service.userinfo().get().execute();
				username = userInfo.getEmail();
			} catch (Exception e) {
				throw new ConnectorException("Failed to validate authentication.", e);
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

	@Override
	protected String getAdditionalAuthorizedParameters() {
		try {
			return String.format("state=%s", URLEncoder.encode(state, "UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalStateException();
		}
	}

	public static Credential createCredentialWithAccessTokenOnly(HttpTransport transport, JsonFactory jsonFactory,
			TokenResponse tokenResponse) {
		return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(tokenResponse);
	}

}
