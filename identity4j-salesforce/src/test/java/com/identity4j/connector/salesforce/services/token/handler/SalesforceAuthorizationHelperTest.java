package com.identity4j.connector.salesforce.services.token.handler;

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

public class SalesforceAuthorizationHelperTest {

private static MultiMap configurationParameters;
	
	static {
		PropertyConfigurator.configure(SalesforceAuthorizationHelperTest.class.getResource("/test-log4j.properties"));
		configurationParameters = loadConfigurationParameters("/salesforce-connector.properties");
	}
	
	private static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = SalesforceAuthorizationHelperTest.class.getResourceAsStream(
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
		SalesforceAuthorizationHelper.getInstance()
		.setLoginSoapEnvelopTemplate(configurationParameters.getString("salesforceLoginSoapEnvelopTemplate"))
		.setLoginSoapUrl(configurationParameters.getString("salesforceLoginSoapUrl"))
		.setVersion(configurationParameters.getString("salesforceRestApiVersion"))
		.setIpRangeOrAppIpLessRestrictive(null);
	}
	
	@Test
	public void itShouldFetchSessionTokenForValidCredentialsOfAnAdminIfIPRangeIsNotSetInConsole() throws IOException{
		//given valid credentials for an admin
		//when token request is made
		Token token = SalesforceAuthorizationHelper.getInstance()
				.setIpRangeOrAppIpLessRestrictive(false)
				.login(
						configurationParameters.getString("salesforceAdminId"),
						configurationParameters
								.getString("salesforceAdminPassword"),
						configurationParameters
								.getString("salesforceAdminSecretKey"));
		//then it should return valid non null access token
		Assert.assertNotNull("Got token instance", token);
		Assert.assertNotNull("Got token value", token.getSessionId());
	}
	
	@Test
	public void itShouldFetchSessionTokenForValidCredentialsOfAnAdminIfIPRangeIsSetInConsole() throws IOException{
		//given valid credentials for an admin
		//when token request is made
		Token token = SalesforceAuthorizationHelper.getInstance()
				.setIpRangeOrAppIpLessRestrictive(true)
				.login(
						configurationParameters.getString("salesforceAdminId"),
						configurationParameters
								.getString("salesforceAdminPassword"),
						configurationParameters
								.getString("salesforceAdminSecretKey"));
		//then it should return valid non null access token
		Assert.assertNotNull("Got token instance", token);
		Assert.assertNotNull("Got token value", token.getSessionId());
	}
	
	@Test(expected=ConnectorException.class)
	public void itShouldNotFetchSessionTokenForInValidCredentialsOfAnAdmin() throws IOException{
		//given in valid credentials for an admin
		//when token request is made
		SalesforceAuthorizationHelper.getInstance()
				.setIpRangeOrAppIpLessRestrictive(true)
				.login(
						"abc",
						configurationParameters
								.getString("salesforceAdminPassword"),
						configurationParameters
								.getString("salesforceAdminSecretKey"));
		//then it should throw connector exception
	}
	
	@Test(expected=IllegalStateException.class)
	public void itShouldThrowExceptionIfIpRangeConfigurationIsMissing() throws IOException{
		//given valid credentials for an admin
		//when token request is made without ip range configuration
		SalesforceAuthorizationHelper.getInstance()
				.login(
						configurationParameters.getString("salesforceAdminId"),
						configurationParameters
								.getString("salesforceAdminPassword"),
						configurationParameters
								.getString("salesforceAdminSecretKey"));
		//then it should throw illegal state exception
	}
	
	@Test(expected=IllegalStateException.class)
	public void itShouldThrowExceptionIfIpRangeConfigurationIsFalseAndAdminSecretKeyIsMissing() throws IOException{
		//given valid credentials for an admin
		//when token request is made with ip range configuration as false but missing admin secret key
		SalesforceAuthorizationHelper.getInstance()
				.setIpRangeOrAppIpLessRestrictive(false)
				.login(
						configurationParameters.getString("salesforceAdminId"),
						configurationParameters
								.getString("salesforceAdminPassword"),
						null);
		//then it should throw illegal state exception
	}
}
