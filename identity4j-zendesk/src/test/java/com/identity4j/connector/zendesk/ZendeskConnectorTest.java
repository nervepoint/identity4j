package com.identity4j.connector.zendesk;

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

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.identity4j.connector.AbstractRestWebServiceConnectorTest;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.Media;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.connector.zendesk.services.Directory;

public class ZendeskConnectorTest extends AbstractRestWebServiceConnectorTest{

	private String validRoleName = null;
	private String testRoleName = null;
	
	private String dummy1RoleName = null;
	private String dummy2RoleName = null;
	
	private String invalidIdentityId = null;
	
	static {
		init("/zendesk-connector.properties");
	}
	
	@Before
	public void setup(){
		validIdentityName = configurationParameters.getStringOrFail("connector.validIdentityName");
		validIdentityId = configurationParameters.getStringOrFail("connector.validIdentityId");
		validIdentityPassword = configurationParameters.getStringOrFail("connector.validIdentityPassword");
		testIdentityName = configurationParameters.getStringOrFail("connector.testIdentityName");
		testIdentityPassword = configurationParameters.getStringOrFail("connector.testIdentityPassword");
		newPassword = configurationParameters.getStringOrFail("connector.newPassword");
		
		invalidIdentityName = configurationParameters.getStringOrFail("connector.invalidIdentityName");
		invalidPassword = configurationParameters.getStringOrFail("connector.invalidPassword");
		
		configurationParameters.getStringOrFail("connector.invalidRoleId");
		
		validRoleName = configurationParameters.getStringOrFail("connector.validRoleName");
		testRoleName = configurationParameters.getStringOrFail("connector.testRoleName");
		
		dummy1RoleName = configurationParameters.getStringOrFail("connector.dummy1RoleName");
		dummy2RoleName = configurationParameters.getStringOrFail("connector.dummy2RoleName");
		
		invalidIdentityId = configurationParameters.getStringOrFail("connector.invalidIdentityId");
		
	}
	
	/***********************************************************************************************************************************/
	/**
	 * Following tests are hard to test, as the api depends on finding Identity by name and newly created identities in Zendesk
	 * as times result in 404 error as the search API is still in process of indexing them.
	 * 
	 * Due to this limitation the tests fails or leave data store in inconsistent state.
	 */

	@Override
	@Test @Ignore
	public void itShouldDeleteIdentityIfPresentInDataStore() {
	}

	
	@Test @Ignore
	public void itShouldChangeThePasswordForValidIdentity() {
	}
	
	@Test @Ignore
	public void itShouldSetPasswordForValidIdentity() {
	}
	
	@Test @Ignore
	public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {
		
	}
	
	/*******************************************************************************************************************************************/
	
