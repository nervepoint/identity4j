package com.identity4j.connector.office365;

import static org.junit.Assert.assertEquals;

/*
 * #%L
 * Identity4J OFFICE 365
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.identity4j.connector.AbstractRestWebServiceConnectorTest;
import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.MultiMap;

/**
 * Test class for testing Active Directory Connector
 * 
 * There is Pre Requisite requirement of having a valid user entry in google
 * user data store That user should be mapped by configuration key for id :
 * connector.validIdentityName and for password :
 * connector.validIdentityPassword
 * 
 * For Role there should be entry for name : connector.validRoleName and for
 * email : connector.validRoleEmail
 * 
 * @author gaurav
 *
 */
public class Office365ConnectorTest extends AbstractRestWebServiceConnectorTest {

	private String validRoleName = null;
	private String validRoleEmail = null;
	private String validRoleDescription = null;
	@SuppressWarnings("unused")
	private String validRoleObjectId = null;
	private String testRoleName = null;
	private String testRoleEmail = null;
	private String testRoleDescription = null;
	private String dummy1RoleId = null;
	private String dummy2RoleId = null;

	static {
		init("/office365-connector.properties");
	}

	@Test(expected = NoSuchElementException.class)
	public final void itShouldReadAllIdentities() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
		Iterator<Identity> allIdentities = connector.allIdentities();
		assertNotNull(allIdentities);
		while (allIdentities.hasNext()) {
			allIdentities.next();
		}
		allIdentities.next();
	}

	@Test(expected = NoSuchElementException.class)
	public final void itShouldReadAllRoles() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));
		Iterator<Role> allRoles = connector.allRoles();
		assertNotNull(allRoles);
		while (allRoles.hasNext()) {
			System.out.println(allRoles.next());
		}
		allRoles.next();
	}

	@After
	public void after() {
		
	}

	@Before
	public void setup() {
		setValidIdentityName(configurationParameters.getStringOrFail("connector.validIdentityName"));
		setValidIdentityId(configurationParameters.getStringOrFail("connector.validIdentityId"));
		setValidIdentityPassword(configurationParameters.getStringOrFail("connector.validIdentityPassword"));
		setTestIdentityName(configurationParameters.getStringOrFail("connector.testIdentityName"));
		setTestIdentityPassword(configurationParameters.getStringOrFail("connector.testIdentityPassword"));
		setNewPassword(configurationParameters.getStringOrFail("connector.newPassword"));
		setInvalidIdentityName(configurationParameters.getStringOrFail("connector.invalidIdentityName"));
		setInvalidPassword(configurationParameters.getStringOrFail("connector.invalidPassword"));
		validRoleName = configurationParameters.getStringOrFail("connector.validRoleName");
		validRoleEmail = configurationParameters.getStringOrFail("connector.validRoleEmail");
		validRoleDescription = configurationParameters.getStringOrFail("connector.validRoleDescription");

		validRoleObjectId = configurationParameters.getStringOrFail("connector.validRoleObjectId");
		testRoleName = configurationParameters.getStringOrFail("connector.testRoleName");
		testRoleEmail = configurationParameters.getStringOrFail("connector.testRoleEmail");
		testRoleDescription = configurationParameters.getStringOrFail("connector.testRoleDescription");

		dummy1RoleId = configurationParameters.getStringOrFail("connector.dummy1RoleId");
		dummy2RoleId = configurationParameters.getStringOrFail("connector.dummy2RoleId");
	}

	@Test
	public void itShouldNotBeInReadOnlyModeIfServicePrincipalHasDeletePrivilege() throws Exception {
		// given a connector with delete privilege
		Office365Connector connector = null;

		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));

		MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		Office365Configuration parms = connectorBuilder.buildConfiguration(configurationParameters);
		// when connector is instantiated
		connector = (Office365Connector) connectorBuilder.buildConnector(parms);

		// then it should not be in read only mode
		assertFalse("Coonector is not in read only mode", connector.isReadOnly());
	}

	@Test
	public void itShouldBeInReadOnlyModeIfServicePrincipalDoesNotHaveDeletePrivilege() throws Exception {
		// given a connector with no delete privilege
		Office365Connector connector = null;

		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));

		MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
		configurationParameters.put("office365AppDeletePrincipalRole", new String[] { "Dummy Delete Role" });
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		Office365Configuration parms = connectorBuilder.buildConfiguration(configurationParameters);
		// when connector is instantiated
		connector = (Office365Connector) connectorBuilder.buildConnector(parms);

		// then it should be in read only mode
		assertTrue("Connector is in read only mode", connector.isReadOnly());
	}

	@Test(expected = ConnectorException.class)
	public void itShouldThrowConnectorExceptionIfServicePrincipalDoesNotHaveDeletePrivilegeAndDeleteIdentityOperationIsPerformed()
			throws Exception {
		// given a connector with no delete privilege
		Office365Connector connector = null;

		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));

		MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
		configurationParameters.put("office365AppDeletePrincipalRole", new String[] { "Dummy Delete Role" });
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		Office365Configuration parms = connectorBuilder.buildConfiguration(configurationParameters);
		connector = (Office365Connector) connectorBuilder.buildConnector(parms);

		assertTrue("Connector is in read only mode", connector.isReadOnly());
		// when delete operation is attempted
		connector.deleteIdentity("dummy key");
		// then it should throw ConnectorException
	}

	@Test(expected = ConnectorException.class)
	public void itShouldThrowConnectorExceptionIfServicePrincipalDoesNotHaveDeletePrivilegeAndUpdateIdentityOperationIsPerformed()
			throws Exception {
		// given a connector with no delete privilege
		Office365Connector connector = null;

		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));

		MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
		configurationParameters.put("office365AppDeletePrincipalRole", new String[] { "Dummy Delete Role" });
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		Office365Configuration parms = connectorBuilder.buildConfiguration(configurationParameters);
		connector = (Office365Connector) connectorBuilder.buildConnector(parms);

		assertTrue("Coonector is in read only mode", connector.isReadOnly());
		// when update operation is attempted
		connector.updateIdentity(new Office365Identity("dummy"));
		// then it should throw ConnectorException
	}

	@Test(expected = ConnectorException.class)
	public void itShouldThrowConnectorExceptionIfServicePrincipalDoesNotHaveDeletePrivilegeAndDeleteRoleOperationIsPerformed()
			throws Exception {
		// given a connector with no delete privilege
		Office365Connector connector = null;

		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));

		MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
		configurationParameters.put("office365AppDeletePrincipalRole", new String[] { "Dummy Delete Role" });
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		Office365Configuration parms = connectorBuilder.buildConfiguration(configurationParameters);
		connector = (Office365Connector) connectorBuilder.buildConnector(parms);

		assertTrue("Coonector is in read only mode", connector.isReadOnly());
		// when delete operation is attempted
		connector.deleteRole("dummy guid");
		// then it should throw ConnectorException
	}

	@Test(expected = ConnectorException.class)
	public void itShouldThrowConnectorExceptionIfServicePrincipalDoesNotHaveDeletePrivilegeAndUpdateRoleOperationIsPerformed()
			throws Exception {
		// given a connector with no delete privilege
		Office365Connector connector = null;

		PropertyConfigurator.configure(Office365ConnectorTest.class.getResource("/test-log4j.properties"));

		MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
		configurationParameters.put("office365AppDeletePrincipalRole", new String[] { "Dummy Delete Role" });
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		Office365Configuration parms = connectorBuilder.buildConfiguration(configurationParameters);
		connector = (Office365Connector) connectorBuilder.buildConnector(parms);

		assertTrue("Coonector is in read only mode", connector.isReadOnly());
		// when update operation is attempted
		connector.updateRole(new RoleImpl("dummy guid", "dummy role"));
		// then it should throw ConnectorException
	}

	@Test
	public void itShouldCreateARoleIfNotPresentInDataSource() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));

		// given a valid role not present in data store
		Role role = buildRole(testRoleName, testRoleEmail, testRoleDescription);
		Role fromDataSource = null;
		try {
			// when role is created
			fromDataSource = connector.createRole(role);

			// then role is saved in data store
			Assert.assertEquals("Role principal names should be same", role.getPrincipalName(),
					fromDataSource.getPrincipalName());

		} catch (PrincipalAlreadyExistsException paee) {
			/* This may be caused by an interrupted test, so cleanup this time */
			connector.deleteRole(testRoleName);
			throw paee;
		} finally {
			if (fromDataSource != null)
				connector.deleteRole(testRoleName);
		}
	}

	protected Role buildRole(String testRoleName, String testRoleEmail, String testRoleDescription) {
		Role role = new RoleImpl(null, testRoleName);
		role.setAttribute("mailNickname", testRoleEmail.split("@")[0]);
		role.setAttribute("description", testRoleDescription);
		role.setAttribute("mailEnabled", "false");
		role.setAttribute("securityEnabled", "true");
		return role;
	}

	@Test(expected = PrincipalAlreadyExistsException.class)
	public void itShouldThrowPrincipalAlreadyExistsExceptionIfRoleAlreadyPresentInDataSource() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));

		// given a valid role already present in data store
		Role role = buildRole(validRoleName, validRoleEmail, validRoleDescription);
		// when role is created
		connector.createRole(role);
		// then PrincipalAlreadyExistsException is thrown
	}

	@Test
	public void itShouldCreateIdentityIfNotPresentInDataSourceAlongWithRolesProvided() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createUser));

		// given an identity not present in data store
		Identity officeIdentity = new Office365Identity(getTestIdentityName());
		officeIdentity.setFullName("Mock User");
		// and a valid role
		officeIdentity.addRole(connector.getRoleByName(validRoleName));
		try {
			// when it is created in data store
			connector.createIdentity(officeIdentity, getTestIdentityPassword().toCharArray());
			// then fetched instance from data store
			Identity googleIdentityFromSource = connector.getIdentityByName(officeIdentity.getPrincipalName());
			// should have same assigned principal name
			Assert.assertEquals("Principal names should be same", officeIdentity.getPrincipalName(),
					googleIdentityFromSource.getPrincipalName());
			// should have same role as provided
			Assert.assertEquals("Role name should be same", validRoleName,
					officeIdentity.getRoles()[0].getPrincipalName());
		} finally {
			connector.deleteIdentity(getTestIdentityName());
		}
	}

	@Test
	public void itShouldFilterIncludedRoles() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));

		// given a valid role not present in data store
		Role role1 = buildRole(testRoleName, testRoleEmail, testRoleDescription);
		Role role2 = buildRole(testRoleName + "2", testRoleEmail + "2", testRoleDescription + "2");
		Role role3 = buildRole(testRoleName + "3", testRoleEmail + "3", testRoleDescription + "3");
		try {
			
			connector.createRole(role1);
			connector.createRole(role2);
			connector.createRole(role3);

			// TODO sort this out in 1.1. We have to build a new connector to try different
			// configuration
			// 1.1 should accept filters to all() methods and this testing should be
			// abstracted
			MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
			ConnectorBuilder connectorBuilder = new ConnectorBuilder();
			Office365Configuration cfg = (Office365Configuration) connectorBuilder
					.buildConfiguration(configurationParameters);
			cfg.setIncludedGroups(new LinkedHashSet<String>(Arrays.asList(testRoleName, testRoleName + "2")));
			Office365Connector nconnector = (Office365Connector) connectorBuilder.buildConnector(cfg);
			try {
				Iterator<Role> idIt = nconnector.allRoles();
				assertTrue("Connector must return at least one role", idIt.hasNext());
				Role r = idIt.next();
				assertEquals("The correct role must have been returned", testRoleName, r.getPrincipalName());
				assertTrue("Connector must return a second role", idIt.hasNext());
				Role r2 = idIt.next();
				assertEquals("The correct role must have been returned", testRoleName + "2", r2.getPrincipalName());
				assertFalse("Connector must return just two roles", idIt.hasNext());
			} finally {
				nconnector.close();
			}

		} finally {
			try {
				connector.deleteRole(testRoleName);
			} catch (Exception e) {
			}
			try {
				connector.deleteRole(testRoleName + "2");
			} catch (Exception e) {
			}
			try {
				connector.deleteRole(testRoleName + "3");
			} catch (Exception e) {
			}
		}
	}

	@Test
	public void itShouldFilterExcludedRoles() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));

		// given a valid role not present in data store
		Role role1 = buildRole(testRoleName, testRoleEmail, testRoleDescription);
		Role role2 = buildRole(testRoleName + "2", testRoleEmail + "2", testRoleDescription + "2");
		Role role3 = buildRole(testRoleName + "3", testRoleEmail + "3", testRoleDescription + "3");
		Role fromDataSource1 = null;
		Role fromDataSource2 = null;
		Role fromDataSource3 = null;
		try {
			fromDataSource1 = connector.createRole(role1);
			fromDataSource2 = connector.createRole(role2);
			fromDataSource3 = connector.createRole(role3);

			// TODO sort this out in 1.1. We have to build a new connector to try different
			// configuration
			// 1.1 should accept filters to all() methods and this testing should be
			// abstracted
			MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
			ConnectorBuilder connectorBuilder = new ConnectorBuilder();
			Office365Configuration cfg = (Office365Configuration) connectorBuilder
					.buildConfiguration(configurationParameters);
			cfg.setExcludedGroups(new LinkedHashSet<String>(Arrays.asList(testRoleName, testRoleName + "2")));
			Office365Connector nconnector = (Office365Connector) connectorBuilder.buildConnector(cfg);
			try {
				Iterator<Role> idIt = nconnector.allRoles();
				assertTrue("Connector must return at least one role", idIt.hasNext());
				Role found = null;
				while (idIt.hasNext()) {
					Role id = idIt.next();
					if (id.getPrincipalName().equals(testRoleName + "3"))
						found = id;
				}
				assertTrue("A role must have been returned", found != null);
				assertEquals("The correct role must have been returned", testRoleName + "3", found.getPrincipalName());
			} finally {
				nconnector.close();
			}

		} catch (PrincipalAlreadyExistsException paee) {
			/* This may be caused by an interrupted test, so cleanup this time */
			try {
				connector.deleteRole(testRoleName);
			} catch (Exception e) {
			}
			try {
				connector.deleteRole(testRoleName + "2");
			} catch (Exception e) {
			}
			try {
				connector.deleteRole(testRoleName + "3");
			} catch (Exception e) {
			}
			throw paee;
		} finally {
			if (fromDataSource1 != null)
				connector.deleteRole(testRoleName);
			if (fromDataSource2 != null)
				connector.deleteRole(testRoleName + "2");
			if (fromDataSource3 != null)
				connector.deleteRole(testRoleName + "3");
		}
	}

	@Test
	public void itShouldFilterIncludedExcludedRoles() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));

		// given a valid role not present in data store
		Role role1 = buildRole(testRoleName, testRoleEmail, testRoleDescription);
		Role role2 = buildRole(testRoleName + "2", testRoleEmail + "2", testRoleDescription + "2");
		Role role3 = buildRole(testRoleName + "3", testRoleEmail + "3", testRoleDescription + "3");
		Role fromDataSource1 = null;
		Role fromDataSource2 = null;
		Role fromDataSource3 = null;
		try {
			fromDataSource1 = connector.createRole(role1);
			fromDataSource2 = connector.createRole(role2);
			fromDataSource3 = connector.createRole(role3);

			// TODO sort this out in 1.1. We have to build a new connector to try different
			// configuration
			// 1.1 should accept filters to all() methods and this testing should be
			// abstracted
			MultiMap configurationParameters = loadConfigurationParameters("/office365-connector.properties");
			ConnectorBuilder connectorBuilder = new ConnectorBuilder();
			Office365Configuration cfg = (Office365Configuration) connectorBuilder
					.buildConfiguration(configurationParameters);
			cfg.setIncludedGroups(new LinkedHashSet<String>(Arrays.asList(testRoleName, testRoleName + "2")));
			cfg.setExcludedGroups(new LinkedHashSet<String>(Arrays.asList(testRoleName)));
			Office365Connector nconnector = (Office365Connector) connectorBuilder.buildConnector(cfg);
			try {
				Iterator<Role> idIt = nconnector.allRoles();
				assertTrue("Connector must return at least one role", idIt.hasNext());
				Role r = idIt.next();
				assertEquals("The correct role must have been returned", testRoleName + "2", r.getPrincipalName());
				assertFalse("Connector must return just one roles", idIt.hasNext());
			} finally {
				nconnector.close();
			}

		} catch (PrincipalAlreadyExistsException paee) {
			/* This may be caused by an interrupted test, so cleanup this time */
			try {
				connector.deleteRole(testRoleName);
			} catch (Exception e) {
			}
			try {
				connector.deleteRole(testRoleName + "2");
			} catch (Exception e) {
			}
			try {
				connector.deleteRole(testRoleName + "3");
			} catch (Exception e) {
			}
			throw paee;
		} finally {
			if (fromDataSource1 != null)
				connector.deleteRole(testRoleName);
			if (fromDataSource2 != null)
				connector.deleteRole(testRoleName + "2");
			if (fromDataSource3 != null)
				connector.deleteRole(testRoleName + "3");
		}
	}

	@Test
	public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createUser));

		// given an identity not present in data store
		Identity officeIdentity = new Office365Identity(getTestIdentityName());
		officeIdentity.setFullName("Mock User");

		Role role = new RoleImpl("bf4ec5c1-af3d-4ae9-9fa3-902217f766b5", "dummy");
		role.setAttribute("mailNickname", "dummyEmail");
		role.setAttribute("description", "dummy role not in data store");
		role.setAttribute("mailEnabled", "false");
		role.setAttribute("securityEnabled", "true");

		// and an in valid role
		officeIdentity.addRole(role);
		try {
			// when it is created in data store
			connector.createIdentity(officeIdentity, getTestIdentityPassword().toCharArray());
			// then it should throw PrincipalNotFoundException
			Assert.fail();
		} catch (PrincipalNotFoundException e) {
			// and PrincipalType should be role
			Assert.assertEquals("", PrincipalType.role, e.getPrincipalType());
		} finally {
			deleteIdentityFromSource(getTestIdentityName());
		}
	}

	@Test
	public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.updateUser));

		// given an identity
		Identity officeIdentity = new Office365Identity(getTestIdentityName());
		officeIdentity.setFullName("Test Junit");
		// and a valid role
		officeIdentity.addRole(connector.getRoleByName(validRoleName));
		try {
			try {
				connector.createRole(buildRole(dummy1RoleId, dummy1RoleId + "@something.com", dummy1RoleId));
				try {
					connector.createRole(buildRole(dummy2RoleId, dummy2RoleId + "@something.com", dummy2RoleId));
					try {
						officeIdentity = connector.createIdentity(officeIdentity, getTestIdentityPassword().toCharArray());
						// when the changes are updated
						officeIdentity.setFullName("Test JunitChanged");
						officeIdentity.setRoles(new Role[] { connector.getRoleByName(dummy1RoleId),
								connector.getRoleByName(dummy2RoleId) });
						connector.updateIdentity(officeIdentity);

						// then the identity from data source should reflect the changes
						Identity officeIdentityFromSource = connector
								.getIdentityByName(officeIdentity.getPrincipalName());
						Assert.assertEquals("Full name should be same as updated name", officeIdentity.getFullName(),
								officeIdentityFromSource.getFullName());
						Assert.assertEquals("Total Roles assigned should be 2", 2,
								officeIdentityFromSource.getRoles().length);
						assertRoleIsPresent(officeIdentityFromSource.getRoles(), dummy1RoleId);
						assertRoleIsPresent(officeIdentityFromSource.getRoles(), dummy2RoleId);
					} finally {
						connector.deleteIdentity(getTestIdentityName());
					}
				} finally {
					connector.deleteRole(dummy2RoleId);
				}
			} catch (PrincipalAlreadyExistsException paee) {
				/* This may be caused by an interrupted test, so cleanup this time */
				connector.deleteRole(dummy2RoleId);
				throw paee;
			} finally {
				connector.deleteRole(dummy1RoleId);
			}
		} catch (PrincipalAlreadyExistsException paee) {
			/* This may be caused by an interrupted test, so cleanup this time */
			connector.deleteRole(dummy1RoleId);
			throw paee;
		}
	}

	private void assertRoleIsPresent(Role[] roles, String principalName) {
		for (Role role : roles) {
			if (role.getPrincipalName().equals(principalName))
				return;
		}
		Assert.fail("Role not found " + principalName);
	}

	@Override
	protected Identity getIdentity(String identityName) {
		return new Office365Identity(identityName);
	}
}
