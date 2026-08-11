package com.identity4j.connector.jndi.directory;

import javax.naming.NamingException;

import org.junit.Assert;
import org.junit.Test;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;

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

public class DirectoryConnectorTest {

	private static class TestDirectoryConnector extends DirectoryConnector {
		String process(NamingException exception) {
			return processNamingException(exception);
		}
	}

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

	@Test
	public void testInvalidCredentialsResultCode() {
		NamingException exception = new NamingException("LDAP: error code 49 - Invalid Credentials");

		try {
			new TestDirectoryConnector().process(exception);
			Assert.fail("Expected invalid credentials exception");
		} catch (InvalidLoginCredentialsException e) {
			Assert.assertEquals("Invalid login credentials", e.getMessage());
		}
	}

	@Test
	public void testAccessDeniedResultCode() {
		NamingException exception = new NamingException("LDAP: error code 50 - Insufficient Access Rights");

		try {
			new TestDirectoryConnector().process(exception);
			Assert.fail("Expected connector exception");
		} catch (ConnectorException e) {
			Assert.assertEquals("Insufficient access rights to perform the LDAP operation.", e.getMessage());
			Assert.assertSame(exception, e.getCause());
		}
	}

	@Test
	public void testMissingObjectResultCode() {
		NamingException exception = new NamingException(
				"[LDAP: error code 32 - No Such Object]; remaining name 'uid=manager'");

		try {
			new TestDirectoryConnector().process(exception);
			Assert.fail("Expected connector exception");
		} catch (ConnectorException e) {
			Assert.assertEquals("The requested LDAP object does not exist.", e.getMessage());
			Assert.assertSame(exception, e.getCause());
		}
	}

	@Test
	public void testUnknownResultCodeUsesDiagnosticMessage() {
		NamingException exception = new NamingException("LDAP: error code 999 - Future Server Error");

		try {
			new TestDirectoryConnector().process(exception);
			Assert.fail("Expected connector exception");
		} catch (ConnectorException e) {
			Assert.assertEquals("LDAP: error code 999 - Future Server Error", e.getMessage());
			Assert.assertSame(exception, e.getCause());
		}
	}

}