	@Test
	public void itShouldCreateARoleIfNotPresentInDataSource(){
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.roles));
		
		//given a valid role not present in data store
		Role role = new RoleImpl(null, testRoleName);
		Role fromDataSource = null;
		try{
			//when role is created
			fromDataSource = connector.createRole(role);
			
			//then role is saved in data store
			Assert.assertEquals("Role principal names should be same",
					role.getPrincipalName(),
					fromDataSource.getPrincipalName());
			
		}finally{
			deleteRoleFromSource(fromDataSource);
		}
	}
	
	@Test(expected=PrincipalAlreadyExistsException.class)
	public void itShouldThrowPrincipalAlreadyExistsExceptionIfRoleAlreadyPresentInDataSource(){
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.roles));
		
		//given a valid role already present in data store
		Role role = new RoleImpl(null, validRoleName);
		//when role is created
		connector.createRole(role);
		//then PrincipalAlreadyExistsException is thrown
	}
	
	@Test
	public void itShouldCreateIdentityIfNotPresentInDataSourceAlongWithRolesProvided() {
		Identity identityFromSource = null;
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		
		//given an identity not present in data store and role agent
		ZendeskIdentity identity = (ZendeskIdentity) getIdentity(testIdentityName);
		identity.setFullName("Mock User");
		identity.setAttribute(ZendeskModelConvertor.USER_ROLE, "agent");
		
		//and a valid role
		identity.addRole(connector.getRoleByName(validRoleName));
		try {
			//when it is created in data store
			identity = (ZendeskIdentity) connector.createIdentity(identity, testIdentityPassword.toCharArray());
			//then fetched instance from data store
			identityFromSource = getIdentityFromSource(identity);
			//should have same assigned principal name
			Assert.assertEquals("Principal names should be same",
					identity.getPrincipalName(),
					identityFromSource.getPrincipalName());
			//should have same role as provided
			Assert.assertEquals("Role name should be same",
					validRoleName,
					identity.getRoles()[0].getPrincipalName());
		}finally {
			if(identityFromSource != null)
				deleteIdentityFromSource(identityFromSource);
		}
	}
	
	
	@Test
	public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.updateUser));
		Identity identityFromSource = null;
		
		//given an identity
		ZendeskIdentity identity = (ZendeskIdentity) getIdentity(testIdentityName);
		identity.setFullName("Test Junit");
		identity.setAttribute(ZendeskModelConvertor.USER_ROLE, "agent");
		
		//and a valid role
		identity.addRole(connector.getRoleByName(validRoleName));
		try {
			identity = (ZendeskIdentity) connector.createIdentity(identity, testIdentityPassword.toCharArray());
			//when the changes are updated
			identity.setFullName("Test JunitChanged");
			identity.setRoles(new Role[]{connector.getRoleByName(dummy1RoleName),connector.getRoleByName(dummy2RoleName)});
			connector.updateIdentity(identity);

			//then the identity from data source should reflect the changes
			identityFromSource = getIdentityFromSource(identity);
			Assert.assertEquals("Full name should be same as updated name",
					identity.getFullName(),
					identityFromSource.getFullName());
			Assert.assertEquals("Total Roles assigned should be 2", 2, identityFromSource.getRoles().length);
			assertRoleIsPresent(identityFromSource.getRoles(), dummy1RoleName);
			assertRoleIsPresent(identityFromSource.getRoles(), dummy2RoleName);
		} finally {
			if(identity != null)
				deleteIdentityFromSource(identity);
		}
	}

	
	@Override
	protected Identity getIdentity(String identityName) {
		ZendeskIdentity zendeskIdentity = null;
		if(identityName.equals(invalidIdentityName)){
			zendeskIdentity = new ZendeskIdentity(invalidIdentityId,identityName);
		}else{
			zendeskIdentity = new ZendeskIdentity(identityName);
		}
		zendeskIdentity.setAddress(Media.email, identityName);
		zendeskIdentity.setAttribute(ZendeskModelConvertor.USER_VERIFIED,"true");
		
		return zendeskIdentity;
	}
	
	/**
	 * We need explicit method calls by ID as search by name @ Zendesk takes time, please refer 
	 * <a href="https://support.zendesk.com/entries/20239737">Search REST API</a> 
	 * details for more information,as a result newly created users/roles on search by name result as not found, 
	 * therefore we have to search by id.
	 */
	@Override
	protected Identity getIdentityFromSource(Identity identity) {
		try {
			return ZendeskModelConvertor.getInstance()
					.convertZendeskUserToZendeskIdentity(
							getDirectoryInstance().users().getByGuid(
									Integer.parseInt(identity.getGuid())));
		}  catch (Exception e) {
			throw new ConnectorException("Problem in private field access", e);
		}
	}

	/**
	 * We need explicit method calls by ID as search by name @ Zendesk takes time, please refer 
	 * <a href="https://support.zendesk.com/entries/20239737">Search REST API</a> 
	 * details for more information,as a result newly created users/roles on search by name result as not found, 
	 * therefore we have to search by id.
	 */
	@Override
	protected void deleteIdentityFromSource(Identity identity) {
		try {
			getDirectoryInstance().users().delete(Integer.parseInt(identity.getGuid()));
		}  catch (Exception e) {
			throw new ConnectorException("Problem in private field access", e);
		}
	}
	
	/**
	 * We need explicit method calls by ID as search by name @ Zendesk takes time, please refer 
	 * <a href="https://support.zendesk.com/entries/20239737">Search REST API</a> 
	 * details for more information,as a result newly created users/roles on search by name result as not found, 
	 * therefore we have to search by id.
	 */
	protected void deleteRoleFromSource(Role role) {
		try {
			if(role.getGuid() == null) return;
			getDirectoryInstance().groups().delete(Integer.parseInt(role.getGuid()));
		}  catch (Exception e) {
			throw new ConnectorException("Problem in private field access", e);
		}
	}
	
	private Directory getDirectoryInstance() throws NoSuchFieldException,IllegalAccessException {
		Field field;
		field = connector.getClass().getDeclaredField("directory");
		field.setAccessible(true); 
		return (Directory) field.get(connector);
	}
	
	private void assertRoleIsPresent(Role[] roles, String principalName){
		for (Role role : roles) {
			if(role.getPrincipalName().equals(principalName)) return;
		}
		Assert.fail("Role not found " + principalName);
	}
	
}
