package com.identity4j.connector.office365;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;

import com.identity4j.connector.AbstractOAuth2;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.services.token.handler.ADToken;
import com.identity4j.connector.office365.services.token.handler.JWTToken;
import com.identity4j.util.http.Http;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpProviderClient;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.json.JsonMapperService;

public class Office365OAuth extends AbstractOAuth2<Office365Configuration> {

	private String state;
	private String username;
	private String redirectUri;

	public Office365OAuth() {
	}

	@Override
	protected void onOpen(String returnTo) {
		redirectUri = returnTo;
		authorizeUrl = "https://login.windows.net/common/oauth2/authorize";
		clientId = configuration.getAppPrincipalId();
		state = generateUID();

		// Needed to get username and other details
		scope = "openid";
	}

	@Override
	public ReturnStatus validate(Map<String, String[]> returnParameters) throws IOException {
		ReturnStatus s = super.validate(returnParameters);
		if (s == ReturnStatus.AUTHENTICATED && !state.equals(returnParameters.get("state")[0])) {
			s = ReturnStatus.FAILED_TO_AUTHENTICATE;
		}

		if (s == ReturnStatus.AUTHENTICATED) {
			String code = returnParameters.get("code")[0];
			ADToken token = null;
			try {
				token = getToken(code);

				// TODO there must be a better way of parsing this
				String jwt = new String(Base64.decodeBase64(token.getIdToken()));
				int idx = jwt.indexOf("}{\"aud\"");
				if (idx != -1) {
					jwt = jwt.substring(idx + 1);
					int eidx = jwt.indexOf("\"}");
					jwt = jwt.substring(0, eidx + 2);
					ObjectMapper objectMapper = new ObjectMapper();
					JWTToken jwtToken = objectMapper.readValue(jwt, JWTToken.class);
					username = jwtToken.getUpn();
				}
				System.out.println("::: " + jwt);

			} catch (Exception e) {
				throw new ConnectorException(Office365Configuration.ErrorGeneratingToken + ":"
						+ Office365Configuration.ErrorGeneratingTokenMessage, e);
			}

			// try {
			// username = getUsername(token);
			// } catch (Exception e) {
			// throw new ConnectorException(
			// Office365Configuration.ErrorGeneratingToken
			// + ":"
			// + Office365Configuration.ErrorGeneratingTokenMessage,
			// e);
			// }

		}

		return s;
	}

	private ADToken getToken(String code) throws IOException {
		String stsUrl = String.format("https://login.microsoftonline.com/common/oauth2/token");

		HttpProviderClient client = Http.getProvider().getClient(stsUrl, null, null, null);
		client.setConnectTimeout(60000);
		HttpResponse resp = client.post(null,
				Arrays.asList(new HttpPair("grant_type", "authorization_code"), new HttpPair("client_id", clientId),
						new HttpPair("code", code), new HttpPair("redirect_uri", redirectUri),
						new HttpPair("resource", configuration.getGraphPrincipalId()),
						new HttpPair("client_secret", configuration.getSymmetricKey())),
				new HttpPair("Content-Type", "application/x-www-form-urlencoded"));
		try {
			return JsonMapperService.getInstance().getObject(ADToken.class, resp.contentString());
		} finally {
			resp.release();
		}
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	protected String getAdditionalAuthorizedParameters() {
		try {
			return String.format("resource=%s&state=%s",
					URLEncoder.encode(configuration.getGraphPrincipalId(), "UTF-8"), URLEncoder.encode(state, "UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalStateException();
		}
	}

	@Override
	public String getUsername() {
		return username;
	}

}
