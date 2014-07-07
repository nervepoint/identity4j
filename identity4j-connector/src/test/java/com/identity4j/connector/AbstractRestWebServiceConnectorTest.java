package com.identity4j.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
import com.identity4j.util.MultiMap;
import com.identity4j.util.TestUtils;

/**
 * Abstract test class for testing REST based connector services.
 * <br />
 * It has common test cases that all connector should meet.
 * <br />
 * Specific test cases are present in supporting test base class.
 * 
 * @author gaurav
 *
 */
public abstract class AbstractRestWebServiceConnectorTest {

	protected static MultiMap configurationParameters;
	protected static Connector connector;
	
	protected static void init(String configFile) {
		PropertyConfigurator.configure(AbstractRestWebServiceConnectorTest.class.getResource("/test-log4j.properties"));
		
		configurationParameters = loadConfigurationParameters(configFile);
		ConnectorBuilder connectorBuilder = new ConnectorBuilder();
		ConnectorConfigurationParameters parms = connectorBuilder
				.buildConfiguration(configurationParameters);
		connector =  connectorBuilder.buildConnector(parms);
	}
	
	protected static MultiMap loadConfigurationParameters(String propertiesFile) {
		try {
			InputStream resourceAsStream = AbstractRestWebServiceConnectorTest.class.getResourceAsStream(
					propertiesFile);
			if (resourceAsStream == null) {
				
				File externalResource = new File("conf" + propertiesFile);
				if(externalResource.exists()) {
					resourceAsStream = new FileInputStream(externalResource);
				} else {
					throw new FileNotFoundException("Properties resource "
						+ propertiesFile
						+ " not found. Check it is on your classpath");
				}
			}
			Properties properties = new Properties();
			properties.load(resourceAsStream);
			return MultiMap.toMultiMap(properties);
		} catch (IOException ioe) {
			throw new RuntimeException(
					"Failed to load configuration parameters", ioe);
		}
	}
	
	protected String validIdentityName = null;
	protected String validIdentityId = null;
	protected String validIdentityPassword = null;
	protected String testIdentityName = null;
	protected String testIdentityPassword = null;
	protected String newPassword = null;
	protected String invalidIdentityName = null;
	protected String invalidPassword = null;
	
	/**
	 * Returns connector specific identity implementation instance.
	 * 
	 * @param identityName
	 * @return
	 */
	protected abstract Identity getIdentity(String identityName);
	
