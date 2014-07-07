package com.identity4j.connector.office365.services.token.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.util.json.JsonMapperService;

/**
 * This class is responsible for handling oAuth related activities.
 * It provides helper methods to 
 * 
 * <ul>
 * 	<li>Get oAuth token for performing REST API calls</li>
 *  <li>Verify credentials simulating oAuth credentials verifying flow.</li>
 * </ul>
 * 
 * @author gaurav
 *
 */
public class DirectoryDataServiceAuthorizationHelper {
	
	private static final Log log = LogFactory.getLog(DirectoryDataServiceAuthorizationHelper.class);

/**
 * Retrieves Json Web Token which is used for authorization of REST API calls.
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
public static AADJWTToken getOAuthAccessTokenFromACS(String tenantName,String graphPrincipalId,String stsUrl,String principalId, String clientKey) throws IOException  {
		
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		try {
			stsUrl = String.format(stsUrl,tenantName);	
			URL url = null;
			
			String data = null;
			
			 data = "grant_type=client_credentials";             
			 data += "&resource=" +  URLEncoder.encode(graphPrincipalId,"UTF-8"); 
			 data += "&client_id=" + URLEncoder.encode(principalId,"UTF-8"); 
			 data += "&client_secret=" +URLEncoder.encode(clientKey,"UTF-8");
	            
	            
			url = new URL(stsUrl);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			conn.setDoOutput(true);
			
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line, response = "";
			
			while((line=rd.readLine()) != null){
				response += line;
			}
			
			return JsonMapperService.getInstance().getObject(AADJWTToken.class, response);
			
		} catch (Exception e) {
			throw new ConnectorException(Office365Configuration.ErrorGeneratingToken + ":" + Office365Configuration.ErrorGeneratingTokenMessage, e);
		} finally{
			if(wr != null) wr.close();
			if(rd != null) rd.close();
		}
	}

	/**
	 * This method simulates the authentication process.
	 * <p>
	 * Simulation happens as follows
	 * 
	 * <ul>
	 * 	<li>oAuth url is asked for code, url is oAuth authorize url along with your client id, redirect uri and resource for which authorization is required</li>
	 *  <li>We are challenged by a sign in page wher ewe have to provide our credentials and submit a POST HTTP request</li>
	 *  <li>If we provided correct credentials we are redirected to redirect uri with code else shown sign in page again</li>
	 * </ul>
	 * </p>
	 * 
	 * @param oAuthUrl
	 * @param principalId
	 * @param graphPrincipalId
	 * @param username
	 * @param password
	 * @return true if credentials are fine
	 */
	public static boolean authenticate(String oAuthUrl,String oAuthUrlRedirectUri,String principalId,String graphPrincipalId, 
			String username,String password) {
		
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		//tracks url currently being processed
		RequestAnalyzer requestAnalyzer = new RequestAnalyzer(webClient);
		try {
			webClient.setWebConnection(requestAnalyzer);
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setAppletEnabled(false);
			webClient.getOptions().setTimeout(60000);
			String url = String.format(oAuthUrl, URLEncoder.encode(principalId,"UTF-8"),oAuthUrlRedirectUri,graphPrincipalId);
			HtmlPage page = webClient.getPage(url);
			HtmlForm form = page.getForms().get(0);
			//submit button
			HtmlSpan htmlSpan = page.getHtmlElementById("cred_sign_in_button");
			//form input elements
			HtmlTextInput login = form.getInputByName("login");
			HtmlPasswordInput pass = form.getInputByName("passwd");
			// set user name
			login.setValueAttribute(username);
			login.blur();
			//set password
			pass.setValueAttribute(password);
			// Now submit the form which would trigger an ajax request.
			htmlSpan.click();
			//we wait for ajax request to complete in 10 seconds
			sleep();
			log.info("Done waiting for javascript executions.");
			//finally we submit the form, if credentials are fine we will be redirected to redirect uri with code
			//else we will be shown sign in page again
			htmlSpan.click().getWebResponse();
		} catch (Exception e) {
			/**
			 * Ignore as redirect uri is localhost where we dont have server running to accept
			 * connections, it will throw exception, we will conclude credentials are fine 
			 * by verifying presence of code query param.
			 */
		}
		return requestAnalyzer.url.contains("code=");
	}
	
	public static void sleep(){
		try{
			Thread.sleep(1000 * 5);
		}catch(Exception e){/*ignore*/}
	}

	/**
	 * Helper class used for oAuth simulated authentication, it keeps track of url being called
	 * for current request.
	 * 
	 * @author gaurav
	 *
	 */
	static class RequestAnalyzer extends WebConnectionWrapper {

		public String url = "";
		public RequestAnalyzer(WebClient webClient) throws IllegalArgumentException {
			super(webClient);
		}
		
		@Override
		public WebResponse getResponse(WebRequest request) throws IOException {
			url = request.getUrl().toString();
			return super.getResponse(request);
		}
		
	}
}
