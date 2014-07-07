/* HEADER */
package com.identity4j.connector.jndi.directory;

import javax.naming.Name;

import com.identity4j.connector.principal.RoleImpl;

public class DirectoryRole extends RoleImpl {

	private static final long serialVersionUID = -4687400736606488002L;

	private final Name dn;

    public DirectoryRole(String guid, String principalName, Name dn) {
        super(guid, principalName);
        this.dn = dn;
    }

    public final Name getDn() {
        return dn;
    }
}