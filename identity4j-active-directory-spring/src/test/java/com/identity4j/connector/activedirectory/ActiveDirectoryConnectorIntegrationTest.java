/* HEADER */
package com.identity4j.connector.activedirectory;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.principal.Identity;

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
}