	@Test
	public void itShouldCreateIdentityIfNotPresentInDataSource() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		
		//given an identity not present in data store
		Identity identity = getIdentity(testIdentityName);
		identity.setFullName("Mock User");
		try {
			//when it is created in data store
			identity = connector.createIdentity(identity, testIdentityPassword.toCharArray());
			//then fetched instance from data store
			Identity identityFromSource = getIdentityFromSource(identity);
			//should have same assigned principal name
			Assert.assertEquals("Principal names should be same",
					identity.getPrincipalName(),
					identityFromSource.getPrincipalName());
		} finally {
			deleteIdentityFromSource(identity);
		}
	}

	@Test(expected=PrincipalAlreadyExistsException.class)
	public void itShouldThrowPrincipalAlreadyExistsExceptionIfIdentityIsPresentInDataSourceOnCreate() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.createUser));
		
		//given an identity already present in data store
		Identity identity = getIdentity(validIdentityName);
		identity.setFullName("Mock User");
		//when it is created in data store
		connector.createIdentity(identity, testIdentityPassword.toCharArray());
		//then PrincipalAlreadyExistsException should be thrown
	}
	
	@Test
	public void itShouldUpdateIdentityWithTheChangesPassed() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.updateUser));
		
		//given an identity
		Identity identity = getIdentity(testIdentityName);
		identity.setFullName("Test Junit");
		try {
			identity = connector.createIdentity(identity, testIdentityPassword.toCharArray());
			//when the changes are updated
			identity.setFullName("Test JunitChanged");
			connector.updateIdentity(identity);

			//then the identity from data source should reflect the changes
			Identity identityFromSource = getIdentityFromSource(identity);
			Assert.assertEquals("Full name should be same as updated name",
					identity.getFullName(),
					identityFromSource.getFullName());
		} finally {
			deleteIdentityFromSource(identity);
		}
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionIfToUpdateIdentityIsNotPresentInDataStore() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.updateUser));
		
		//given an identity not present in data store
		//when identity is updated
		Identity identity = getIdentity(invalidIdentityName);
		identity.setFullName("Mock User");
		connector.updateIdentity(identity);
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test
	public void itShouldDeleteIdentityIfPresentInDataStore() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.deleteUser));
		
		//given a valid identity present in data store
		Identity identity = getIdentity(testIdentityName);
		identity.setFullName("Mock User");
		
		identity = connector.createIdentity(identity, testIdentityPassword.toCharArray());
		//when identity is deleted
		connector.deleteIdentity(identity.getPrincipalName());

		//then it should be deleted from data source
		//attempt to find it in data source should throw PrincipalNotFoundException
		try {
			Identity identityFromSource = getIdentityFromSource(identity);
			Assert.fail("Identity supposed to be deleted found "
					+ identityFromSource.getPrincipalName());
		} catch (PrincipalNotFoundException e) {
			// ignore
		}
	}
	
	@Test(expected=PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionOnDeleteIfIdentityNotPresentInDataStore() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.deleteUser));
		
		//given an identity not present in data store
		//when identity is deleted
		connector.deleteIdentity(invalidIdentityName);
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test
	public void itShouldFetchValidUserIfPresentInDataStore() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		
		//given an identity present in data source
		//when it is fetched by principal name
		Identity identityFromSource = connector
				.getIdentityByName(validIdentityName);
		//then it should return identity instance
		assertPrincipalMatches(validIdentityName, identityFromSource);
	}
	
	@Test(expected = PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionForInValidUserNotPresentInDataStore() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		
		//given an identity not present in data source
		//when it is fetched by principal name
		connector.getIdentityByName(invalidIdentityName);
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test
	public void itShouldReturnTrueFlagIfIdentityNameIsInUse() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		//given a valid identity name present in data store
		//when a check is made for presence in data store
		boolean identityNameInUse = connector.isIdentityNameInUse(validIdentityName);
		//then it should return true flag
		assertTrue("Identity name should be in use", identityNameInUse);
	}
	
	@Test
	public void itShouldReturnFalseFlagIfIdentityNameIsNotInUse() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.identities));
		
		//given an identity name not present in data store
		//when a check is made for presence in data store
		boolean identityNameInUse = connector.isIdentityNameInUse(TestUtils
				.randomValue());
		//then it should return false flag
		assertFalse("Identity name should not be in use", identityNameInUse);
	}
	
	@Test
	public void itShouldReturnIdentityOnSuccessfulLogon() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.authentication));
		
		//given an identity present in data store, with correct password
		//when login is attempted
		Identity identity = connector.logon(
				validIdentityName,
				validIdentityPassword.toCharArray());
		//then it should login user to the system and return a valid instance
		//and should have same principal name
		Assert.assertEquals("Principal names should be same",
				validIdentityName,
						identity.getPrincipalName());
	}
	
	@Test(expected = InvalidLoginCredentialsException.class)
	public void itShouldThrowInvalidLoginCredentialsExceptionForBadCredentialsOnLogon() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.authentication));
		
		//given an identity present in data store, but invalid password
		//when login is attempted
		connector.logon(validIdentityName, invalidPassword.toCharArray());
		//then InvalidLoginCredentialsException should be thrown
	}
	
	@Test(expected = PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionForInValidUserNotPresentInDataStoreOnLogon() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.authentication));
		
		//given invalid identity not present in data store
		//when login is attempted
		connector.logon(invalidIdentityName, invalidPassword.toCharArray());
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test
	public void itShouldDisableAnIdentity(){
		//given a valid identity
		Identity identity = getIdentity(testIdentityName);
		identity.setFullName("Mock User");
		try {
			//and stored in data store
			identity = connector.createIdentity(identity, testIdentityPassword.toCharArray());
			
			
			//when it is disabled
			connector.disableIdentity(identity);
			
			Identity identityFromSource = getIdentityFromSource(identity);
			
			//then the account status disabled field should return true
			Assert.assertTrue("Identity is disabled",
					identityFromSource.getAccountStatus().isDisabled()
					);
			
			
		} finally {
			deleteIdentityFromSource(identity);
		}
	}
	
	@Test
	public void itShouldEnableAnIdentity(){
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.accountDisable));
		
		//given a valid identity
		Identity identity = getIdentity(testIdentityName);
		identity.setFullName("Mock User");
		Identity identityFromSource = null;
		try {
			//and stored in data store
			Identity identityCreated = connector.createIdentity(identity, testIdentityPassword.toCharArray());
			//which is disabled
			connector.disableIdentity(identityCreated);
			
			Assert.assertTrue("Identity is disabled",
					identityCreated.getAccountStatus().isDisabled()
					);
			//when the identity is enabled
			connector.enableIdentity(identityCreated);
			
			identityFromSource = getIdentityFromSource(identityCreated);
			
			//then the account status disabled field should return false
			Assert.assertFalse("Identity is enabled",
					identityFromSource.getAccountStatus().isDisabled()
					);
			
			
		} finally {
			if(identityFromSource != null)
				deleteIdentityFromSource(identityFromSource);
		}
	}
	
	@Test public void itShouldCreateARoleIfNotPresentInDataSource() {throw new UnsupportedOperationException("To be implemented.");}
	@Test public void itShouldThrowPrincipalAlreadyExistsExceptionIfRoleAlreadyPresentInDataSource() {throw new UnsupportedOperationException("To be implemented.");}
	
	@Test
	public void itShouldReturnTrueFlagForValidCredentials() {
		//given a valid user
		boolean checkCredentials = connector.checkCredentials(validIdentityName,
		//and valid password		
				validIdentityPassword.toCharArray());
		//when credential check is made
		//then it should return true flag
		assertTrue("Credentials are valid",
				checkCredentials);
	}
	
	@Test
	public void itShouldReturnFalseFlagForInValidPrincipal() {
		//given an identity with invalid principal name
		//when credential check is made
		boolean checkCredentials = connector.checkCredentials(
				TestUtils.randomValue(), validIdentityPassword.toCharArray());
		//then it should return false flag
		assertFalse("Credentials are in valid. These should be valid",
				checkCredentials);
	}
	
	@Test
	public void itShouldReturnFalseFlagForInValidPassword() {
		//given an identity with valid principal name
		boolean checkCredentials = connector.checkCredentials(validIdentityName,
				//and in correct password
				TestUtils.randomValue().toCharArray());
		//when credential check is made
		//then it should return false flag
		assertFalse("Credentials are in valid. These should be valid",
				checkCredentials);
	}
	
	@Test
	public void itShouldChangeThePasswordForValidIdentity() {
		
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));
		
		try {
			//given a valid identity with new password
			//when password change is attempted
			connector.changePassword(validIdentityName, validIdentityId,
					validIdentityPassword.toCharArray(), 
					newPassword.toCharArray());
			//then check with new password should return true flag
			//check with old password should return false
			assertPasswordChange(validIdentityName, validIdentityPassword, newPassword);
			
		} finally {
			// reset to original password
			connector.setPassword(validIdentityName, validIdentityId,
					validIdentityPassword.toCharArray(), false);
		}
	}
	
	@Test(expected = PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionWithInvalidGuidIdentityOnChangePassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));
		
		//given an identity with invalid guid
		final String invalidGuid = validIdentityId + validIdentityId;
		//when change password is attempted
		connector.changePassword(validIdentityName, invalidGuid,
				validIdentityPassword.toCharArray(), newPassword.toCharArray());
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test(expected = PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionWithInvalidPrincipalNameIdentityOnChangePassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));
		
		//given an identity with invalid principal name
		//when change password is attempted
		connector.changePassword(TestUtils.randomValue(), validIdentityId,
				validIdentityPassword.toCharArray(), newPassword.toCharArray());
		//then PrincipalNotFoundException should be thrown
	}
	
	@Test(expected = InvalidLoginCredentialsException.class)
	public void itShouldThrowInvalidLoginCredentialsExceptionWithInvalidPasswordIdentityOnChangePassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));
		
		//given an identity with invalid password
		//when change password is attempted
		connector.changePassword(validIdentityName, validIdentityId, TestUtils
				.randomValue().toCharArray(), newPassword.toCharArray());
		//then InvalidLoginCredentialsException should be thrown 
	}
	
	@Test
	public void itShouldSetPasswordForValidIdentity() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordSet));
		
		try {
			//given a valid identity with new password
			//and no force change password
			final boolean forcePasswordChangeAtLogon = false;
			//when set password is attempted
			connector.setPassword(validIdentityName, validIdentityId,
					newPassword.toCharArray(),forcePasswordChangeAtLogon);
			//then check with new password should return true flag
			//check with old password should return false
			assertPasswordChange(validIdentityName, validIdentityPassword, newPassword);
			
		} finally {
			// reset to original password
			connector.setPassword(validIdentityName, validIdentityId,
					validIdentityPassword.toCharArray(), false);
		}
	}
	
	@Test(expected = PrincipalNotFoundException.class)
	public void itShouldThrowPrincipalNotFoundExceptionForInvalidIdentityOnSetPassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordSet));
		
		//given an invalid identity
		final boolean forcePasswordChangeAtLogon = false;
		//when set password is attempted
		connector.setPassword(TestUtils.randomValue(), validIdentityId,
				newPassword.toCharArray(), forcePasswordChangeAtLogon);
		//then PrincipalNotFoundException is thrown
	}
	
	@Test public void itShouldCreateIdentityIfNotPresentInDataSourceAlongWithRolesProvided() {throw new UnsupportedOperationException("To be implemented.");}
	@Test public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {throw new UnsupportedOperationException("To be implemented.");}
	@Test public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {throw new UnsupportedOperationException("To be implemented.");}
	
	
	protected void assertPasswordChange(String identityName, String oldPassword,String newPassword) {
		boolean checkOldCredentials = connector.checkCredentials(
				identityName, oldPassword.toCharArray());
		assertFalse("Credentials are valid. These should be invalid",
				checkOldCredentials);
		boolean checkNewCredentials = connector.checkCredentials(identityName,
				newPassword.toCharArray());
		assertTrue("Credentials are invalid. These should be valid",
				checkNewCredentials);
	}
	
	protected void assertPrincipalMatches(String expectedPrincipalName,
			Principal principal) {
		assertNotNull(principal);
		assertEquals(expectedPrincipalName, principal.getPrincipalName());
	}
	
	protected Identity getIdentityFromSource(Identity identity) {
		return connector
				.getIdentityByName(identity.getPrincipalName());
	}
	
	protected void deleteIdentityFromSource(Identity identity) {
		connector.deleteIdentity(identity.getPrincipalName());
	}
}
