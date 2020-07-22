package com.identity4j.connector.salesforce.services;

/*
 * #%L
 * Identity4J Salesforce
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
import org.junit.BeforeClass;
import org.junit.Test;

import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.salesforce.SalesforceConfiguration;
import com.identity4j.connector.salesforce.entity.Group;
import com.identity4j.connector.salesforce.services.token.handler.SalesforceAuthorizationHelper;
import com.identity4j.util.MultiMap;
import com.identity4j.util.http.request.HttpRequestHandler;

public class GroupServiceIntegrationTest {

	private static MultiMap configurationParameters;
	private static GroupService groupService = null;
	
	static {
		PropertyConfigurator.configure(GroupServiceIntegrationTest.class.getResource("/test-log4j.properties"));
		configurationParameters = loadConfigurationParameters("/salesforce-connector.properties");
	}
	
	private static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = GroupServiceIntegrationTest.class.getResourceAsStream(
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
		SalesforceAuthorizationHelper.getInstance()
		.setLoginSoapEnvelopTemplate(configurationParameters.getString("salesforceLoginSoapEnvelopTemplate"))
		.setLoginSoapUrl(configurationParameters.getString("salesforceLoginSoapUrl"))
		.setVersion(configurationParameters.getString("salesforceRestApiVersion"))
		.setIpRangeOrAppIpLessRestrictive(false);
		
		SalesforceConfiguration salesforceConfiguration = new SalesforceConfiguration(configurationParameters);
		TokenHolder.getInstance().initToken(salesforceConfiguration);
		groupService = new GroupService(new HttpRequestHandler(), salesforceConfiguration);
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
		Group group = groupService.getByGuid(configurationParameters.getString("connector.validRoleId"));
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
		groupService.getByGuid(configurationParameters.getString("connector.invalidRoleId"));
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
		//and fetch by it's id should throw Principal not found exception
		try{
			groupService.getByGuid(groupPersisted.getId());
			Assert.fail();
		}catch(PrincipalNotFoundException e){
			//expected as already deleted
		}
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfGroupToBeDeletedIsNotPresentInDataStore() throws IOException {
		//given a group not present in data store
		Group group = new Group();
		group.setId(configurationParameters.getString("connector.invalidRoleId"));//random id, ensure not present in datastore
		//when group is deleted
		groupService.delete(group.getId());
		//then it should throw PrincipalNotFoundException
	}
}
