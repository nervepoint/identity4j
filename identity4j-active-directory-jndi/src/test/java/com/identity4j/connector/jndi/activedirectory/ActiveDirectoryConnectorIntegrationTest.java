/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Assume;
import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.principal.Identity;
import com.identity4j.util.TestUtils;


public class ActiveDirectoryConnectorIntegrationTest extends
		AbstractConnectorTest {

	public ActiveDirectoryConnectorIntegrationTest() {
		super("/active-directory-connector.properties");
	}

	@Override
	protected void assertUpdatedAttributes(Identity identity,
			Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {
		assertEquals("Attributes should be identical", "My Office",
				newIdentity.getAttribute("physicalDeliveryOfficeName"));
	}

	@Override
	protected void updateAttributes(Identity identity,
			Map<String, String[]> attributes) {
		attributes.put("physicalDeliveryOfficeName",
				new String[] { "My Office" });
	}
	
	@Override
	@Test
	public void changePassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(
				ConnectorCapability.passwordChange));

		try { 
			String password = "aA" + TestUtils.randomValue()+ "1$12123123######121221212";
			connector.changePassword(identityName, identityGuid,
					identityPassword.toCharArray(), password.toCharArray());
			assertPasswordChange(identityName, identityPassword, password);
		} finally {
			// reset to original password
			connector.setPassword(identityName, identityGuid,
					identityPassword.toCharArray(), false);
		}
	}
}