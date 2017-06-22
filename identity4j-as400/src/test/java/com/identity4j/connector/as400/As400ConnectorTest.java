package com.identity4j.connector.as400;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;

public class As400ConnectorTest extends AbstractConnectorTest {

	private int lastAmt;
	private String lastDesc;

	public As400ConnectorTest() {
		super("/as400-connector.properties");
		
		// Checking old credentials disables account
		checkOldCredentials = false;
	}

	@Test
	public void test() {
	}

	@Override
	protected void updateAttributes(Identity identity, Map<String, String[]> attributes) {
		lastAmt = (int) (Math.random() * 100) * 4;
		attributes.put("maximumStorageAllowed", new String[] { String.valueOf(lastAmt)});
	}
	

	@Override
	protected void assertUpdatedAttributes(Identity identity,
			Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {
		assertEquals("Attributes should be identical", String.valueOf(lastAmt),
				newIdentity.getAttribute("maximumStorageAllowed"));
	}

	@Override
	protected void updateRoleAttributes(Role role, Map<String, String[]> attributes) {
		lastDesc= "Description " +  (Math.random() * 100);
		attributes.put("description", new String[] { String.valueOf(lastDesc)});
	}
	

	@Override
	protected void assertUpdatedRoleAttributes(Role role,
			Map<String, String[]> attributes, Role newRole,
			Map<String, String[]> newAttributes) {
		assertEquals("Attributes should be identical", lastDesc,
				newRole.getAttribute("description"));
	}

}
