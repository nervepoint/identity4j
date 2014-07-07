/* HEADER */
package com.identity4j.connector.jndi.directory;

import javax.naming.Name;

import com.identity4j.connector.principal.IdentityImpl;

public class DirectoryIdentity extends IdentityImpl {
	private static final long serialVersionUID = 1L;

	private final Name dn;

	public DirectoryIdentity(String guid, String principalName, Name dn) {
		super(guid, principalName);
		this.dn = dn;
	}

	public final Name getDn() {
		return dn;
	}

}