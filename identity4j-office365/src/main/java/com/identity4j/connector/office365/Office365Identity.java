package com.identity4j.connector.office365;

import com.identity4j.connector.principal.IdentityImpl;

/**
 * Identity represented in Windows Azure Active Directory data store
 * 
 * @author gaurav
 *
 */
public class Office365Identity extends IdentityImpl{

	private static final long serialVersionUID = 3161726959507786145L;

	public Office365Identity(String guid, String principalName) {
		super(guid, principalName);
	}
	
	public Office365Identity(String principalName) {
		this(null, principalName);
	}
	

}
