package com.identity4j.connector.aws;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.connector.AbstractRestWebServiceConnectorTest;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;

public class AwsConnectorTest extends AbstractRestWebServiceConnectorTest {

	static {
		init("/aws-connector.properties");
	}
	
	String validRoleName;
	
	@Before
    public void setup() {
        validIdentityName = configurationParameters.getStringOrFail("connector.validIdentityName");
        validIdentityId = configurationParameters.getStringOrFail("connector.validIdentityId");
        validIdentityPassword = configurationParameters.getStringOrFail("connector.validIdentityPassword");
        testIdentityName = configurationParameters.getStringOrFail("connector.testIdentityName");
        testIdentityPassword = configurationParameters.getStringOrFail("connector.testIdentityPassword");
        newPassword = configurationParameters.getStringOrFail("connector.newPassword");
        invalidIdentityName = configurationParameters.getStringOrFail("connector.invalidIdentityName");
        invalidPassword = configurationParameters.getStringOrFail("connector.invalidPassword");
        validRoleName = configurationParameters.getStringOrFail("connector.validRoleName");
    }
	
	@Override
	protected Identity getIdentity(String identityName) {
		return new AwsIdentity(identityName);
	}
	
	@Test
    public void itShouldDeleteIdentityIfPresentInDataStore() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.deleteUser));

        // given a valid identity present in data store
        Identity identity = getIdentity(testIdentityName);
        identity.setFullName("Mock User");

        identity = connector.createIdentity(identity, testIdentityPassword.toCharArray());
        
        try {
			// needed as post create delete op too fast for AWS to handle
			Thread.sleep(30 * 1000);
		} catch (Exception e) {
			// ignore
		}
        
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
	
	@Override
	public void itShouldCreateARoleIfNotPresentInDataSource() {
		
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createRole));
		
		RoleImpl roleImpl = new RoleImpl(UUID.randomUUID().toString(), validRoleName);
		
		try {
			connector.createRole(roleImpl);
		} finally {
			deleteRoleFromSource(roleImpl);
		}
	}
	
	@Override
	public void itShouldThrowPrincipalAlreadyExistsExceptionIfRoleAlreadyPresentInDataSource() {
	}

	@Override
	public void itShouldCreateIdentityIfNotPresentInDataSourceAlongWithRolesProvided() {
	}
	
	@Override
	public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {
	}
	
	@Override
	public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {
	}
	
	@Override
	protected void deleteIdentityFromSource(Identity identity) {
		try {
			// needed as post create cleanup delete op too fast for AWS to handle
			Thread.sleep(30 * 1000);
		} catch (Exception e) {
			// ignore
		}
		super.deleteIdentityFromSource(identity);
	}
	
	protected void deleteRoleFromSource(Role role) {
		try {
			// needed as post create cleanup delete op too fast for AWS to handle
			Thread.sleep(30 * 1000);
		} catch (Exception e) {
			// ignore
		}
		connector.deleteRole(role.getPrincipalName());
	}
}
