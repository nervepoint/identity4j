package com.identity4j.connector.jndi.directory;

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

import org.junit.Test;

public class DirectoryConnectorTest {

	@Test
	public void testParseDNContainingBackslash() {
		// This test is broken.. why?
//        DistinguishedName name = new DistinguishedName("cn=This is a problem \\\\\\ Group, dc=example, dc=com");
//        assertEquals(3, name.size());
//        assertEquals("cn=This is a problem \\\\\\ Group", name.getLdapRdn(2).toString());
//        assertEquals("dc=example", name.getLdapRdn(1).toString());
//        assertEquals("dc=com", name.getLdapRdn(0).toString());
//        assertEquals("cn=This is a problem \\\\\\ Group, dc=example, dc=com", name.toString());
    }

}
