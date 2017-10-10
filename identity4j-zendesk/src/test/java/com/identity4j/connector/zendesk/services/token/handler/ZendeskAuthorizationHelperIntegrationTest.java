package com.identity4j.connector.zendesk.services.token.handler;

/*
 * #%L
 * Identity4J Zendesk
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

public class ZendeskAuthorizationHelperIntegrationTest {

private static MultiMap configurationParameters;
	
	static {
		PropertyConfigurator.configure(ZendeskAuthorizationHelperIntegrationTest.class.getResource("/test-log4j.properties"));
		configurationParameters = loadConfigurationParameters("/zendesk-connector.properties");
	}
	
	private static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = ZendeskAuthorizationHelperIntegrationTest.class.getResourceAsStream(
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
