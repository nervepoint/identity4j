package com.identity4j.connector.as400;

import com.ibm.as400.access.User;
import com.identity4j.connector.principal.IdentityImpl;

public class As400Identity extends IdentityImpl implements As400Principal {

	private static final long serialVersionUID = 9058250982847754663L;
	private User as400User;

	public As400Identity(User as400User, String guid, String principalName) {
		super(guid, principalName);
		this.as400User = as400User;
	}

	public As400Identity(User as400User, String principalName) {
		super(principalName);
		this.as400User = as400User;
	}

	public User getNativeUser() {
		return as400User;
	}

}
