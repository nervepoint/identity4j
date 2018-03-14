/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

/*
 * #%L
 * Identity4J Active Directory JNDI
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