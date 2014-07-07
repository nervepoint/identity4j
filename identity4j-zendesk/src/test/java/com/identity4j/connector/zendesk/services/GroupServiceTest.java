package com.identity4j.connector.zendesk.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.connector.zendesk.entity.Group;
import com.identity4j.connector.zendesk.entity.GroupMembership;
import com.identity4j.connector.zendesk.entity.GroupMemberships;
import com.identity4j.connector.zendesk.services.token.handler.ZendeskAuthorizationHelper;
import com.identity4j.util.MultiMap;
import com.identity4j.util.http.request.HttpRequestHandler;

public class GroupServiceTest {

	private static MultiMap configurationParameters;
	private static GroupService groupService = null;
	
	static {
		PropertyConfigurator.configure(GroupServiceTest.class.getResource("/test-log4j.properties"));
		configurationParameters = loadConfigurationParameters("/zendesk-connector.properties");
	}
	
	private static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = GroupServiceTest.class.getResourceAsStream(
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
		groupService = new GroupService(new HttpRequestHandler(), zendeskConfiguration);
	}
	
	@Test
	public void itShouldFindGroupByName(){
		//given group service
		//when service is used to find a group by name
		Group group = groupService.getByName(configurationParameters.getString("connector.validRoleName"));
		//then group instance should be returned with same name
		Assert.assertNotNull(group);
		Assert.assertEquals(configurationParameters.getString("connector.validRoleName"), group.getName());
		Assert.assertEquals(configurationParameters.getString("connector.validRoleId"), group.getId().toString());
	}
	
	@Test
	public void itShouldFindGroupByGuid(){
		//given group service
		//when service is used to find a group by guid
		Group group = groupService.getByGuid(Integer.parseInt(configurationParameters.getString("connector.validRoleId")));
		//then group instance should be returned with same id
		Assert.assertNotNull(group);
		Assert.assertEquals(configurationParameters.getString("connector.validRoleName"), group.getName());
		Assert.assertEquals(configurationParameters.getString("connector.validRoleId"), group.getId().toString());
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfGroupByNameNotFoundInTheSystem(){
		//given group service
		//when service is used to find a group by name and name does not exists
		groupService.getByName(configurationParameters.getString("connector.invalidRoleName"));
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfGroupByGuidNotFoundInTheSystem(){
		//given group service
		//when service is used to find a group by id and id does not exists
		groupService.getByGuid(Integer.parseInt(configurationParameters.getString("connector.invalidRoleId")));
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test
	public void itShouldCreateAGroupIfNotPresentInDataStore() throws IOException {
		Group groupPersisted = null;
		try{
			//given a group not present in data store
			Group group = new Group();
			group.setName(configurationParameters.getString("connector.testRoleName"));
			//when group is created
			groupPersisted = groupService.save(group);
			//then it should save the group and return an id
			Assert.assertNotNull(groupPersisted);
			Assert.assertNotNull(groupPersisted.getId());
			Assert.assertEquals(group.getName(), groupPersisted.getName());
			Assert.assertFalse(groupPersisted.getDeleted());
		}finally{
			if(groupPersisted.getId() != null) groupService.delete(groupPersisted.getId());
		}
	}
	
	
	@Test(expected=PrincipalAlreadyExistsException.class)
	public void itShouldThrowPrincipalAlreadyExistsExceptionIfGroupToBeCreatedIsAlreadyPresentInDataStore() throws IOException {
		//given a group present in data store
		Group group = new Group();
		group.setName(configurationParameters.getString("connector.validRoleName"));
		//when group is created
		groupService.save(group);
		//then it should throw PrincipalAlreadyExistsException
	}
	
	@Test
	public void itShouldDeleteAGroupPresentInDataStore() throws IOException {
		//given a group not present in data store
		Group group = new Group();
		group.setName("Dummy Group");
		//and saved in datastore
		Group groupPersisted = groupService.save(group);
		//when it is deleted
		groupService.delete(groupPersisted.getId());
		//then it should be deleted in datastore
		//and fetch by it's id should show active as false
		Group groupFromDs = groupService.getByGuid(groupPersisted.getId());
		Assert.assertTrue(groupFromDs.getDeleted());
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfGroupToBeDeletedIsNotPresentInDataStore() throws IOException {
		//given a group not present in data store
		Group group = new Group();
		group.setId(Integer.parseInt(configurationParameters.getString("connector.invalidRoleId")));//random id, ensure not present in datastore
		//when group is deleted
		groupService.delete(group.getId());
		//then it should throw PrincipalNotFoundException
	}
	
	@Test
	public void itShouldAddUserToAGroup(){
		GroupMembership groupMembership = null;
		try{
			//given a users and groups ids
			int userId = Integer.parseInt(configurationParameters.getString("connector.validIdentityId"));
			int groupId = Integer.parseInt(configurationParameters.getString("connector.validRoleId"));
			
			//when user is added to group
			groupMembership = groupService.addUserToGroup(userId,groupId);
			Assert.assertNotNull(groupMembership.getId());
			Assert.assertEquals(userId, groupMembership.getUserId().intValue());
			Assert.assertEquals(groupId, groupMembership.getGroupId().intValue());
			
			//then GroupMemberships relation should link user to group
			GroupMemberships groupMemberships = groupService.getGroupMembershipsForUser(groupMembership.getUserId());
			for (GroupMembership gms : groupMemberships.getGroupMemberships()) {
				if(gms.getGroupId().equals(groupMembership.getGroupId()) && gms.getUserId().equals(groupMembership.getUserId())){
					return;
				}
			}
			//should not reach here, as member ship should be found in above loop and exit
			Assert.fail();
		}finally{
			if(groupMembership != null) groupService.removeUserFromGroup(groupMembership.getUserId(), groupMembership.getGroupId());
		}
	}
	
	
	@Test
	public void itShouldRemoveUserFromAGroup(){
		//given a users and groups ids
		int userId = Integer.parseInt(configurationParameters.getString("connector.validIdentityId"));
		int groupId = Integer.parseInt(configurationParameters.getString("connector.validRoleId"));
		
		//and user is added to group
		GroupMembership groupMembership = groupService.addUserToGroup(userId,groupId);
		Assert.assertNotNull(groupMembership.getId());
		Assert.assertEquals(userId, groupMembership.getUserId().intValue());
		Assert.assertEquals(groupId, groupMembership.getGroupId().intValue());
		
		//when user is removed from group
		groupService.removeUserFromGroup(groupMembership.getUserId(), groupMembership.getGroupId());
		
		//then there should not be any GroupMemberships relation linking group to user
		GroupMemberships groupMemberships = groupService.getGroupMembershipsForUser(groupMembership.getUserId());
		List<GroupMembership> groupMembershipsList = groupMemberships.getGroupMemberships();
		for (GroupMembership gms : groupMembershipsList) {
			if(gms.getGroupId().equals(groupId) && gms.getUserId().equals(userId)){
				Assert.fail();//if any relation found
			}
		}
	}
	
}
