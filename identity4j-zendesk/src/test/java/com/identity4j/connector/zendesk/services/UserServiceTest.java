package com.identity4j.connector.zendesk.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.connector.zendesk.entity.User;
import com.identity4j.connector.zendesk.entity.Users;
import com.identity4j.connector.zendesk.services.token.handler.Token;
import com.identity4j.connector.zendesk.services.token.handler.ZendeskAuthorizationHelper;
import com.identity4j.connector.zendesk.services.token.handler.ZendeskAuthorizationHelperTest;
import com.identity4j.util.MultiMap;
import com.identity4j.util.http.request.HttpRequestHandler;

public class UserServiceTest {

	private static MultiMap configurationParameters;
	private static UserService userService = null;
	
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
	
	
	@BeforeClass
	public static void init() throws IOException{
		ZendeskAuthorizationHelper.getInstance()
		.setClientId(configurationParameters.getString("zendeskClientId"))
		.setClientSecret(configurationParameters.getString("zendeskClientSecret"))
		.setoAuthUrl(configurationParameters.getString("zendeskOAuthUrl"))
		.setPasswordAccessJSON(configurationParameters.getString("zendeskOAuthPasswordAccessJSON"))
		.setScope(configurationParameters.getString("zendeskScope"))
		.setSubDomain(configurationParameters.getString("zendeskSubDomain"));
		
		
		ZendeskConfiguration zendeskConfiguration = new ZendeskConfiguration(configurationParameters);
		TokenHolder.getInstance().initToken(zendeskConfiguration);
		HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
		userService = new UserService(httpRequestHandler, zendeskConfiguration,new GroupService(httpRequestHandler, zendeskConfiguration));
	}
	
	@Test
	public void itShouldFindUserByName(){
		//given user service
		//when service is used to find a user by name
		User user = userService.getByName(configurationParameters.getString("connector.validIdentityName"));
		//then user instance should be returned with same name
		Assert.assertNotNull(user);
		Assert.assertEquals(configurationParameters.getString("connector.validIdentityName"), user.getEmail());
		Assert.assertEquals(configurationParameters.getString("connector.validIdentityId"), user.getId().toString());
	}
	
