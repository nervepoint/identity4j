package com.identity4j.connector.zendesk.services.token.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.util.MultiMap;

public class ZendeskAuthorizationHelperTest {

private static MultiMap configurationParameters;
	
	static {
		PropertyConfigurator.configure(ZendeskAuthorizationHelperTest.class.getResource("/test-log4j.properties"));
		configurationParameters = loadConfigurationParameters("/zendesk-connector.properties");
	}
	
	private static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = ZendeskAuthorizationHelperTest.class.getResourceAsStream(
					propertiesFile);
			if (resourceAsStream == null) {
				throw new FileNotFoundException("Properties resource "
						+ propertiesFile
						+ " not found. Check it is on your classpath");
			}
			Properties properties = new Properties();
			properties.load(resourceAsStream);
			return MultiMap.toMultiMap(properties);
		} catch (IOException ioe) {
			throw new RuntimeException(
					"Failed to load configuration parameters", ioe);
		}
	}
	
	@Before
	public void setUp(){
		ZendeskAuthorizationHelper.getInstance()
		.setClientId(configurationParameters.getString("zendeskClientId"))
		.setClientSecret(configurationParameters.getString("zendeskClientSecret"))
		.setoAuthUrl(configurationParameters.getString("zendeskOAuthUrl"))
		.setPasswordAccessJSON(configurationParameters.getString("zendeskOAuthPasswordAccessJSON"))
		.setScope(configurationParameters.getString("zendeskScope"))
		.setSubDomain(configurationParameters.getString("zendeskSubDomain"));
	}
	
	@Test
	public void itShouldFetchAccessTokenForValidCredentialsOfClient() throws IOException{
		//given valid credentials for a client
		//when token request is made
		Token token = ZendeskAuthorizationHelper.getInstance().getOAuthAccessToken(configurationParameters.getString("zendeskAdminUserId"),
				configurationParameters.getString("zendeskAdminUserPassword"));
				
		//then it should return valid non null access token
		Assert.assertNotNull("Got token instance", token);
		Assert.assertNotNull("Got token value", token.getAccessToken());
	}
	
	@Test(expected=ConnectorException.class)
	public void itShouldNotFetchAccessTokenForInValidCredentialsOfClient() throws IOException{
		//given invalid credentials for a client
		//when token request is made
		ZendeskAuthorizationHelper.getInstance().setClientSecret("abc").getOAuthAccessToken(configurationParameters.getString("zendeskAdminUserId"),
				configurationParameters.getString("zendeskAdminUserPassword"));
		
		//then it should throw ConnectorException
	}
	
	
}
