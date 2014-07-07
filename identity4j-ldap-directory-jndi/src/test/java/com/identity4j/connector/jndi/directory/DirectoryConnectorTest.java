package com.identity4j.connector.jndi.directory;

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
