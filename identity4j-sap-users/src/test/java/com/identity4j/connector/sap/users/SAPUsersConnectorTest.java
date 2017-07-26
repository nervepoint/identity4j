package com.identity4j.connector.sap.users;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.principal.Identity;

public class SAPUsersConnectorTest extends AbstractConnectorTest {

	private int lastAmt;

	public SAPUsersConnectorTest() {
		super("/sap-users-connector.properties");
		setReecreateTestUserOnSelectedTests(true);
	}

	@Test
	public void test() {
	}



	@Override
	protected void assertUpdatedAttributes(Identity identity,
			Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {
		assertEquals("Attributes should be identical", String.valueOf(lastAmt),
				newIdentity.getAttribute("RANDOMNO"));
	}

	@Override
	protected void updateAttributes(Identity identity, Map<String, String[]> attributes) {
		lastAmt = (int) (Math.random() * 100) * 4;
		attributes.put("RANDOMNO", new String[] { String.valueOf(lastAmt)});
	}
}
