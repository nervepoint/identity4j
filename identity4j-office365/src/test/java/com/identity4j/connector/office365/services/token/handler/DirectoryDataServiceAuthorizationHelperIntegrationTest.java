package com.identity4j.connector.office365.services.token.handler;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.AbstractOffice365Test;

public class DirectoryDataServiceAuthorizationHelperIntegrationTest extends AbstractOffice365Test {
	
	
	@Test
	public void itShouldFetchAccessTokenForValidCredentialsAndTenantName() throws IOException{
		//given valid credentials for a tenant
		//when token request is made
		ADToken token = DirectoryDataServiceAuthorizationHelper.getOAuthAccessTokenFromACS(
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
}
