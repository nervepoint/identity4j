package com.identity4j.connector.salesforce;

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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.connector.AbstractRestWebServiceConnectorTest;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.Media;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;

public class SalesforceConnectorTest extends AbstractRestWebServiceConnectorTest{

	private String validRoleName = null;
	private String testRoleName = null;
	
	private String dummy1RoleName = null;
	private String dummy2RoleName = null;
	private String profileIdForProfileWhichAllowsPublicGroupAddition = null;
	
	static {
		init("/salesforce-connector.properties");
	}
	
	@Before
	public void setup(){
		setValidIdentityName(configurationParameters.getStringOrFail("connector.validIdentityName"));
		setValidIdentityId(configurationParameters.getStringOrFail("connector.validIdentityId"));
		setValidIdentityPassword(configurationParameters.getStringOrFail("connector.validIdentityPassword"));
		setTestIdentityName(configurationParameters.getStringOrFail("connector.testIdentityName"));
		setTestIdentityPassword(configurationParameters.getStringOrFail("connector.testIdentityPassword"));
		setNewPassword(configurationParameters.getStringOrFail("connector.newPassword"));
		
		setInvalidIdentityName(configurationParameters.getStringOrFail("connector.invalidIdentityName"));
		setInvalidPassword(configurationParameters.getStringOrFail("connector.invalidPassword"));
		
		validRoleName = configurationParameters.getStringOrFail("connector.validRoleName");
		testRoleName = configurationParameters.getStringOrFail("connector.testRoleName");
		
		profileIdForProfileWhichAllowsPublicGroupAddition = configurationParameters.getStringOrFail("connector.profileIdForProfileWhichAllowsPublicGroupAddition");
		dummy1RoleName = configurationParameters.getStringOrFail("connector.dummy1RoleName");
		dummy2RoleName = configurationParameters.getStringOrFail("connector.dummy2RoleName");
	}
	
	
	@Test
	public void itShouldChangeThePasswordForValidIdentity() {
		
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));
		
		//given a valid identity with new password
		Identity identity = getIdentity(getTestIdentityName());
		identity.setFullName("Mock User");
		
		//and stored in data store
		Identity identityCreated = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
		
		
		//when password change is attempted
		connector.changePassword(identityCreated.getPrincipalName(), identityCreated.getGuid(),
				getTestIdentityPassword().toCharArray(), 
				getNewPassword().toCharArray(), true);
		//then check with new password should return true flag
		//check with old password should return false
		assertPasswordChange(identityCreated.getPrincipalName(), getTestIdentityPassword(), getNewPassword());
			
		
	}
	
	@Test
	public void itShouldSetPasswordForValidIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordSet));
		
		//given a valid identity with new password
		Identity identity = getIdentity(getTestIdentityName());
		identity.setFullName("Mock User");
		
		//and stored in data store
		Identity identityCreated = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
		
		//when set password is attempted
		connector.setPassword(identityCreated.getPrincipalName(), identityCreated.getGuid(),
				getNewPassword().toCharArray(),false);
		//then check with new password should return true flag
		//check with old password should return false
		assertPasswordChange(identityCreated.getPrincipalName(), getTestIdentityPassword(), getNewPassword());
			
	}
	
	
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
			connector.deleteRole(fromDataSource.getPrincipalName());
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
		
		//given an identity not present in data store
		SalesforceIdentity identity = (SalesforceIdentity) getIdentity(getTestIdentityName());
		identity.setFullName("Mock User");
		identity.setAttribute("ProfileId", profileIdForProfileWhichAllowsPublicGroupAddition);
		
		//and a valid role
		identity.addRole(connector.getRoleByName(validRoleName));
		try {
			//when it is created in data store
			connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
			//then fetched instance from data store
			identityFromSource = connector
					.getIdentityByName(identity.getPrincipalName());
			//should have same assigned principal name
			Assert.assertEquals("Principal names should be same",
					identity.getPrincipalName(),
					identityFromSource.getPrincipalName());
			//should have same role as provided
			Assert.assertEquals("Role name should be same",
					validRoleName,
					identity.getRoles()[0].getPrincipalName());
		}finally {
			connector.disableIdentity(identityFromSource);
		}
	}
	
	
	@Test
	public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		
		//given an identity not present in data store
		Identity identity = getIdentity(getTestIdentityName());
		identity.setFullName("Mock User");
		
		Role role = new RoleImpl("00G90000001Ti0XEAA", "dummy");
		
		//and an in valid role
		identity.addRole(role);
		try {
			//when it is created in data store
			connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
			//then it should throw PrincipalNotFoundException
			Assert.fail();
		} catch(PrincipalNotFoundException e){
			//and PrincipalType should be role
			Assert.assertEquals("", PrincipalType.role, e.getPrincipalType());
		}finally {
			connector.deleteIdentity(identity.getPrincipalName());
		}
	}
	
	@Test
	public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.updateUser));
		Identity identityFromSource = null;
		
		//given an identity
		SalesforceIdentity identity = (SalesforceIdentity) getIdentity(getTestIdentityName());
		identity.setFullName("Test Junit");
		identity.setAttribute("ProfileId", profileIdForProfileWhichAllowsPublicGroupAddition);
		//and a valid role
		identity.addRole(connector.getRoleByName(validRoleName));
		try {
			identity = (SalesforceIdentity) connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
			//when the changes are updated
			identity.setFullName("Test JunitChanged");
			identity.setRoles(new Role[]{connector.getRoleByName(dummy1RoleName),connector.getRoleByName(dummy2RoleName)});
			connector.updateIdentity(identity);

			//then the identity from data source should reflect the changes
			identityFromSource = connector
					.getIdentityByName(identity.getPrincipalName());
			Assert.assertEquals("Full name should be same as updated name",
					identity.getFullName(),
					identityFromSource.getFullName());
			Assert.assertEquals("Total Roles assigned should be 2", 2, identityFromSource.getRoles().length);
			assertRoleIsPresent(identityFromSource.getRoles(), dummy1RoleName);
			assertRoleIsPresent(identityFromSource.getRoles(), dummy2RoleName);
		} finally {
			connector.disableIdentity(identityFromSource);
		}
	}

	
	@Override
	protected Identity getIdentity(String identityName) {
		if(!identityName.equals(getValidIdentityName())){
			//we cannot delete an identity in Salesforce data store
			//hence we need unique names for each test, we dont want each test to
			//depend on single identity name,therefore we use Math.random
			String[] parts = identityName.split("@");	
			identityName = parts[0] + Math.random() + "@" + parts[1];
		}
		SalesforceIdentity salesforceIdentity = new SalesforceIdentity(identityName);
		salesforceIdentity.setAddress(Media.email, identityName);
		return salesforceIdentity;
	}
	
	@Test
	public void itShouldDeleteIdentityIfPresentInDataStore() {
		//Salesforce does not supports delete, ignore
	}
	
	@Test
	public void itShouldThrowPrincipalNotFoundExceptionOnDeleteIfIdentityNotPresentInDataStore() {
		//Salesforce does not supports delete, ignore
	}
	
	private void assertRoleIsPresent(Role[] roles, String principalName){
		for (Role role : roles) {
			if(role.getPrincipalName().equals(principalName)) return;
		}
		Assert.fail("Role not found " + principalName);
	}
	
}
