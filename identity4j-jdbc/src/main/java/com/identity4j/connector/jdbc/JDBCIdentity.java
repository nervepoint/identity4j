package com.identity4j.connector.jdbc;

import com.identity4j.connector.principal.IdentityImpl;

public class JDBCIdentity extends IdentityImpl {

	private static final long serialVersionUID = 4140374422471162920L;

	public JDBCIdentity(String guid, String principalName) {
		super(guid, principalName);
	}

}