	@Test
	public void itShouldFindUserByGuid(){
		//given user service
		//when service is used to find a user by guid
		User user = userService.getByGuid(Integer.parseInt(configurationParameters.getString("connector.validIdentityId")));
		//then user instance should be returned with same id
		Assert.assertNotNull(user);
		Assert.assertEquals(configurationParameters.getString("connector.validIdentityName"), user.getEmail());
		Assert.assertEquals(configurationParameters.getString("connector.validIdentityId"), user.getId().toString());
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfUserByNameNotFoundInTheSystem(){
		//given user service
		//when service is used to find a user by name and name does not exists
		userService.getByName(configurationParameters.getString("connector.invalidIdentityName"));
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfUserByGuidNotFoundInTheSystem(){
		//given user service
		//when service is used to find a user by id and id does not exists
		userService.getByGuid(Integer.parseInt(configurationParameters.getString("connector.invalidIdentityId")));
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test
	public void itShouldCreateAUserIfNotPresentInDataStore() throws IOException {
		User userPersisted = null;
		try{
			//given a user not present
			User user = createUser();
			//when user is created
			userPersisted = userService.save(user);
			//then it should save the user and return an id
			Assert.assertNotNull(userPersisted);
			Assert.assertNotNull(userPersisted.getId());
			Assert.assertEquals(user.getName(), userPersisted.getName());
			Assert.assertTrue(userPersisted.getActive());
		}finally{
			if(userPersisted.getId() != null) userService.delete(userPersisted.getId());
		}
	}
	
	
	@Test(expected=PrincipalAlreadyExistsException.class)
	public void itShouldThrowPrincipalAlreadyExistsExceptionIfUserToBeCreatedIsAlreadyPresentInDataStore() throws IOException {
		//given a user present in data store
		User user = new User();
		user.setEmail(configurationParameters.getString("connector.validIdentityName"));
		user.setName(configurationParameters.getString("connector.validIdentityFullName"));
		user.setRole("end-user");
		user.setVerified(true);
		//when user is created
		userService.save(user);
		//then it should throw PrincipalAlreadyExistsException
	}
	
	
	@Test
	public void itShouldDeleteAUserPresentInDataStore() throws IOException {
		//given a user not present in data store
		User user = createUser();
		//and saved in datastore
		User userPersisted = userService.save(user);
		//when it is deleted
		User userDeleted = userService.delete(userPersisted.getId());
		//then it should be deleted in datastore
		//and show active as false
		Assert.assertFalse(userDeleted.getActive());
	}


	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfUserToBeDeletedIsNotPresentInDataStore() throws IOException {
		//given a user not present in data store
		User user = new User();
		user.setId(Integer.parseInt(configurationParameters.getString("connector.invalidIdentityId")));//random id, ensure not present in datastore
		//when user is deleted
		userService.delete(user.getId());
		//then it should throw PrincipalNotFoundException
	}
	
	@Test
	public void itShouldUpdateAUserIfNotPresentInDataStore() throws IOException {
		User userPersisted = null;
		User userUpdated = null;
		try{
			//given a user
			User user = createUser();
			userPersisted = userService.save(user);
			//and change in name applied
			userPersisted.setName("Test User Changed");
			//when user is updated in data store
			userService.update(userPersisted);
			//then it should update the changed property
			userUpdated = userService.getByGuid(userPersisted.getId());
			Assert.assertNotNull(userUpdated);
			Assert.assertNotNull(userUpdated.getId());
			Assert.assertEquals(userPersisted.getName(), userUpdated.getName());
			Assert.assertTrue(userUpdated.getActive());
		}finally{
			if(userPersisted.getId() != null) userService.delete(userPersisted.getId());
		}
	}
	
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfUserToBeUpdatedIsNotPresentInDataStore() throws IOException {
		//given a user not present in data store
		User user = new User();
		user.setId(Integer.parseInt(configurationParameters.getString("connector.invalidIdentityId")));//random id, ensure not present in datastore
		//when user is deleted
		userService.update(user);
		//then it should throw PrincipalNotFoundException
	}
	
	
	@Test
	public void itShouldSetAUsersPasswordPresentInDataStoreAndFetchTokenWithIt() throws IOException{
		User userPersisted = null;
		try{
			//given a user
			User user = createUser();
			//and user is created
			userPersisted = userService.save(user);
			//when password is set
			userService.setPassword(userPersisted.getId(), configurationParameters.getString("connector.testIdentityPassword"));
			//then it should set password for it
			//and token access method should fetch a token with the set password
			Token token = ZendeskAuthorizationHelper.getInstance().getOAuthAccessToken(userPersisted.getEmail(),
					configurationParameters.getString("connector.testIdentityPassword"));
			
			Assert.assertNotNull(token);
			Assert.assertNotNull(token.getAccessToken());
		}finally{
			if(userPersisted != null) userService.delete(userPersisted.getId());
		}
	}
	
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfUsersWhosePasswordToBeSetIsNotPresentInDataStore() throws IOException{
		//given a user not present in data store
		User user = new User();
		user.setId(Integer.parseInt(configurationParameters.getString("connector.invalidIdentityId")));//random id, ensure not present in datastore
		//when password is set
		userService.setPassword(user.getId(), configurationParameters.getString("connector.testIdentityPassword"));
		//then it should throw PrincipalNotFoundException
	}
	
	@Test
	public void itShouldFetchAllUsersInTheDataStore(){
		//when all users are fetched
		Users users = userService.all();
		//it should return paginated list of users
		Assert.assertNotNull(users);
		Assert.assertTrue(users.getCount() > 0);
		Assert.assertTrue(users.getUsers().size() > 0);
		Assert.assertEquals(users.getUsers().size(),users.getCount().intValue());
	}
	
	@Test
	public void itShouldReturnTrueForValidCredentials() throws IOException{
		User userPersisted = null;
		try{
			//given a user not present in datastore
			User user = createUser();
			//and user is created
			userPersisted = userService.save(user);
			//when valid password is validated
			Boolean valid = userService.areCredentialsValid(userPersisted.getEmail(), userPersisted.getPassword().toCharArray());
			//then it should return true
			Assert.assertTrue(valid);
		}finally{
			if(userPersisted != null) userService.delete(userPersisted.getId());
		}
	}
	
	@Test
	public void itShouldReturnFalseForInValidCredentials() throws IOException{
		User userPersisted = null;
		try{
			//given a user not present in datastore
			User user = createUser();
			//and user is created
			userPersisted = userService.save(user);
			//when invalid password is validated
			Boolean valid = userService.areCredentialsValid(userPersisted.getEmail(), "abcpass123#".toCharArray());
			//then it should return true
			Assert.assertFalse(valid);
		}finally{
			if(userPersisted != null) userService.delete(userPersisted.getId());
		}
	}
	
	@Test
	public void itShouldSuspendAUserMarkedForSuspension() throws IOException{
		User userPersisted = null;
		try{
			//given a user not present in datastore
			User user = createUser();
			//and user is created
			userPersisted = userService.save(user);
			//when user is suspended
			userPersisted.setSuspended(true);
			User userSuspended = userService.suspend(userPersisted.getId(),true);
			//then suspended property should be true
			Assert.assertTrue(userSuspended.getSuspended());
		}finally{
			if(userPersisted != null) userService.delete(userPersisted.getId());
		}
	}
	
	
	@Test
	public void itShouldRevokeSuspensionForAUser() throws IOException{
		User userPersisted = null;
		try{
			//given a user not present in datastore
			User user = createUser();
			//and user is created as suspended
			user.setSuspended(true);
			userPersisted = userService.save(user);
			//when suspension is revoked
			User userSuspended = userService.suspend(userPersisted.getId(),false);
			//then suspended property should be false
			Assert.assertFalse(userSuspended.getSuspended());
		}finally{
			if(userPersisted != null) userService.delete(userPersisted.getId());
		}
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfUsersWhoIsToBeSuspendedIsNotPresentInDataStore() throws IOException{
		//given a user not present in data store
		//when password is set (random id, ensure not present in datastore)
		userService.suspend(Integer.parseInt(configurationParameters.getString("connector.invalidIdentityId")), true);
		//then it should throw PrincipalNotFoundException
	}
	
	
	private User createUser() {
		User user = new User();
		user.setEmail("test.user@mail.com");
		user.setName("Test User");
		user.setRole("end-user");
		user.setVerified(true);
		user.setPassword("mypass123#");
		return user;
	}
	
}
