package com.identity4j.connector.jdbc;

import com.identity4j.connector.principal.RoleImpl;

public class JDBCRole extends RoleImpl {

	private static final long serialVersionUID = 2806889574818787939L;

	public JDBCRole(String guid, String roleName) {
		super(guid, roleName);
	}

}
