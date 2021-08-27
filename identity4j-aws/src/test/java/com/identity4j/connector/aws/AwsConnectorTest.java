package com.identity4j.connector.aws;

import static org.junit.Assert.fail;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.identity4j.connector.AbstractRestWebServiceConnectorTest;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.TestUtils;

public class AwsConnectorTest extends AbstractRestWebServiceConnectorTest {
	
	private static final Log log = LogFactory.getLog(AwsConnectorTest.class);

	static {
		init("/aws-connector.properties");
	}
	
	protected String validRoleName;
	protected String testRoleName;
	
	@Before
    public void setup() {
        setValidIdentityName(configurationParameters.getStringOrFail("connector.validIdentityName"));
        setValidIdentityId(configurationParameters.getStringOrFail("connector.validIdentityId"));
        setValidIdentityPassword(configurationParameters.getStringOrFail("connector.validIdentityPassword"));
        setNewPassword(configurationParameters.getStringOrFail("connector.newPassword"));
        setInvalidIdentityName(configurationParameters.getStringOrFail("connector.invalidIdentityName"));
        setInvalidPassword(configurationParameters.getStringOrFail("connector.invalidPassword"));
        setValidRoleName(configurationParameters.getStringOrFail("connector.validRoleName"));
    }

	
	@Override
	protected Identity getIdentity(String identityName) {
		return new AwsIdentity(identityName);
	}
	
