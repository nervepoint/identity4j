package com.identity4j.connector.google;

/*
 * #%L
 * Identity4J GOOGLE
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

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.connector.AbstractRestWebServiceConnectorTest;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;

/**
 * Test class for testing Google Connector
 * 
 * There is Pre Requisite requirement of having a valid user entry in google
 * user data store
 * That user should be mapped by configuration key for id :
 * connector.validIdentityName and
 * for password : connector.validIdentityPassword
 * 
 * For Role there should be entry for name : connector.validRoleName and for
 * email :
 * connector.validRoleEmail
 * 
 * @author gaurav
 *
 */
public class GoogleConnectorTest extends AbstractRestWebServiceConnectorTest {

    private String validRoleName = null;
    private String validRoleEmail = null;
    private String testRoleName = null;
    private String testRoleEmail = null;
    private String testRoleDescription = null;
    private String testRoleEmail1 = null;
    private String testRoleEmail2 = null;

    static {
        init("/google-connector.properties");
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
        testRoleName = configurationParameters.getStringOrFail("connector.testRoleName");
        testRoleEmail = configurationParameters.getStringOrFail("connector.testRoleEmail");
        testRoleDescription = configurationParameters.getStringOrFail("connector.testRoleDescription");
        testRoleEmail1 = configurationParameters.getStringOrFail("connector.testRoleEmail1");
        testRoleEmail2 = configurationParameters.getStringOrFail("connector.testRoleEmail2");
    }

    @Test
    public void itShouldListAllIdentities() {
        Iterator<Identity> ids = connector.allIdentities();
        while (ids.hasNext()) {
            ids.next();
        }
    }

    @Test
    public void itShouldListAllRoles() {
        Iterator<Role> ids = connector.allRoles();
        while (ids.hasNext()) {
            ids.next();
        }
    }

