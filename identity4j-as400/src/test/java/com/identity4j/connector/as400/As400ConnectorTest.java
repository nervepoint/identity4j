package com.identity4j.connector.as400;

/*
 * #%L
 * Identity4J AS400
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

import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;

public class As400ConnectorTest extends AbstractConnectorTest<As400Configuration> {

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
