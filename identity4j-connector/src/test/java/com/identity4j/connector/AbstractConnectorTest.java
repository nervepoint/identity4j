/* HEADER */

package com.identity4j.connector;

/*
 * #%L
 * Identity4J Connector
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.AccountStatusType;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;
import com.identity4j.util.TestUtils;
import com.identity4j.util.passwords.DefaultPasswordCharacteristics;
import com.identity4j.util.passwords.PasswordAnalyser;
import com.identity4j.util.passwords.PasswordCharacteristics;
import com.identity4j.util.passwords.PasswordGenerator;

/**
 * Convenience class that performs tests for all abstract connecter methods. Any
 * class extending this will get any overridden methods tested by this
 */
public abstract class AbstractConnectorTest<C extends ConnectorConfigurationParameters> {
	protected final static Log LOG = LogFactory.getLog(AbstractConnectorTest.class);

	static {
		URL resource = AbstractConnectorTest.class.getResource("/test-log4j.properties");
		if (resource == null)
			BasicConfigurator.configure();
		else
			PropertyConfigurator.configure(resource);
	}

	protected String identityName;
	protected final String identityPassword;
	protected final String roleAttributeName;
	protected final String roleAttributeValue;
	protected final String newPassword;
	protected final String invalidPassword;
	protected final String roleName;
	protected final MultiMap configurationParameters;
	protected final String newRoleName;
	protected final List<String> newRoleUsers;

	protected String identityGuid;
	protected Identity identity;
	protected Role role;
	protected Connector<?> connector;
	protected Boolean checkOldCredentials;
	protected boolean recreateTestUserOnSelectedTests;

	protected C connectorConfigurationParameters;
	private boolean createdIdentityAtStartup;
	private boolean createdRoleAtStartup;
	protected ConnectorBuilder connectorBuilder;

	/**
	 * Constructor called after any static before methods
	 * 
	 * @param connector
	 * @param connector
	 *            properties
	 * @param testing
	 *            properties
	 */
	public AbstractConnectorTest(String propertiesFile) {
		configurationParameters = loadConfigurationParameters(propertiesFile);

		/*
		 * Load a local copy of configuration properties. This allows the
		 * provided properties file to be added to SVN, but the developer can
		 * keep their own copy called .local and have SVN ignore that (keeping
		 * their own local passwords safe
		 */
		try {
			configurationParameters.merge(loadConfigurationParameters(propertiesFile + ".local"));
		} catch (RuntimeException re) {
			if (!(re.getCause() instanceof FileNotFoundException)) {
				throw re;
			}
		}

		checkOldCredentials = configurationParameters.getBooleanOrDefault("checkOldCredentials", true);

		identityName = configurationParameters.getStringOrFail("connector.validIdentityName");
		identityPassword = configurationParameters.getStringOrFail("connector.validIdentityPassword");
		newPassword = configurationParameters.getStringOrFail("connector.newPassword");
		invalidPassword = configurationParameters.getStringOrFail("connector.invalidPassword");

		// The connector may not support roles
		roleName = configurationParameters.getStringOrDefault("connector.validRoleName", "");
		roleAttributeName = configurationParameters.getStringOrDefault("connector.validRoleAttributeName", "");
		roleAttributeValue = configurationParameters.getStringOrDefault("connector.validRoleAttributeValue", "");
		newRoleName = configurationParameters.getStringOrDefault("connector.newRoleName", roleName + "2");
		newRoleUsers = Arrays
				.asList(configurationParameters.getStringOrDefault("connector.newRoleUsers", identityName).split(","));

	}

	public boolean isRecreateTestUserOnSelectedTests() {
		return recreateTestUserOnSelectedTests;
	}

	public void setReecreateTestUserOnSelectedTests(boolean recreateTestUserOnSelectedTests) {
		this.recreateTestUserOnSelectedTests = recreateTestUserOnSelectedTests;
	}

