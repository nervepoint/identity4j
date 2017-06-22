package com.identity4j.connector.as400;

import com.ibm.as400.access.UserGroup;
import com.identity4j.connector.principal.RoleImpl;

public class As400Role extends RoleImpl implements As400Principal {

	private static final long serialVersionUID = -942438845698381278L;
	private UserGroup as400Group;

	public As400Role(UserGroup as400Group, String guid, String roleName) {
		super(guid, roleName);
		this.as400Group = as400Group;
	}

	public UserGroup getNativeUser() {
		return as400Group;
	}

}
