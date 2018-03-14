package com.identity4j.connector.sap.users;

/*
 * #%L
 * Identity4J SAP Users
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
