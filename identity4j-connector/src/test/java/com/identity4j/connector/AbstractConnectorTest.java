/* HEADER */

package com.identity4j.connector;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

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
import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;
import com.identity4j.util.TestUtils;
import com.identity4j.util.passwords.PasswordCharacteristics;

/**
 * Convenience class that performs tests for all abstract connecter methods. Any
 * class extending this will get any overridden methods tested by this
 */
public abstract class AbstractConnectorTest {

	static {
		PropertyConfigurator.configure(AbstractConnectorTest.class
				.getResource("/test-log4j.properties"));
	}

	protected final String identityName;
	protected final String identityPassword;
	protected final String newPassword;
	protected final String invalidPassword;
	protected final String roleName;
	protected final MultiMap configurationParameters;

	protected String identityGuid;
	protected Identity identity;
	protected Connector connector;
	protected Boolean checkOldCredentials;

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
			configurationParameters
					.merge(loadConfigurationParameters(propertiesFile
							+ ".local"));
		} catch (RuntimeException re) {
			if (!(re.getCause() instanceof FileNotFoundException)) {
				throw re;
			}
		}

		checkOldCredentials = configurationParameters.getBooleanOrDefault(
				"checkOldCredentials", true);

		identityName = configurationParameters
				.getStringOrFail("connector.validIdentityName");
		identityPassword = configurationParameters
				.getStringOrFail("connector.validIdentityPassword");
		newPassword = configurationParameters
				.getStringOrFail("connector.newPassword");
		invalidPassword = configurationParameters
				.getStringOrFail("connector.invalidPassword");

		// The connector may not support roles
		roleName = configurationParameters.getStringOrDefault(
				"connector.validRoleName", "");
	}

	private MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = getClass().getResourceAsStream(
					propertiesFile);
			if (resourceAsStream == null) {
				throw new FileNotFoundException("Properties resource "
						+ propertiesFile
						+ " not found. Check it is on your classpath");
			}
			Properties properties = new Properties();
			properties.load(resourceAsStream);
			onLoadConfigurationProperties(properties);
			return MultiMap.toMultiMap(properties);
		} catch (IOException ioe) {
			throw new RuntimeException(
					"Failed to load configuration parameters", ioe);
		}
	}

	protected void onLoadConfigurationProperties(Properties properties) {
		//
	}

	protected final Connector getConnector() {
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

	/**
	 * Create a new user ready for use by each test
	 */
	@Before
	public final void setUp() throws Exception {
		beforeSetUp();
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		ConnectorConfigurationParameters parms = connectorBuilder
				.buildConfiguration(configurationParameters);
		parms.setIdentityAttributesToRetrieve(Arrays
				.asList(configurationParameters.getStringOrDefault(
						"attributesToRetrieve", "").split(",")));
		connector = connectorBuilder.buildConnector(parms);
		assertTrue("Connector must be open.", connector.isOpen());
		identity = connector.getIdentityByName(identityName);
		identityGuid = identity.getGuid();
	}

	protected void beforeSetUp() throws Exception {
	}

	/**
	 * Tear down
	 */
	@After
	public final void tearDown() {
		if (connector != null && connector.isOpen()) {
			connector.close();
		}
	}

	@Test
	public final void logon() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.authentication));

		Identity logon = connector.logon(identityName,
				identityPassword.toCharArray());
		assertPrincipalMatches(identityName, logon);
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void logonWithInvalidPrincipalName() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.authentication));

		connector
				.logon(getTestPrincipalName(), identityPassword.toCharArray());
	}

	@Test(expected = InvalidLoginCredentialsException.class)
	public final void logonWithInvalidPrincipalPassword() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.authentication));

		connector.logon(identityName, TestUtils.randomValue().toCharArray());
	}

	@Test
	public final void checkCredentials() {
		boolean checkCredentials = connector.checkCredentials(identityName,
				identityPassword.toCharArray());
		assertTrue("Credentials are invalid. These should be valid",
				checkCredentials);
	}

	@Test
	public void checkCredentialsIncorrectPrincipal() {
		boolean checkCredentials = connector.checkCredentials(
				getTestPrincipalName(), identityPassword.toCharArray());
		assertFalse("Credentials are valid. These should be invalid",
				checkCredentials);
	}

	@Test
	public final void checkCredentialsIncorrectPassword() {
		boolean checkCredentials = connector.checkCredentials(identityName,
				TestUtils.randomValue().toCharArray());
		assertFalse("Credentials are valid. These should be invalid",
				checkCredentials);
	}

	@Test
	public void changePassword() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));

		try {
			connector.changePassword(identityName, identityGuid,
					identityPassword.toCharArray(), newPassword.toCharArray());
			assertPasswordChange(identityName, identityPassword, newPassword);
		} finally {
			// reset to original password
			connector.setPassword(identityName, identityGuid,
					identityPassword.toCharArray(), false);
		}
	}

	@Test(expected = PrincipalNotFoundException.class)
	public final void changePasswordWithInvalidGuid() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));

		final String invalidGuid = identityGuid + identityGuid;
		connector.changePassword(identityName, invalidGuid,
				identityPassword.toCharArray(), newPassword.toCharArray());
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void changePasswordWithInvalidPrincipalName() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));
		connector.changePassword(getTestPrincipalName(), identityGuid,
				identityPassword.toCharArray(), newPassword.toCharArray());
	}

	@Test(expected = InvalidLoginCredentialsException.class)
	public final void changePasswordWithInvalidPassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));
		connector.changePassword(identityName, identityGuid, TestUtils.randomValue().toCharArray(), newPassword.toCharArray());
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

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordSet));
		try {
			final boolean forcePasswordChangeAtLogon = false;
			connector.setPassword(identityName, identityGuid,
					newPassword.toCharArray(), forcePasswordChangeAtLogon);
			try {
				assertPasswordChange(identityName, identityPassword,
						newPassword);
			} finally {
				// reset to original password
				try {
					connector.setPassword(identityName, identityGuid,
							identityPassword.toCharArray(), false);
				} catch (UnsupportedOperationException uoe) {
				}
			}
		} finally {

		}
	}

	@Test
	public final void disableAccount() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.accountDisable));
		Identity identityByName = connector.getIdentityByName(identityName);
		if (!identityByName.getAccountStatus().getType()
				.equals(AccountStatusType.unlocked)) {
			throw new IllegalStateException(
					"The test account "
							+ identityName
							+ " must be unlocked at the start of this test, it is currently "
							+ identityByName.getAccountStatus().getType()
							+ ". Please correct this before running the test");
		}
		try {
			connector.disableIdentity(identityByName);
			identityByName = connector.getIdentityByName(identityName);
			assertEquals("Identity should be disabled",
					AccountStatusType.disabled, identityByName
							.getAccountStatus().getType());
		} catch (UnsupportedOperationException uoe) {
		} finally {
			// Re-enable
			connector.enableIdentity(identityByName);
			identityByName = connector.getIdentityByName(identityName);
			if (identityByName.getAccountStatus().getType()
					.equals(AccountStatusType.disabled)) {
				throw new IllegalStateException(
						"Failed to re-enable the account after being disabled");
			}
		}
	}

	@Test
	public final void setPasswordChangeAtNextLogon() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.forcePasswordChange));
		final boolean forcePasswordChangeAtLogon = true;

		connector.setPassword(identityName, identityGuid,
				newPassword.toCharArray(), forcePasswordChangeAtLogon);
		try {
			try {
				assertPasswordChange(identityName, identityPassword,
						newPassword);
				throw new IllegalStateException(
						"Expected a PasswordChangeRequiredException");
			} catch (PasswordChangeRequiredException pcre) {
				// Expect this
			}
			Identity identityByName = connector.getIdentityByName(identityName);
			assertEquals(
					"Identity should have change password at next logon set",
					PasswordStatusType.changeRequired, identityByName
							.getPasswordStatus().getType());
		} finally {
			// reset to original password
			connector.setPassword(identityName, identityGuid,
					identityPassword.toCharArray(), false);
		}
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void setPasswordWithInvalidPrincipal() {

		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordSet));

		final boolean forcePasswordChangeAtLogon = false;
		connector.setPassword(getTestPrincipalName(), identityGuid,
				newPassword.toCharArray(), forcePasswordChangeAtLogon);
	}

	protected void assertPasswordChange(String identityName, String oldPassword,
			String newPassword) {
		if (checkOldCredentials) {
			/*
			 * Asserting password change will not work unless
			 * "OldPasswordAllowedPeriod" is set to zero (disabled). See
			 * http://support.microsoft.com/kb/906305
			 */
			boolean checkOldCredentials = connector.checkCredentials(
					identityName, oldPassword.toCharArray());
			assertFalse("Credentials are valid. These should be invalid",
					checkOldCredentials);
		}
		boolean checkNewCredentials = connector.checkCredentials(identityName,
				newPassword.toCharArray());
		assertTrue("Credentials are invalid. These should be valid",
				checkNewCredentials);
	}

	@Test
	public final void firstOfAllIdentities() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		Iterator<Identity> allIdentities = connector.allIdentities();
		assertNotNull(allIdentities);
		assertTrue("Identities should be found", allIdentities.hasNext());
		assertThat(toIterable(connector.allIdentities()), hasItem(identity));
	}

	@Test(expected = NoSuchElementException.class)
	public final void allIdentities() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
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
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		long count = connector.countIdentities();
		if (count == 0) {
			fail("Expected at least 1 identity");
		}
	}

	@Test
	public final void isIdentityNameInUse() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		boolean identityNameInUse = connector.isIdentityNameInUse(identityName);
		assertTrue("Identity name should be in use", identityNameInUse);
	}

	@Test
	public void isIdentityAvailableUnknownPrincipal() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		boolean identityNameInUse = connector.isIdentityNameInUse(getTestPrincipalName());
		assertFalse("Identity name should not be in use", identityNameInUse);
	}

	@Test
	public final void getIdentityByName() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		Identity identityByName = connector.getIdentityByName(identityName);
		assertPrincipalMatches(identityName, identityByName);
	}

	@Test(expected = PrincipalNotFoundException.class)
	public void getIdentityByNameUnknownPrincipal() {
		connector.getIdentityByName(getTestPrincipalName());
	}

	@Test
	public final void allRoles() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			Iterator<Role> allRoles = connector.allRoles();
			assertNotNull(allRoles);
			assertTrue("Roles should be found", allRoles.hasNext());
			Role roleByName = connector.getRoleByName(roleName);
			List<Role> roles = new ArrayList<Role>();
			
			Iterator<Role> it = connector.allRoles();
			while(it.hasNext()) {
				Role r = it.next();
				roles.add(r);
			}
			
			assertTrue(roles.contains(roleByName));
		}

	}

	@Test
	public final void isRoleNameInUse() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			boolean roleNameInUse = connector.isRoleNameInUse(roleName);
			assertTrue("Role name should be in use", roleNameInUse);
		}
	}

	@Test
	public final void isRoleNameInUseUnknownPrincipal() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			boolean roleNameInUse = connector.isRoleNameInUse(getTestPrincipalName());
			assertFalse("Role name should not be in use", roleNameInUse);
		}
	}

	@Test
	public final void getRoleByName() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.roles));
		if (!StringUtil.isNullOrEmpty(roleName)) {
			Role roleByName = connector.getRoleByName(roleName);
			assertPrincipalMatches(roleName, roleByName);
		}
	}

	/**
	 * With assume evaluating to false, we cannot use (expected = PrincipalNotFoundException.class) in @Test annotation 
	 * as exception thrown on assume failure is org.junit.internal.AssumptionViolatedException which
	 * clashes with class in annotation, and hence test fails.
	 * 
	 */
	@Test
	public void getRoleByNameUnknownPrincipal() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.roles));
		try{
			connector.getRoleByName(getTestPrincipalName());
			Assert.fail("Should have thrown PrincipalNotFoundException.");
		}catch(PrincipalNotFoundException e){
			//expected
		}
	}

	@Test
	public final void getPasswordCharacteristics() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.hasPasswordPolicy));
		PasswordCharacteristics ch = connector.getPasswordCharacteristics();
		if (ch != null) {
			// TODO do something
			System.out.println(">> " + ch.toString());
		}
	}

	@Test
	public void createIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		String newPrincipalName = identityName + "2";
		Identity newIdentity = new IdentityImpl(newPrincipalName);
		String fullName = newPrincipalName + "s full name";
		newIdentity.setFullName(fullName);
		connector.createIdentity(newIdentity, identityPassword.toCharArray());
		try {
			newIdentity = connector.getIdentityByName(newPrincipalName);
			assertEquals("Expect full name to be the same.", fullName,
					newIdentity.getFullName());

			Identity logon = connector.logon(newPrincipalName,
					identityPassword.toCharArray());
			assertPrincipalMatches(newPrincipalName, logon);
		} finally {
			connector.deleteIdentity(newPrincipalName);
		}
	}

	@Test
	public void updateIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.updateUser));
		Map<String, String[]> currentAttributes = new HashMap<String, String[]>(
				identity.getAttributes());
		try {
			Map<String, String[]> attributes = new HashMap<String, String[]>(
					currentAttributes);
			updateAttributes(identity, attributes);
			identity.setAttributes(attributes);
			connector.updateIdentity(identity);
			Identity newIdentity = connector.getIdentityByName(identity
					.getPrincipalName());
			assertUpdatedAttributes(identity, attributes, newIdentity,
					newIdentity.getAttributes());
		} finally {
			identity.setAttributes(currentAttributes);
			try {
				connector.updateIdentity(identity);
			} catch (Exception e) {
				System.err
						.println("Could not revert original attributes. State of test user is incorrect and future tests may fail.");
			}
		}
	}


	@Test
	public final void createRole() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createRole));
		throw new UnsupportedOperationException(
				"Create role test has not been implemented");
	}

	@Test
	public final void updateRole() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.updateRole));
		throw new UnsupportedOperationException(
				"Update role test has not been implemented");
	}

	protected void assertUpdatedAttributes(Identity identity,
			Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {
		throw new UnsupportedOperationException(
				"The connector test implementation must assert the changes attributes are correct");
	}

	protected void updateAttributes(Identity identity2,
			Map<String, String[]> attributes) {
		throw new UnsupportedOperationException(
				"The connector test implementation must provide some attributes to update");
	}

	protected void assertPrincipalMatches(String expectedPrincipalName,
			Principal principal) {
		assertNotNull(principal);
		assertEquals(expectedPrincipalName, principal.getPrincipalName());
	}

	protected final <T extends Principal> Map<String, T> toMap(
			final Iterator<T> itr) {
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
	protected final <T extends Principal> Iterable<T> toIterable(
			final Iterator<T> itr) {
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
}