	@Test
    public void itShouldDeleteIdentityIfPresentInDataStore() {
        Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.deleteUser));

        // given a valid identity to be created in data store
        Identity identity = getIdentity(getTestIdentityName());
        identity.setFullName("Mock User");

        identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
        
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
		
		// given a role not present in data store
		RoleImpl roleImpl = new RoleImpl(UUID.randomUUID().toString(), getTestRoleName());
		
		try {
			// when it is created in data store
			Role roleCreated = connector.createRole(roleImpl);
			// then fetched instance from data store
			Role roleFromSource = connector.getRoleByName(roleCreated.getPrincipalName());
			// should have same assigned principal name
			Assert.assertEquals("Principal names should be same", roleCreated.getPrincipalName(), roleFromSource
                    .getPrincipalName());
			
	        
		} finally {
			deleteRoleFromSource(roleImpl);
		}
	}
	
	@Override
	public void itShouldThrowPrincipalAlreadyExistsExceptionIfRoleAlreadyPresentInDataSource() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.createRole));
		
		try {
			// given a role present in data store
			RoleImpl roleImpl = new RoleImpl(UUID.randomUUID().toString(), getValidRoleName());
			// when it is created in data store
			connector.createRole(roleImpl);
			// it should throw exception
			fail("Should not have created role");
		} catch (PrincipalAlreadyExistsException e) {
			// ignore marks test as pass
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail("Was expecting PrincipalAlreadyExistsException, " + e.getMessage());
		}
		
	}

	@Override
	public void itShouldCreateIdentityIfNotPresentInDataSourceAlongWithRolesProvided() {
		// given a valid identity to be created in data store
        Identity identity = getIdentity(getTestIdentityName());
        RoleImpl roleImpl = new RoleImpl(UUID.randomUUID().toString(), getValidRoleName());
		try {
			
	        identity.setFullName("Mock User");
	        identity.addRole(roleImpl);
	
	        // when it is created in data store
	        identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
	        
	        Identity identityFromSource = getIdentityFromSource(identity);
	        
	        // should have same assigned principal name
            Assert.assertEquals("Principal names should be same", identity.getPrincipalName(), identityFromSource
                            .getPrincipalName());
            Role[] roles = identityFromSource.getRoles();
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.length > 0);
            
            Role roleFromSource = roles[0];
            // should have same assigned role name
            Assert.assertEquals("Role names should be same", roleImpl.getPrincipalName(), roleFromSource
                            .getPrincipalName());
		} finally {
			try {
				deleteIdentityFromSource(identity);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void itShouldThrowPrincipalNotFoundExceptionOnCreateIdentityAlongWithRolesProvidedIfRoleNotPresentInDataSource() {
		
		// given a valid identity to be created in data store
		Identity identity = getIdentity(getTestIdentityName());
		// and a role that does not exists in the data store
        RoleImpl roleImpl = new RoleImpl(UUID.randomUUID().toString(), TestUtils.randomValue());
        
        try {
			
	        identity.setFullName("Mock User");
	        identity.addRole(roleImpl);
	
	        // when it is created in data store
	        identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
	        // it should throw exception
	        fail("Should not have passed, role in identity is not a valid role");
		} catch (PrincipalNotFoundException e) {
            // ignore
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        	fail("Was expecting PrincipalNotFoundException, " + e.getMessage());
		}
	}
	
	@Override
	public void itShouldUpdateIdentityWithTheChangesPassedSpecificToRole() {
		// given a valid identity
		Identity identity = getIdentity(getTestIdentityName());
		// with role
		RoleImpl roleImpl = new RoleImpl(UUID.randomUUID().toString(), getValidRoleName());
		
		RoleImpl roleImplNew = new RoleImpl("Update_Role_Test_Helper_New" + UUID.randomUUID().toString(), TestUtils.randomValue());
		try {
			
	        identity.setFullName("Mock User");
	        identity.addRole(roleImpl);
	        
	        identity = connector.createIdentity(identity, getTestIdentityPassword().toCharArray());
	        connector.createRole(roleImplNew);
	        
	        // when new role is added
	        identity.setRoles(new Role[] {roleImplNew});
	        // and identity updated
	        connector.updateIdentity(identity);
	        
	        // it should add new role to the identity
	        
	        Identity identityFromSource = connector.getIdentityByName(identity.getPrincipalName());
	        
            Role[] roles = identityFromSource.getRoles();
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.length > 0);
            
            Role roleFromSource = roles[0];
            // should have same assigned role name
            Assert.assertEquals("Role names should be same", roleImplNew.getPrincipalName(), roleFromSource
                            .getPrincipalName());
		} finally {
			try {
				deleteRoleFromSource(roleImplNew);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			try {
				deleteIdentityFromSource(identity);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void itShouldUpdateIdentityWithTheChangesPassed() {
		// update of properties not supported
		// ignore
	}
	
	@Override
	public void itShouldThrowPrincipalNotFoundExceptionIfToUpdateIdentityIsNotPresentInDataStore() {
		
		// given an invalid identity to be updated in data store
		Identity identity = getIdentity(invalidIdentityName);
		
		try {
			
	        // when it is updated in data store
	        connector.updateIdentity(identity);
	        // it should throw exception
	        fail("Should not have passed, identity is not a valid");
		} catch (PrincipalNotFoundException e) {
            // ignore
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        	fail("Was expecting PrincipalNotFoundException, " + e.getMessage());
		}
	}
	
	@Override
	protected void deleteIdentityFromSource(String identity) {
		try {
			// needed as post create cleanup delete op too fast for AWS to handle, it will throw exception
			Thread.sleep(30 * 1000);
		} catch (Exception e) {
			// ignore
		}
		super.deleteIdentityFromSource(identity);
		
	}
	
	protected void deleteRoleFromSource(Role role) {
		try {
			// needed as post create cleanup delete op too fast for AWS to handle, it will throw exception
			Thread.sleep(30 * 1000);
		} catch (Exception e) {
			// ignore
		}
		connector.deleteRole(role.getPrincipalName());
		
	}

	String getValidRoleName() {
		return validRoleName;
	}

	void setValidRoleName(String validRoleName) {
		this.validRoleName = validRoleName;
	}

	/**
	 * AWS is slow in syncing CRUD ops, as test create/delete objects in quick succession AWS cannot handle this.
	 * We need different value for each case, else state collision occurs. When one test case has deleted an object
	 * on cleanup, but when another test case starts on with it, it is still getting deleted on AWS leading to local errors.
	 * Hence better just keep them random for each case.
	 * @return
	 */
	String getTestRoleName() {
		return TestUtils.randomValue();
	}

	void setTestRoleName(String testRoleName) {
		this.testRoleName = testRoleName;
	}
	
	/**
	 * AWS is slow in syncing CRUD ops, as test create/delete objects in quick succession AWS cannot handle this.
	 * We need different value for each case, else state collision occurs. When one test case has deleted an object
	 * on cleanup, but when another test case starts on with it, it is still getting deleted on AWS leading to local errors.
	 * Hence better just keep them random for each case.
	 * @return
	 */
	@Override
	protected String getTestIdentityName() {
		return TestUtils.randomValue();
	}
	
	/**
	 * AWS is slow in syncing CRUD ops, as test create/delete objects in quick succession AWS cannot handle this.
	 * We need different value for each case, else state collision occurs. When one test case has deleted an object
	 * on cleanup, but when another test case starts on with it, it is still getting deleted on AWS leading to local errors.
	 * Hence better just keep them random for each case.
	 * @return
	 */
	@Override
	protected String getTestIdentityPassword() {
		return TestUtils.randomValue() + getValidIdentityPassword();
	}
}
