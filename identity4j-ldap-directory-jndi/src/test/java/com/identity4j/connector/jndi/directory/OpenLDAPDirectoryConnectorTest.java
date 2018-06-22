package com.identity4j.connector.jndi.directory;

import static org.junit.Assert.assertEquals;

import java.util.Map;

/*
 * #%L
 * Idenity4J LDAP Directory JNDI
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

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.principal.Identity;

public class OpenLDAPDirectoryConnectorTest extends AbstractConnectorTest<DirectoryConfiguration> {

	public OpenLDAPDirectoryConnectorTest() {
		super("/openldap-directory-connector.properties");
	}

	@Override
	protected void populateIdentityForCreation(Identity newIdentity) {
		newIdentity.setAttribute("gidNumber",
				String.valueOf(configurationParameters.getIntegerOrFail("directory.validGidNumber")));
		newIdentity.setAttribute("uidNumber", String.valueOf(1000 + (int) (Math.random() * 1000)));
		newIdentity.setAttribute("sn", "Surname");
		newIdentity.setAttribute("homeDirectory", "/home/" + newIdentity.getPrincipalName());
	}

	@Override
	protected void updateAttributes(Identity identity2, Map<String, String[]> attributes) {
		attributes.put("sn", new String[] { "Newsurname" });
	}

	@Override
	protected void assertUpdatedAttributes(Identity identity, Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {
		assertEquals("Attributes should be identical", "Newsurname", newIdentity.getAttribute("sn"));
	}
}
