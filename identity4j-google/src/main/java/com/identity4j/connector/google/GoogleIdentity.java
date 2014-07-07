package com.identity4j.connector.google;

import com.identity4j.connector.principal.IdentityImpl;

/**
 * Identity represented in google data store
 * 
 * @author gaurav
 *
 */
public class GoogleIdentity extends IdentityImpl{

	private static final long serialVersionUID = 5720450424208555835L;

	public GoogleIdentity(String guid, String principalName) {
		super(guid, principalName);
	}
	
	public GoogleIdentity(String principalName) {
		this(null, principalName);
	}
	

}
