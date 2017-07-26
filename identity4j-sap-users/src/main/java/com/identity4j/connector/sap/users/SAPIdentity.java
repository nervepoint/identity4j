package com.identity4j.connector.sap.users;

import com.identity4j.connector.principal.IdentityImpl;

public class SAPIdentity extends IdentityImpl{

	private static final long serialVersionUID = 4512400124762759579L;
	
	public SAPIdentity(String principalName) {
		super(principalName);
	}

	public SAPIdentity(String guid, String principalName) {
		super(guid, principalName);
	}

}