    @Test
    public void itShouldCreateARoleIfNotPresentInDataSource() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));

        try {
            // Try and delete just in case of a failed test
            connector.deleteRole(testRoleEmail);
        } catch (ConnectorException e) {
        }

        // given a valid role not present in data store
        Role role = new RoleImpl(null, testRoleName);
        role.setAttribute("email", testRoleEmail);
        role.setAttribute("description", testRoleDescription);
        // when role is created
        Role fromDataSource = connector.createRole(role);
        try {

            // then role is saved in data store
            Assert.assertEquals("Role principal names should be same", role.getPrincipalName(), fromDataSource.getPrincipalName());

        } finally {
            connector.deleteRole(role.getAttribute("email"));
        }
    }

    @Test(expected = PrincipalAlreadyExistsException.class)
    public void itShouldThrowPrincipalAlreadyExistsExceptionIfRoleAlreadyPresentInDataSource() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.roles));

        // given a valid role already present in data store
        Role role = new RoleImpl(null, validRoleName);
        role.setAttribute("email", validRoleEmail);
        // when role is created
        connector.createRole(role);
        // then PrincipalAlreadyExistsException is thrown
    }

    public void itShouldThrowConnectorExceptionForBadPasswordPolicyOfLengthLessThan8OnChangePassword() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));

        // given an identity with bad password policy of length less than 8
        // when change password is attempted
        try {
            connector.changePassword(getValidIdentityName(), getValidIdentityId(), getValidIdentityPassword().toCharArray(), "bad".toCharArray());
        } catch (ConnectorException ce) {
            return;
        }

        Assert.assertTrue("Must not reach here.", false);
    }

    @Test
    public void itShouldSetPasswordForValidIdentityAndEnforcePasswordChangeOnCheckCredential() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordSet) && connector.getCapabilities()
                        .contains(ConnectorCapability.authentication));

        // given a valid identity with new password
        // and force change password
        final boolean forcePasswordChangeAtLogon = true;
        // when set password is attempted
        connector.setPassword(getValidIdentityName(), getValidIdentityId(), getNewPassword().toCharArray(), forcePasswordChangeAtLogon);
        try {
            // then PasswordChangeRequiredException should be thrown
            try {
                assertPasswordChange(getValidIdentityName(), getValidIdentityPassword(), getNewPassword());
                throw new IllegalStateException("Expected a PasswordChangeRequiredException");
            } catch (PasswordChangeRequiredException pcre) {
                // Expect this
            }
            // and identity password status should be changeRequired
            Identity identityByName = connector.getIdentityByName(getValidIdentityName());
            assertEquals("Identity should have change password at next logon set", PasswordStatusType.changeRequired, identityByName
                            .getPasswordStatus().getType());
        } finally {
            // reset to original password
            connector.setPassword(getValidIdentityName(), getValidIdentityId(), getValidIdentityPassword().toCharArray(), false);
        }
    }

    @Test
    public void itShouldCreateIdentityIfNotPresentInDataSourceAlongWithRolesProvided() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createUser));

        // given an identity not present in data store
        Identity googleIdentity = new GoogleIdentity(getTestIdentityName());
        googleIdentity.setFullName("Mock User");
        // and a valid role
        googleIdentity.addRole(connector.getRoleByName(validRoleEmail));
        try {
            // when it is created in data store
            connector.createIdentity(googleIdentity, getTestIdentityPassword().toCharArray());
            // then fetched instance from data store
            Identity googleIdentityFromSource = connector.getIdentityByName(googleIdentity.getPrincipalName());
            // should have same assigned principal name
            Assert.assertEquals("Principal names should be same", googleIdentity.getPrincipalName(), googleIdentityFromSource
                            .getPrincipalName());
            // should have same role as provided
            Assert.assertEquals("Role name should be same", validRoleName, googleIdentity.getRoles()[0].getPrincipalName());
        } finally {
            connector.deleteIdentity(googleIdentity.getPrincipalName());
        }
    }

    @Test
    public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createUser));

        // given an identity not present in data store
        Identity googleIdentity = new GoogleIdentity(getTestIdentityName());
        googleIdentity.setFullName("Mock User");

        Role role = new RoleImpl(null, "dummy");
        role.setAttribute("email", "dummy@kitchu.com");
        role.setAttribute("description", "dummy role not in data store");

        // and an in valid role
        googleIdentity.addRole(role);
        try {
            // when it is created in data store
            connector.createIdentity(googleIdentity, getTestIdentityPassword().toCharArray());
            // then it should throw PrincipalNotFoundException
            Assert.fail();
        } catch (PrincipalNotFoundException e) {
            // and PrincipalType should be role
            Assert.assertEquals("", PrincipalType.role, e.getPrincipalType());
        } finally {
            connector.deleteIdentity(googleIdentity.getPrincipalName());
        }
    }

    @Test
    public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.updateUser));

        // given an identity
        Identity googleIdentity = new GoogleIdentity(getTestIdentityName());
        googleIdentity.setFullName("Test Junit");
        // and a valid role
        googleIdentity.addRole(connector.getRoleByName(validRoleEmail));
        try {
            connector.createIdentity(googleIdentity, getTestIdentityPassword().toCharArray());
            // when the changes are updated
            googleIdentity.setFullName("Test JunitChanged");
            googleIdentity.setRoles(new Role[] { connector.getRoleByName(testRoleEmail1), connector.getRoleByName(
                testRoleEmail2) });
            connector.updateIdentity(googleIdentity);

            // then the identity from data source should reflect the changes
            Identity googleIdentityFromSource = connector.getIdentityByName(googleIdentity.getPrincipalName());
            Assert.assertEquals("Full name should be same as updated name", googleIdentity.getFullName(), googleIdentityFromSource
                            .getFullName());
            Assert.assertEquals("Total Roles assigned should be 2", 2, googleIdentityFromSource.getRoles().length);
            assertRoleIsPresent(googleIdentityFromSource.getRoles(), testRoleEmail1);
            assertRoleIsPresent(googleIdentityFromSource.getRoles(), testRoleEmail2);
        } finally {
            connector.deleteIdentity(googleIdentity.getPrincipalName());
        }
    }

    private void assertRoleIsPresent(Role[] roles, String principalName) {
        for (Role role : roles) {
            if (role.getAttribute("email").equals(principalName))
                return;
        }
        Assert.fail("Role not found " + principalName);
    }

    @Override
    protected Identity getIdentity(String identityName) {
        return new GoogleIdentity(identityName);
    }

}