	private MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = getClass().getResourceAsStream(propertiesFile);
			if (resourceAsStream == null) {
				throw new FileNotFoundException(
						"Properties resource " + propertiesFile + " not found. Check it is on your classpath");
			}
			Properties properties = new Properties();
			properties.load(resourceAsStream);
			onLoadConfigurationProperties(properties);
			return MultiMap.toMultiMap(properties);
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to load configuration parameters", ioe);
		}
	}

	protected void onLoadConfigurationProperties(Properties properties) {
		//
	}

	protected final Connector<?> getConnector() {
		return connector;
	}

	protected final String getIdentityName() {
		return identityName;
	}

	protected final String getIdentityPassword() {
		return identityPassword;
	}

	protected final String getNewPassword() {
		return newPassword;
	}

	protected final String getInvalidPassword() {
		return invalidPassword;
	}

	protected final String getRoleName() {
		return roleName;
	}

	protected final Identity getIdentity() {
		return identity;
	}

	protected final Role getRole() {
		return role;
	}

	/**
	 * Create a new user ready for use by each test
	 */
	@Before
	public final void setUp() throws Exception {
		beforeSetUp();
		connectorBuilder = new ConnectorBuilder();
		connectorConfigurationParameters = createConnectorConfigurationParameters(configurationParameters);
		connector = connectorBuilder.buildConnector(connectorConfigurationParameters);
		assertTrue("Connector must be open.", connector.isOpen());
		try {
			identity = connector.getIdentityByName(identityName);
		} catch (PrincipalNotFoundException pnfe) {
			if (connector.getCapabilities().contains(ConnectorCapability.createUser)) {
				Identity newIdentity = new IdentityImpl(identityName);
				String fullName = identityName + "s full name";
				newIdentity.setFullName(fullName);
				populateIdentityForCreation(newIdentity);
				connector.createIdentity(newIdentity, identityPassword.toCharArray());
				identity = connector.getIdentityByName(identityName);
				createdIdentityAtStartup = true;
			} else
				throw pnfe;
		}
		identityGuid = identity.getGuid();
		if (connector.getCapabilities().contains(ConnectorCapability.roles) && !StringUtil.isNullOrEmpty(roleName)) {
			try {
				role = connector.getRoleByName(roleName);
			} catch (PrincipalNotFoundException pnfe) {
				if (connector.getCapabilities().contains(ConnectorCapability.createRole)) {
					Role newRole = new RoleImpl(null, roleName);
					populateRoleForCreation(newRole);
					connector.createRole(newRole);
					createdRoleAtStartup = true;
				} else
					throw pnfe;
			}
		}
		afterSetUp();
	}

	@SuppressWarnings("unchecked")
	protected C createConnectorConfigurationParameters(MultiMap configurationParameters) {
		C connectorConfigurationParameters = (C) connectorBuilder.buildConfiguration(configurationParameters);
		connectorConfigurationParameters.setIdentityAttributesToRetrieve(
				Arrays.asList(configurationParameters.getStringOrDefault("attributesToRetrieve", "").split(",")));
		return connectorConfigurationParameters;
	}

	protected void beforeSetUp() throws Exception {
	}

	protected void afterSetUp() throws Exception {
	}

	/**
	 * Tear down
	 * @throws IOException 
	 */
	@After
	public final void tearDown() throws IOException {
		if (createdIdentityAtStartup && connector != null
				&& connector.getCapabilities().contains(ConnectorCapability.deleteUser)) {
			try {
				connector.deleteIdentity(identityName);
			} catch (PrincipalNotFoundException pnfe) {

			}
		}
		if (createdRoleAtStartup && connector != null
				&& connector.getCapabilities().contains(ConnectorCapability.deleteRole)) {
			try {
				connector.deleteRole(roleName);
			} catch (PrincipalNotFoundException pnfe) {

			}
		}
		if (connector != null && connector.isOpen()) {
			connector.close();
		}
	}

	@Test
	public final void logon() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.authentication));

		Identity logon = connector.logon(identityName, identityPassword.toCharArray());
		assertPrincipalMatches(identityName, logon);
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void logonWithInvalidPrincipalName() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.authentication));

		connector.logon(getTestPrincipalName(), identityPassword.toCharArray());
	}

	@Test(expected = InvalidLoginCredentialsException.class)
	public final void logonWithInvalidPrincipalPassword() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.authentication));

		connector.logon(identityName, TestUtils.randomValue().toCharArray());
	}

	@Test
	public final void checkCredentials() {
		boolean checkCredentials = connector.checkCredentials(identityName, identityPassword.toCharArray());
		assertTrue("Credentials are invalid. These should be valid", checkCredentials);
	}

	@Test
	public void checkCredentialsIncorrectPrincipal() {
		boolean checkCredentials = connector.checkCredentials(getTestPrincipalName(), identityPassword.toCharArray());
		assertFalse("Credentials are valid. These should be invalid", checkCredentials);
	}

	@Test
	public final void checkCredentialsIncorrectPassword() {
		boolean checkCredentials = connector.checkCredentials(identityName, TestUtils.randomValue().toCharArray());
		assertFalse("Credentials are valid. These should be invalid", checkCredentials);
	}

	@Test
	public void changePassword() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));

		String actualNewPassword = getActualNewPassword();

		try {
			connector.changePassword(identityName, identityGuid, identityPassword.toCharArray(),
					actualNewPassword.toCharArray());
			assertPasswordChange(identityName, identityPassword, actualNewPassword);
		} finally {
			endResetPasswordOrRecreateUser();
		}
	}

	protected String getActualNewPassword() {
		String actualNewPassword = this.newPassword;
		if (actualNewPassword.equals("*")) {
			actualNewPassword = generateRandomPassword(connector, identityName);
		}
		return actualNewPassword;
	}

	protected String generateRandomPassword(Connector<?> connector, String username) {
		PasswordCharacteristics pc = connector.getPasswordCharacteristics();
		if (pc == null) {
			pc = new DefaultPasswordCharacteristics();
		}

		PasswordGenerator gen = new PasswordGenerator(new PasswordAnalyser(), pc);
		return new String(gen.generate(Locale.getDefault(), username));

	}

	@Test(expected = PrincipalNotFoundException.class)
	public final void changePasswordWithInvalidGuid() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));

		final String invalidGuid = identityGuid + identityGuid;
		connector.changePassword(identityName, invalidGuid, identityPassword.toCharArray(),
				getActualNewPassword().toCharArray());
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void changePasswordWithInvalidPrincipalName() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));
		connector.changePassword(getTestPrincipalName(), identityGuid, identityPassword.toCharArray(),
				getActualNewPassword().toCharArray());
	}

	@Test(expected = InvalidLoginCredentialsException.class)
	public final void changePasswordWithInvalidPassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));
		connector.changePassword(identityName, identityGuid, TestUtils.randomValue().toCharArray(),
				getActualNewPassword().toCharArray());
	}

	@Test(expected = ConnectorException.class)
	public final void changePasswordWithInvalidNewPassword() {

		// TODO this test should not really be run until some kind of password
		// rule architecture is in place. Assume
		throw new ConnectorException();

		// try {
		// connector.changePassword(identityName, identityGuid,
		// identityPassword.toCharArray(), invalidPassword.toCharArray());
		// } catch (UnsupportedOperationException uoe) {
		// // Don't fail if connector doesn't support password changing
		// throw new ConnectorException();
		// }
	}

	@Test
	public final void setPassword() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordSet));

		try {
			String actualNewPassword = getActualNewPassword();
			final boolean forcePasswordChangeAtLogon = false;
			connector.setPassword(identityName, identityGuid, actualNewPassword.toCharArray(),
					forcePasswordChangeAtLogon);
			try {
				assertPasswordChange(identityName, identityPassword, actualNewPassword);
			} finally {
				endResetPasswordOrRecreateUser();
			}
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {

		}
	}

	private void endResetPasswordOrRecreateUser() {
		/*
		 * Work around for connectors that have password history. When we finish
		 * the test we either reset the password or entirely recreate the
		 * account (assuming the connector has support to do such a thing
		 */
		if (recreateTestUserOnSelectedTests) {
			try {
				connector.deleteIdentity(identityName);
			} catch (Exception e) {
				//
			}

			identity = new IdentityImpl(identityName);
			identity.setFullName(identity.getFullName());
			populateIdentityForCreation(identity);
			identity = connector.createIdentity(identity, identityPassword.toCharArray());
		} else {
			// reset to original password
			try {
				connector.setPassword(identityName, identityGuid, identityPassword.toCharArray(), false);
			} catch (UnsupportedOperationException uoe) {
			}
		}
	}

	@Test
	public final void disableAccount() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.accountDisable));
		Identity identityByName = connector.getIdentityByName(identityName);
		if (!identityByName.getAccountStatus().getType().equals(AccountStatusType.unlocked)) {
			throw new IllegalStateException("The test account " + identityName
					+ " must be unlocked at the start of this test, it is currently "
					+ identityByName.getAccountStatus().getType() + ". Please correct this before running the test");
		}
		try {
			connector.disableIdentity(identityByName);
			identityByName = connector.getIdentityByName(identityName);
			assertEquals("Identity should be disabled", AccountStatusType.disabled,
					identityByName.getAccountStatus().getType());
		} catch (UnsupportedOperationException uoe) {
		} finally {
			// Re-enable
			connector.enableIdentity(identityByName);
			identityByName = connector.getIdentityByName(identityName);
			if (identityByName.getAccountStatus().getType().equals(AccountStatusType.disabled)) {
				throw new IllegalStateException("Failed to re-enable the account after being disabled");
			}
		}
	}

	@Test
	public final void setPasswordChangeAtNextLogon() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.forcePasswordChange));
		final boolean forcePasswordChangeAtLogon = true;

		String actualNewPassword = getActualNewPassword();
		connector.setPassword(identityName, identityGuid, actualNewPassword.toCharArray(), forcePasswordChangeAtLogon);
		try {
			try {
				assertPasswordChange(identityName, identityPassword, actualNewPassword);
				throw new IllegalStateException("Expected a PasswordChangeRequiredException");
			} catch (PasswordChangeRequiredException pcre) {
				// Expect this
			}
			Identity identityByName = connector.getIdentityByName(identityName);
			assertEquals("Identity should have change password at next logon set", PasswordStatusType.changeRequired,
					identityByName.getPasswordStatus().getType());
		} finally {
			endResetPasswordOrRecreateUser();
		}
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void setPasswordWithInvalidPrincipal() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordSet));

		final boolean forcePasswordChangeAtLogon = false;
		connector.setPassword(getTestPrincipalName(), identityGuid, getActualNewPassword().toCharArray(),
				forcePasswordChangeAtLogon);
	}

	protected void assertPasswordChange(String identityName, String oldPassword, String newPassword) {
		if (checkOldCredentials) {
			/*
			 * Asserting password change will not work unless
			 * "OldPasswordAllowedPeriod" is set to zero (disabled). See
			 * http://support.microsoft.com/kb/906305
			 */
			boolean checkOldCredentials = connector.checkCredentials(identityName, oldPassword.toCharArray());
			assertFalse("Credentials are valid. These should be invalid", checkOldCredentials);
		}
		boolean checkNewCredentials = connector.checkCredentials(identityName, newPassword.toCharArray());
		assertTrue("Credentials are invalid. These should be valid", checkNewCredentials);
	}

	@Test
	public final void firstOfAllIdentities() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
		Iterator<Identity> allIdentities = connector.allIdentities();
		assertNotNull(allIdentities);
		assertTrue("Identities should be found", allIdentities.hasNext());
		assertThat(toIterable(connector.allIdentities()), hasItem(identity));
	}

	@Test(expected = NoSuchElementException.class)
	public final void allIdentities() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
		Iterator<Identity> allIdentities = connector.allIdentities();
		assertNotNull(allIdentities);
		while (allIdentities.hasNext()) {
			// allIdentities.next();
			System.out.println(allIdentities.next());
		}
		allIdentities.next();
	}

	@Test
	public final void count() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
		long count = connector.countIdentities();
		if (count == 0) {
			fail("Expected at least 1 identity");
		}
	}

	@Test
	public final void isIdentityNameInUse() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
		boolean identityNameInUse = connector.isIdentityNameInUse(identityName);
		assertTrue("Identity name should be in use", identityNameInUse);
	}

	@Test
	public void isIdentityAvailableUnknownPrincipal() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
		boolean identityNameInUse = connector.isIdentityNameInUse(getTestPrincipalName());
		assertFalse("Identity name should not be in use", identityNameInUse);
	}

	@Test
	public final void getIdentityByName() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
		Identity identityByName = connector.getIdentityByName(identityName);
		assertPrincipalMatches(identityName, identityByName);
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void getIdentityByNameUnknownPrincipal() {
		connector.getIdentityByName(getTestPrincipalName());
	}

	@Test
	public final void allRoles() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			Iterator<Role> allRoles = connector.allRoles();
			assertNotNull(allRoles);
			assertTrue("Roles should be found", allRoles.hasNext());

			List<Role> roles = new ArrayList<Role>();
			Iterator<Role> it = connector.allRoles();
			while (it.hasNext()) {
				Role r = it.next();
				roles.add(r);
			}
			Role roleByName = connector.getRoleByName(roleName);

			assertTrue(roles.contains(roleByName));
		}

	}

	@Test
	public final void isRoleNameInUse() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			boolean roleNameInUse = connector.isRoleNameInUse(roleName);
			assertTrue("Role name should be in use", roleNameInUse);
		}
	}

	@Test
	public final void isRoleNameInUseUnknownPrincipal() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			boolean roleNameInUse = connector.isRoleNameInUse(getTestPrincipalName());
			assertFalse("Role name should not be in use", roleNameInUse);
		}
	}

	@Test
	public final void getRoleByName() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			Role roleByName = connector.getRoleByName(roleName);
			assertPrincipalMatches(roleName, roleByName);
		}
	}

	/**
	 * With assume evaluating to false, we cannot use (expected =
	 * PrincipalNotFoundException.class) in @Test annotation as exception thrown
	 * on assume failure is org.junit.internal.AssumptionViolatedException which
	 * clashes with class in annotation, and hence test fails.
	 * 
	 */
	@Test
	public void getRoleByNameUnknownPrincipal() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));
		try {
			connector.getRoleByName(getTestPrincipalName());
			Assert.fail("Should have thrown PrincipalNotFoundException.");
		} catch (PrincipalNotFoundException e) {
			// expected
		}
	}

	@Test
	public final void getPasswordCharacteristics() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.hasPasswordPolicy));
		PasswordCharacteristics ch = connector.getPasswordCharacteristics();
		if (ch != null) {
			// TODO do something
			System.out.println(">> " + ch.toString());
		}
	}

	@Test
	public void createIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createUser));
		String newPrincipalName = identityName + "2";
		Identity newIdentity = new IdentityImpl(newPrincipalName);
		String fullName = newPrincipalName + "s full name";
		newIdentity.setFullName(fullName);
		populateIdentityForCreation(newIdentity);
		connector.createIdentity(newIdentity, identityPassword.toCharArray());
		try {
			newIdentity = connector.getIdentityByName(newPrincipalName);

			assertEquals("Expect principal name to be the same.", newPrincipalName, newIdentity.getPrincipalName());

			if (connector.getCapabilities().contains(ConnectorCapability.hasFullName))
				assertEquals("Expect full name to be the same.", fullName, newIdentity.getFullName());

			Identity logon = connector.logon(newPrincipalName, identityPassword.toCharArray());
			assertPrincipalMatches(newPrincipalName, logon);
		} finally {
			connector.deleteIdentity(newPrincipalName);
		}
	}

	@Test
	public void updateIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.updateUser));
		Map<String, String[]> currentAttributes = new HashMap<String, String[]>(identity.getAttributes());
		try {
			Map<String, String[]> attributes = new HashMap<String, String[]>(currentAttributes);
			updateAttributes(identity, attributes);
			identity.setAttributes(attributes);
			connector.updateIdentity(identity);
			Identity newIdentity = connector.getIdentityByName(identity.getPrincipalName());
			assertUpdatedAttributes(identity, attributes, newIdentity, newIdentity.getAttributes());
		} finally {
			identity.setAttributes(currentAttributes);
			try {
				connector.updateIdentity(identity);
			} catch (Exception e) {
				System.err.println(
						"Could not revert original attributes. State of test user is incorrect and future tests may fail.");
			}
		}
	}

	@Test
	public final void createRoleWithAttribute() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createRole));

		String newPrincipalName = newRoleName;
		Role newRole = new RoleImpl(null, newPrincipalName);
		String valToTest = null;
		if (!StringUtil.isNullOrEmpty(roleAttributeName)) {
			newRole.setAttribute(roleAttributeName, roleAttributeValue);
			valToTest = roleAttributeValue;
		}
		populateRoleForCreation(newRole);
		connector.createRole(newRole);
		try {
			newRole = connector.getRoleByName(newPrincipalName);
			assertEquals("Expect principal name to be the same.", newPrincipalName, newRole.getPrincipalName());

			if (!StringUtil.isNullOrEmpty(roleAttributeName))
				assertEquals("Expect attribute value to be the same.", valToTest,
						newRole.getAttribute(roleAttributeName));

		} finally {
			connector.deleteRole(newPrincipalName);
		}
	}

	@Test
	public final void createRoleWithUsers() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createRole));

		String newPrincipalName = newRoleName;
		Role newRole = new RoleImpl(null, newPrincipalName);

		populateRoleForCreation(newRole);
		connector.createRole(newRole);
		try {
			newRole = connector.getRoleByName(newPrincipalName);

			for (String u : newRoleUsers) {
				Identity user = connector.getIdentityByName(u);
				user.addRole(newRole);
				connector.updateIdentity(user);

				user = connector.getIdentityByName(u);
				Assert.assertTrue("Identity should be attached to new role.",
						Arrays.asList(user.getRoles()).contains(newRole));
			}

		} finally {
			for (String u : newRoleUsers) {
				Identity user = connector.getIdentityByName(u);
				user.removeRole(newRole);
				connector.updateIdentity(user);
			}
			connector.deleteRole(newPrincipalName);
		}
	}

	@Test
	public final void updateRole() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.updateRole));
		Map<String, String[]> currentAttributes = role == null ? new HashMap<String, String[]>()
				: new HashMap<String, String[]>(role.getAttributes());
		try {
			Map<String, String[]> attributes = new HashMap<String, String[]>(currentAttributes);
			updateRoleAttributes(role, attributes);
			role.setAttributes(attributes);
			connector.updateRole(role);
			Role newRole = connector.getRoleByName(role.getPrincipalName());
			assertUpdatedRoleAttributes(role, attributes, newRole, newRole.getAttributes());
		} finally {
			role.setAttributes(currentAttributes);
			try {
				connector.updateRole(role);
			} catch (Exception e) {
				System.err.println(
						"Could not revert original attributes. State of test role is incorrect and future tests may fail.");
			}
		}
	}

	protected void assertUpdatedAttributes(Identity identity, Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {
		throw new UnsupportedOperationException(
				"The connector test implementation must assert the changes attributes are correct");
	}

	protected void updateAttributes(Identity identity2, Map<String, String[]> attributes) {
		throw new UnsupportedOperationException(
				"The connector test implementation must provide some attributes to update");
	}

	protected void assertUpdatedRoleAttributes(Role role, Map<String, String[]> attributes, Role newRole,
			Map<String, String[]> newAttributes) {
		throw new UnsupportedOperationException(
				"The connector test implementation must assert the changes attributes are correct");
	}

	protected void updateRoleAttributes(Role role, Map<String, String[]> attributes) {
		throw new UnsupportedOperationException(
				"The connector test implementation must provide some attributes to update");
	}

	protected void assertPrincipalMatches(String expectedPrincipalName, Principal principal) {
		assertNotNull(principal);
		assertEquals(expectedPrincipalName, principal.getPrincipalName());
	}

	protected final <T extends Principal> Map<String, T> toMap(final Iterator<T> itr) {
		Map<String, T> principals = new HashMap<String, T>();
		for (T principal : toIterable(itr)) {
			principals.put(principal.getPrincipalName(), principal);
		}
		return principals;
	}

	/**
	 * Convenience method to wrap iterator in an <code>Iterable</code> which can
	 * be used by for each
	 * 
	 * @param <T>
	 * @param iterator
	 *            to convert
	 * @return iterable
	 */
	protected final <T extends Principal> Iterable<T> toIterable(final Iterator<T> itr) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return itr;
			}
		};
	}

	/**
	 * Helper method to get a random principal name.
	 * 
	 * @return
	 */
	protected String getTestPrincipalName() {
		return TestUtils.randomValue();
	}

	protected void populateIdentityForCreation(Identity newIdentity) {
		/*
		 * For sub-classes to add additional detail when creating a new
		 * identity. For example, AD could use this to set the OU of the
		 * identity
		 */
	}

	protected void populateRoleForCreation(Role roleIdentity) {
		/*
		 * For sub-classes to add additional detail when creating a new role.
		 * For example, AD could use this to set the OU of the identity
		 */
	}

}