package com.identity4j.connector.zendesk;

import com.identity4j.connector.principal.IdentityImpl;

/**
 * Identity represented in Zendesk data store
 * 
 * @author gaurav
 *
 */
public class ZendeskIdentity extends IdentityImpl{

	private static final long serialVersionUID = 4512400124762759579L;
	
	public ZendeskIdentity(String principalName) {
		super(principalName);
	}

	public ZendeskIdentity(String guid, String principalName) {
		super(guid, principalName);
	}

}
