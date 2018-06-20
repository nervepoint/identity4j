package com.identity4j.connector.mysql.users;

/*
 * #%L
 * Identity4J MySQL Users Connector
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
import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;

public class MySQLUsersConnectorTest extends AbstractConnectorTest<MySQLUsersConfiguration> {

	private String testDatabaseForGrant;
	private String testDatabaseAnother2ForGrant;
	private String testDatabaseAnother3ForGrant;
	private String invaliduser;

	public MySQLUsersConnectorTest() {
		super("/mysql-users-connector.properties");
		testDatabaseForGrant = configurationParameters.getString("connector.grantOnDatabaseForTest");
		testDatabaseAnother2ForGrant = configurationParameters.getString("connector.grantOnDatabaseAnother2ForTest");
		testDatabaseAnother3ForGrant = configurationParameters.getString("connector.grantOnDatabaseAnother3ForTest");
		invaliduser = configurationParameters.getString("connector.invaliduser");
	}
	
	
	
	@Test
	public void createIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		String newPrincipalName = "test_" + identityName;
		Identity newIdentity = new IdentityImpl(newPrincipalName);
		newIdentity.setAttribute(MySqlUsersConstants.USER_ACCESS, testDatabaseForGrant);
		
		connector.createIdentity(newIdentity, identityPassword.toCharArray());
		try {
			newIdentity = connector.getIdentityByName(newPrincipalName);
			
			Assert.assertNotNull(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS));
			Assert.assertTrue(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseForGrant));

			Identity logon = connector.logon(newPrincipalName,
					identityPassword.toCharArray());
			assertPrincipalMatches(newPrincipalName, logon);
		} finally {
			connector.deleteIdentity(newPrincipalName);
		}
	}
	
	@Test
	public void identityCanBeDisabled() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		String newPrincipalName = "test_" + identityName;
		Identity newIdentity = new IdentityImpl(newPrincipalName);
		newIdentity.setAttribute(MySqlUsersConstants.USER_ACCESS, testDatabaseForGrant);
		
		connector.createIdentity(newIdentity, identityPassword.toCharArray());
		try {
			newIdentity = connector.getIdentityByName(newPrincipalName);

			Identity logon = connector.logon(newPrincipalName,
					identityPassword.toCharArray());
			assertPrincipalMatches(newPrincipalName, logon);
			
			connector.disableIdentity(newIdentity);
			
			Assert.assertTrue("Identity is disabled",newIdentity.getAccountStatus().isDisabled());
			
			Identity fromSource = connector.getIdentityByName(newIdentity.getPrincipalName());
			
			Assert.assertTrue("Identity is disabled",fromSource.getAccountStatus().isDisabled());
			
		} finally {
			connector.deleteIdentity(newPrincipalName);
		}
	}
	
	
	@Test
	public void identityCanBeEnabledWithAllGrants() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		String newPrincipalName = "test_" + identityName;
		Identity newIdentity = new IdentityImpl(newPrincipalName);
		newIdentity.setAttribute(MySqlUsersConstants.USER_ACCESS, testDatabaseForGrant);
		
		connector.createIdentity(newIdentity, identityPassword.toCharArray());
		try {
			newIdentity = connector.getIdentityByName(newPrincipalName);

			Identity logon = connector.logon(newPrincipalName,
					identityPassword.toCharArray());
			assertPrincipalMatches(newPrincipalName, logon);
			
			connector.disableIdentity(newIdentity);
			
			Identity fromSource = connector.getIdentityByName(newIdentity.getPrincipalName());
			
			Assert.assertTrue("Identity is disabled",fromSource.getAccountStatus().isDisabled());
			
			connector.enableIdentity(fromSource);
			
			fromSource = connector.getIdentityByName(newIdentity.getPrincipalName());
			
			Assert.assertFalse("Identity is enabled",fromSource.getAccountStatus().isDisabled());
			
			Assert.assertNotNull(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS));
			Assert.assertTrue(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseForGrant));
			
		} finally {
			connector.deleteIdentity(newPrincipalName);
		}
	}
	
	
	@Test
	public void updateIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.updateUser));
		String newPrincipalName = "test_" + identityName;
		Identity newIdentity = new IdentityImpl(newPrincipalName);
		newIdentity.setAttribute(MySqlUsersConstants.USER_ACCESS, testDatabaseForGrant +
				MySqlUsersConstants.NEW_LINE +  
				testDatabaseAnother2ForGrant);
		
		connector.createIdentity(newIdentity, identityPassword.toCharArray());
		try {
			newIdentity = connector.getIdentityByName(newPrincipalName);
			
			Assert.assertNotNull(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS));
			
			Assert.assertTrue(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseForGrant));
			Assert.assertTrue(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseAnother2ForGrant));
			Assert.assertFalse(newIdentity.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseAnother3ForGrant));

			newIdentity.setAttribute(MySqlUsersConstants.USER_ACCESS, testDatabaseForGrant +
					MySqlUsersConstants.NEW_LINE + 
					testDatabaseAnother3ForGrant);
			
			connector.updateIdentity(newIdentity);
			
			Identity identityFromSource = connector.getIdentityByName(newIdentity.getPrincipalName());
			
			Assert.assertNotNull(identityFromSource.getAttribute(MySqlUsersConstants.USER_ACCESS));
			
			Assert.assertTrue(identityFromSource.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseForGrant));
			Assert.assertTrue(identityFromSource.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseAnother3ForGrant));
			Assert.assertFalse(identityFromSource.getAttribute(MySqlUsersConstants.USER_ACCESS).contains(testDatabaseAnother2ForGrant));
			
			
		} finally {
			connector.deleteIdentity(newPrincipalName);
		}
	}
	
	/**
	 * Helper method to get a random principal name.
	 * 
	 * @return
	 */
	protected String getTestPrincipalName() {
		return invaliduser;
	}
}
