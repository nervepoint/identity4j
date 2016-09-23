package com.identity4j.connector.office365;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;

import com.identity4j.connector.AbstractOAuth2;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.services.token.handler.ADToken;
import com.identity4j.connector.office365.services.token.handler.JWTToken;
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
	public ReturnStatus validate(Map<String, String[]> returnParameters)
			throws IOException {
		ReturnStatus s = super.validate(returnParameters);
		if (s == ReturnStatus.AUTHENTICATED
				&& !state.equals(returnParameters.get("state")[0])) {
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
				if(idx != -1) {
					jwt = jwt.substring(idx + 1);
					int eidx = jwt.indexOf("\"}");
					jwt = jwt.substring(0, eidx + 2);
				    ObjectMapper objectMapper = new ObjectMapper();
					JWTToken jwtToken = objectMapper.readValue(jwt, JWTToken.class);
					username = jwtToken.getUpn();
				}
				System.out.println("::: " + jwt);
				
			} catch (Exception e) {
				throw new ConnectorException(
						Office365Configuration.ErrorGeneratingToken
								+ ":"
								+ Office365Configuration.ErrorGeneratingTokenMessage,
						e);
			}
			

//			try {
//				username = getUsername(token);
//			} catch (Exception e) {
//				throw new ConnectorException(
//						Office365Configuration.ErrorGeneratingToken
//								+ ":"
//								+ Office365Configuration.ErrorGeneratingTokenMessage,
//						e);
//			}

		}

		return s;
	}

	private ADToken getToken(String code) throws IOException {
		String stsUrl = String
				.format("https://login.microsoftonline.com/common/oauth2/token");

		// Get the token
		URL url = new URL(stsUrl);
		String data = String
				.format("client_id=%s"
						+ "&code=%s&grant_type=authorization_code&redirect_uri=%s&resource=%s&client_secret=%s",
						URLEncoder.encode(clientId, "UTF-8"), URLEncoder
								.encode(code, "UTF-8"), URLEncoder.encode(
								redirectUri, "UTF-8"), URLEncoder.encode(
								configuration.getGraphPrincipalId(), "UTF-8"),
						URLEncoder.encode(configuration.getSymmetricKey(),
								"UTF-8"));

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(60000);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		conn.setUseCaches(false);
		conn.setDoOutput(true);

		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		try {
			wr.write(data);
			wr.flush();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			try {
				String line, response = "";
				while ((line = rd.readLine()) != null) {
					response += line;
				}
				return JsonMapperService.getInstance().getObject(
						ADToken.class, response);
			} finally {
				rd.close();
			}
		} finally {
			wr.close();
		}
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	protected String getAdditionalAuthorizedParameters() {
		try {
			return String.format("resource=%s&state=%s", URLEncoder
					.encode(configuration.getGraphPrincipalId(), "UTF-8"),
					URLEncoder.encode(state, "UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalStateException();
		}
	}

	@Override
	public String getUsername() {
		return username;
	}

}
