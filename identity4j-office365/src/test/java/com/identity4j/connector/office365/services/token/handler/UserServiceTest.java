package com.identity4j.connector.office365.services.token.handler;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.identity4j.connector.office365.AbstractOffice365Test;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.services.TokenHolder;
import com.identity4j.connector.office365.services.UserService;
import com.identity4j.util.http.request.HttpRequestHandler;

public class UserServiceTest extends AbstractOffice365Test {
	
	@Test
	public void itShouldRefreshToken() throws Exception {
		/*
		 * NOTE:
		 * 
		 * For this test to function correctly, the AccessTokenLifetime must be set to the
		 * minimum of 10 minutes.
		 * 
		 * https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-configurable-token-lifetimes
		 * 
		 * New-AzureADPolicy -Definition @('{"TokenLifetimePolicy":{"Version":1,"AccessTokenLifetime","00:10:00"}}') -DisplayName "OrganizationDefaultPolicyScenario" -IsOrganizationDefault $true -Type "TokenLifetimePolicy"
		 */
		Office365Configuration configuration = new Office365Configuration(configurationParameters);
		HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
		ADToken token = TokenHolder.refreshToken(null, configuration);
		UserService userService = new UserService(token, httpRequestHandler,configuration);
		System.out.println("Got " + userService.all().getUsers().size() + " users, waiting 11 minutes, trying again");
		Thread.sleep(TimeUnit.MINUTES.toMillis(11));
		System.out.println("Got another " + userService.all().getUsers().size() + " users, waiting 11 minutes, trying one last time.");
		Thread.sleep(TimeUnit.MINUTES.toMillis(11));
		System.out.println("Got another " + userService.all().getUsers().size() + " users");
	}
}
