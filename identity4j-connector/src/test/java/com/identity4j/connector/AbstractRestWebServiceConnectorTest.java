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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    protected static Connector<?> connector;

    protected static void init(String configFile) {
        PropertyConfigurator.configure(AbstractRestWebServiceConnectorTest.class.getResource("/test-log4j.properties"));

        configurationParameters = loadConfigurationParameters(configFile);
        ConnectorBuilder connectorBuilder = new ConnectorBuilder();
        ConnectorConfigurationParameters parms = connectorBuilder.buildConfiguration(configurationParameters);
        connector = connectorBuilder.buildConnector(parms);
    }

    protected static MultiMap loadConfigurationParameters(String propertiesFile) {
        try {
            InputStream resourceAsStream = AbstractRestWebServiceConnectorTest.class.getResourceAsStream(propertiesFile);
            if (resourceAsStream == null) {

                File externalResource = new File("conf" + propertiesFile);
                if (externalResource.exists()) {
                    resourceAsStream = new FileInputStream(externalResource);
                } else {
                    throw new FileNotFoundException("Properties resource " + propertiesFile
                                    + " not found. Check it is on your classpath");
                }
            }
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            return MultiMap.toMultiMap(properties);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to load configuration parameters", ioe);
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
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createUser));

        // given an identity not present in data store
        Identity identity = getIdentity(getTestIdentityName());
        identity.setFullName("Mock User");
        try {
            // when it is created in data store
            identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
            // then fetched instance from data store
            Identity identityFromSource = getIdentityFromSource(identity);
            // should have same assigned principal name
            Assert.assertEquals("Principal names should be same", identity.getPrincipalName(), identityFromSource
                            .getPrincipalName());
        } finally {
            deleteIdentityFromSource(getTestIdentityName());
        }
    }

    @Test(expected = PrincipalAlreadyExistsException.class)
    public void itShouldThrowPrincipalAlreadyExistsExceptionIfIdentityIsPresentInDataSourceOnCreate() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createUser));

        // given an identity already present in data store
        Identity identity = getIdentity(getValidIdentityName());
        identity.setFullName("Mock User");
        // when it is created in data store
        connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
        // then PrincipalAlreadyExistsException should be thrown
        fail(String.format("Identity with name %s should not have been created, it was supposed to already exist.", identity.getPrincipalName()));
    }

    @Test
    public void itShouldUpdateIdentityWithTheChangesPassed() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.updateUser));

        // given an identity
        Identity identity = getIdentity(getTestIdentityName());
        identity.setFullName("Test Junit");
        identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
        try {
            // when the changes are updated
            identity.setFullName("Test JunitChanged");
            connector.updateIdentity(identity);

            // then the identity from data source should reflect the changes
            Identity identityFromSource = getIdentityFromSource(identity);
            Assert.assertEquals("Full name should be same as updated name", identity.getFullName(), identityFromSource
                            .getFullName());
        } finally {
            deleteIdentityFromSource(getTestIdentityName());
        }
    }

    @Test
    public void itShouldThrowPrincipalNotFoundExceptionIfToUpdateIdentityIsNotPresentInDataStore() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.updateUser));

        try {
	        // given an identity not present in data store
	        // when identity is updated
	        Identity identity = getIdentity(getInvalidIdentityName());
	        identity.setFullName("Mock User");
	        connector.updateIdentity(identity);
	        // then PrincipalNotFoundException should be thrown
	        fail();
        } catch(PrincipalNotFoundException e) {
        	// exception was thrown
		} catch (Exception e) {
			fail(e.getMessage());
		}
    }

    @Test
    public void itShouldDeleteIdentityIfPresentInDataStore() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.deleteUser));

        // given a valid identity present in data store
        Identity identity = getIdentity(getTestIdentityName());
        identity.setFullName("Mock User");

       	identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
        // when identity is deleted
        connector.deleteIdentity(identity.getPrincipalName());

        // then it should be deleted from data source
        // attempt to find it in data source should throw
        // PrincipalNotFoundException
        try {
            Identity identityFromSource = getIdentityFromSource(identity);
            Assert.fail("Identity supposed to be deleted found " + identityFromSource.getPrincipalName());
        } catch (PrincipalNotFoundException e) {
            // ignore
        }
    }

    @Test
    public void itShouldThrowPrincipalNotFoundExceptionOnDeleteIfIdentityNotPresentInDataStore() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.deleteUser));

        try {
	        // given an identity not present in data store
	        // when identity is deleted
	        connector.deleteIdentity(getInvalidIdentityName());
	        // then PrincipalNotFoundException should be thrown
	        fail();
        } catch (PrincipalNotFoundException e) {
			// exception was thrown
		} catch (Exception e) {
			fail(e.getMessage());
		}
    }

    @Test
    public void itShouldFetchValidUserIfPresentInDataStore() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));

        // given an identity present in data source
        // when it is fetched by principal name
        Identity identityFromSource = connector.getIdentityByName(getValidIdentityName());
        // then it should return identity instance
        assertPrincipalMatches(getValidIdentityName(), identityFromSource);
    }

    @Test(expected = PrincipalNotFoundException.class)
    public void itShouldThrowPrincipalNotFoundExceptionForInValidUserNotPresentInDataStore() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));

        // given an identity not present in data source
        // when it is fetched by principal name
        connector.getIdentityByName(getInvalidIdentityName());
        // then PrincipalNotFoundException should be thrown
    }

    @Test
    public void itShouldReturnTrueFlagIfIdentityNameIsInUse() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));
        // given a valid identity name present in data store
        // when a check is made for presence in data store
        boolean identityNameInUse = connector.isIdentityNameInUse(getValidIdentityName());
        // then it should return true flag
        assertTrue("Identity name should be in use", identityNameInUse);
    }

    @Test
    public void itShouldReturnFalseFlagIfIdentityNameIsNotInUse() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.identities));

        // given an identity name not present in data store
        // when a check is made for presence in data store
        boolean identityNameInUse = connector.isIdentityNameInUse(TestUtils.randomValue());
        // then it should return false flag
        assertFalse("Identity name should not be in use", identityNameInUse);
    }

    @Test
    public void itShouldReturnIdentityOnSuccessfulLogon() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.authentication));

        // given an identity present in data store, with correct password
        // when login is attempted
        Identity identity = connector.logon(getValidIdentityName(), getValidIdentityPassword().toCharArray());
        // then it should login user to the system and return a valid instance
        // and should have same principal name
        Assert.assertEquals("Principal names should be same", getValidIdentityName(), identity.getPrincipalName());
    }

    @Test(expected = InvalidLoginCredentialsException.class)
    public void itShouldThrowInvalidLoginCredentialsExceptionForBadCredentialsOnLogon() {
        if (!connector.getCapabilities().contains(ConnectorCapability.authentication)) {
            // just pass - "Assume" doesn't work as expected here
            throw new InvalidLoginCredentialsException();
        }

        // given an identity present in data store, but invalid password
        // when login is attempted
        connector.logon(getValidIdentityName(), getInvalidPassword().toCharArray());
        // then InvalidLoginCredentialsException should be thrown
    }

    @Test(expected = PrincipalNotFoundException.class)
    public void itShouldThrowPrincipalNotFoundExceptionForInValidUserNotPresentInDataStoreOnLogon() {
        if (!connector.getCapabilities().contains(ConnectorCapability.authentication)) {
            // just pass - "Assume" doesn't work as expected here
            throw new PrincipalNotFoundException("No auh.");
        }

        // given invalid identity not present in data store
        // when login is attempted
        connector.logon(getInvalidIdentityName(), getInvalidPassword().toCharArray());
        // then PrincipalNotFoundException should be thrown
    }

    @Test
    public void itShouldDisableAnIdentity() {
    	Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.accountDisable));
    	
        // given a valid identity
        Identity identity = getIdentity(getTestIdentityName());
        identity.setFullName("Mock User");
        try {
            // and stored in data store
            identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());

            // when it is disabled
            connector.disableIdentity(identity);

            Identity identityFromSource = getIdentityFromSource(identity);

            // then the account status disabled field should return true
            Assert.assertTrue("Identity is disabled", identityFromSource.getAccountStatus().isDisabled());

        } finally {
            deleteIdentityFromSource(getTestIdentityName());
        }
    }

    @Test
    public void itShouldEnableAnIdentity() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.accountDisable));

        // given a valid identity
        Identity identity = getIdentity(getTestIdentityName());
        identity.setFullName("Mock User");
        Identity identityFromSource = null;
        try {
            // and stored in data store
            Identity identityCreated = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
            // which is disabled
            connector.disableIdentity(identityCreated);

            Assert.assertTrue("Identity is disabled", identityCreated.getAccountStatus().isDisabled());
            // when the identity is enabled
            connector.enableIdentity(identityCreated);

            identityFromSource = getIdentityFromSource(identityCreated);

            // then the account status disabled field should return false
            Assert.assertFalse("Identity is enabled", identityFromSource.getAccountStatus().isDisabled());

        } finally {
            deleteIdentityFromSource(getTestIdentityName());
        }
    }

    @Test
    public void itShouldCreateARoleIfNotPresentInDataSource() {
        throw new UnsupportedOperationException("To be implemented.");
    }

    @Test
    public void itShouldThrowPrincipalAlreadyExistsExceptionIfRoleAlreadyPresentInDataSource() {
        throw new UnsupportedOperationException("To be implemented.");
    }

    @Test
    public void itShouldReturnTrueFlagForValidCredentials() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.authentication));

        // given a valid user
        boolean checkCredentials = connector.checkCredentials(getValidIdentityName(),
            // and valid password
            getValidIdentityPassword().toCharArray());
        // when credential check is made
        // then it should return true flag
        assertTrue("Credentials are valid", checkCredentials);
    }

    @Test
    public void itShouldReturnFalseFlagForInValidPrincipal() {
    	Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.authentication));
        // given an identity with invalid principal name
        // when credential check is made
        boolean checkCredentials = connector.checkCredentials(TestUtils.randomValue(), getValidIdentityPassword().toCharArray());
        // then it should return false flag
        assertFalse("Credentials are in valid. These should be valid", checkCredentials);
    }

    @Test
    public void itShouldReturnFalseFlagForInValidPassword() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.authentication));
        // given an identity with valid principal name
        boolean checkCredentials = connector.checkCredentials(getValidIdentityName(),
            // and in correct password
            TestUtils.randomValue().toCharArray());
        // when credential check is made
        // then it should return false flag
        assertFalse("Credentials are in valid. These should be valid", checkCredentials);
    }

    @Test
    public void itShouldChangeThePasswordForValidIdentity() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange) && connector.getCapabilities()
                        .contains(ConnectorCapability.authentication));

        try {
            // given a valid identity with new password
            // when password change is attempted
            connector.changePassword(getValidIdentityName(), getValidIdentityId(), getValidIdentityPassword().toCharArray(), getNewPassword()
                            .toCharArray());
            // then check with new password should return true flag
            // check with old password should return false
            assertPasswordChange(getValidIdentityName(), getValidIdentityPassword(), getNewPassword());

        } finally {
            // reset to original password
            connector.setPassword(getValidIdentityName(), getValidIdentityId(), getValidIdentityPassword().toCharArray(), false);
        }
    }

    @Test
    public void itShouldThrowPrincipalNotFoundExceptionWithInvalidGuidIdentityOnChangePassword() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));

        try {
	        // given an identity with invalid guid
	        final String invalidGuid = getValidIdentityId() + getValidIdentityId();
	        // when change password is attempted
	        connector.changePassword(getValidIdentityName(), invalidGuid, getValidIdentityPassword().toCharArray(), getNewPassword().toCharArray());
	        // then PrincipalNotFoundException should be thrown
	        fail();
        } catch (PrincipalNotFoundException e) {
        	// exception was thrown
		} 
    }

    public void itShouldThrowPrincipalNotFoundExceptionWithInvalidPrincipalNameIdentityOnChangePassword() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));

        // given an identity with invalid principal name
        // when change password is attempted
        try {
            connector.changePassword(TestUtils.randomValue(), getValidIdentityId(), getValidIdentityPassword().toCharArray(), getNewPassword()
                            .toCharArray());
        } catch (PrincipalNotFoundException pnfe) {
            return;
        }
        Assert.assertTrue("Must not reach here.", false);
    }

    public void itShouldThrowInvalidLoginCredentialsExceptionWithInvalidPasswordIdentityOnChangePassword() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange) && connector.getCapabilities()
                        .contains(ConnectorCapability.authentication));

        // given an identity with invalid password
        // when change password is attempted
        try {
            connector.changePassword(getValidIdentityName(), getValidIdentityId(), TestUtils.randomValue().toCharArray(), getNewPassword()
                            .toCharArray());
        } catch (InvalidLoginCredentialsException ilce) {
            return;
        }
        Assert.assertTrue("Must not reach here.", false);
    }

    @Test
    public void itShouldSetPasswordForValidIdentity() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordSet));

        try {
            // given a valid identity with new password
            // and no force change password
            final boolean forcePasswordChangeAtLogon = false;
            // when set password is attempted
            connector.setPassword(getValidIdentityName(), getValidIdentityId(), getNewPassword().toCharArray(), forcePasswordChangeAtLogon);
            // then check with new password should return true flag
            // check with old password should return false
            assertPasswordChange(getValidIdentityName(), getValidIdentityPassword(), getNewPassword());

        } finally {
            // reset to original password
            connector.setPassword(getValidIdentityName(), getValidIdentityId(), getValidIdentityPassword().toCharArray(), false);
        }
    }

    @Test
    public void itShouldThrowPrincipalNotFoundExceptionForInvalidIdentityOnSetPassword() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordSet));

        try {
	        // given an invalid identity
	        final boolean forcePasswordChangeAtLogon = false;
	        // when set password is attempted
	        connector.setPassword(TestUtils.randomValue(), getValidIdentityId(), getNewPassword().toCharArray(), forcePasswordChangeAtLogon);
	        // then PrincipalNotFoundException is thrown
	        fail();
        } catch (PrincipalNotFoundException e) {
        	// exception was thrown
		} catch (Exception e) {
			fail(e.getMessage());
		}
    }

    @Test
    public void itShouldCreateIdentityIfNotPresentInDataSourceAlongWithRolesProvided() {
        throw new UnsupportedOperationException("To be implemented.");
    }

    @Test
    public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {
        throw new UnsupportedOperationException("To be implemented.");
    }

    @Test
    public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {
        throw new UnsupportedOperationException("To be implemented.");
    }

    protected void assertPasswordChange(String identityName, String oldPassword, String newPassword) {

        if (!connector.getCapabilities().contains(ConnectorCapability.authentication)) {
            return;
        }

        boolean checkOldCredentials = connector.checkCredentials(identityName, oldPassword.toCharArray());
        assertFalse("Credentials are valid. These should be invalid", checkOldCredentials);
        boolean checkNewCredentials = connector.checkCredentials(identityName, newPassword.toCharArray());
        assertTrue("Credentials are invalid. These should be valid", checkNewCredentials);
    }

    protected void assertPrincipalMatches(String expectedPrincipalName, Principal principal) {
        assertNotNull(principal);
        assertEquals(expectedPrincipalName, principal.getPrincipalName());
    }

    protected Identity getIdentityFromSource(Identity identity) {
        return connector.getIdentityByName(identity.getPrincipalName());
    }

    protected void deleteIdentityFromSource(String identity) {
        connector.deleteIdentity(identity);
    }

	protected String getValidIdentityName() {
		return validIdentityName;
	}

	protected void setValidIdentityName(String validIdentityName) {
		this.validIdentityName = validIdentityName;
	}

	protected String getTestIdentityName() {
		return testIdentityName;
	}

	protected void setTestIdentityName(String testIdentityName) {
		this.testIdentityName = testIdentityName;
	}

	protected String getTestIdentityPassword() {
		return testIdentityPassword;
	}

	protected void setTestIdentityPassword(String testIdentityPassword) {
		this.testIdentityPassword = testIdentityPassword;
	}

	protected String getNewPassword() {
		return newPassword;
	}

	protected void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	protected String getInvalidIdentityName() {
		return invalidIdentityName;
	}

	protected void setInvalidIdentityName(String invalidIdentityName) {
		this.invalidIdentityName = invalidIdentityName;
	}

	protected String getInvalidPassword() {
		return invalidPassword;
	}

	protected void setInvalidPassword(String invalidPassword) {
		this.invalidPassword = invalidPassword;
	}

	protected String getValidIdentityId() {
		return validIdentityId;
	}

	protected void setValidIdentityId(String validIdentityId) {
		this.validIdentityId = validIdentityId;
	}

	protected String getValidIdentityPassword() {
		return validIdentityPassword;
	}

	protected void setValidIdentityPassword(String validIdentityPassword) {
		this.validIdentityPassword = validIdentityPassword;
	}
}
