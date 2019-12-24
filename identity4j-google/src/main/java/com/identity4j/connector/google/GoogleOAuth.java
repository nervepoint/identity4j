package com.identity4j.connector.google;

/*
 * #%L
 * Identity4J GOOGLE
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.identity4j.connector.AbstractOAuth2;
import com.identity4j.connector.exception.ConnectorException;

public class GoogleOAuth extends AbstractOAuth2 {

    private String state;
    private String username;
    private GoogleConfiguration configuration;

    public GoogleOAuth(GoogleConfiguration configuration) {
    	this.configuration = configuration;
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
                // GoogleCredential credential = new
                // GoogleCredential().setAccessToken(code);
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

                Oauth2 service = new Oauth2.Builder(netHttpTransport, jacksonFactory, credential).setApplicationName("Identity4J")
                                .build();
                Userinfoplus userInfo = service.userinfo().get().execute();
                username = userInfo.getEmail();
            } catch(TokenResponseException tre) {
                if (tre.getMessage() != null && tre.getMessage().contains("401 Unauthorized")) {
                    throw new ConnectorException(
                                    "Failed to validatate OAuth client authentication. Please check your OAuth secret is correct and matches the client ID you have provided.");
                }
                else
                    throw new ConnectorException("Failed to validate authentication.", tre);
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
