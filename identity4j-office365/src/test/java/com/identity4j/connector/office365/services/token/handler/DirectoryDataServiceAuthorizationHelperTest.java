package com.identity4j.connector.office365.services.token.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.Office365ConnectorTest;
import com.identity4j.util.MultiMap;

public class DirectoryDataServiceAuthorizationHelperTest {
	private static MultiMap configurationParameters;
	
	static {
		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));
		configurationParameters = loadConfigurationParameters("/office365-connector.properties");
	}
	
	private static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = Office365ConnectorTest.class.getResourceAsStream(
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
	
	@Test
	public void itShouldFetchAccessTokenForValidCredentialsAndTenantName() throws IOException{
		//given valid credentials for a tenant
		//when token request is made
		AADJWTToken token = DirectoryDataServiceAuthorizationHelper.getOAuthAccessTokenFromACS(
				configurationParameters.getString("office365TenantDomainName"),
				configurationParameters.getString("office365GraphPrincipalId"), 
				configurationParameters.getString("office365StsUrl"), 
				configurationParameters.getString("office365AppPrincipalId"), 
				configurationParameters.getString("office365SymmetricKey"));
		//then it should return valid non null access token
		Assert.assertNotNull("Got token instance", token);
		Assert.assertNotNull("Got token value", token.getAccessToken());
		Assert.assertFalse("Should be valid", token.willExpireIn(0));
	}
	
	@Test(expected=ConnectorException.class)
	public void itShouldNoFetchAccessTokenForInValidCredentials() throws IOException{
		//given in valid credentials for a tenant
		//when token request is made
		DirectoryDataServiceAuthorizationHelper.getOAuthAccessTokenFromACS(
				configurationParameters.getString("office365TenantDomainName"),
				configurationParameters.getString("office365GraphPrincipalId"), 
				configurationParameters.getString("office365StsUrl"), 
				"abc", 
				configurationParameters.getString("office365SymmetricKey"));
		//then it should throw connector exception
	}
	
	@Test(expected=ConnectorException.class)
	public void itShouldNoFetchAccessTokenForInValidTenantName() throws IOException{
		//given in valid tenant
		//when token request is made
		DirectoryDataServiceAuthorizationHelper.getOAuthAccessTokenFromACS(
				"abc",
				configurationParameters.getString("office365GraphPrincipalId"), 
				configurationParameters.getString("office365StsUrl"), 
				configurationParameters.getString("office365AppPrincipalId"), 
				configurationParameters.getString("office365SymmetricKey"));
		//then it should throw connector exception
	}
	
	@Test
	public void itShouldAuthenticateValidCredentials() throws Exception{
		//given valid credentials for a tenant
		//when authentication request is made
		boolean token = DirectoryDataServiceAuthorizationHelper.authenticate(
				configurationParameters.getString("office365OAuthUrl"),
				configurationParameters.getString("office365OAuthUrlRedirectUri"),
				configurationParameters.getString("office365AppPrincipalId"), 
				configurationParameters.getString("office365GraphPrincipalId"), 
				"test.user@leenervepoint.onmicrosoft.com","testing123#");
		//then it should return true
		Assert.assertTrue("Credentials are fine", token);
	}
	
	@Test
	public void itShouldNotAuthenticateInValidCredentials() throws Exception{
		//given in valid credentials for a tenant
		//when authentication request is made
		boolean token = DirectoryDataServiceAuthorizationHelper.authenticate(
				configurationParameters.getString("office365OAuthUrl"),
				configurationParameters.getString("office365OAuthUrlRedirectUri"),
				configurationParameters.getString("office365AppPrincipalId"), 
				configurationParameters.getString("office365GraphPrincipalId"), 
				"test.user@leenervepoint.onmicrosoft.com","testing");
		//then it should return false
		Assert.assertFalse("Credentials are fine", token);
	}
}
