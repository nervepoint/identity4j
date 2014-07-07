package com.identity4j.connector.salesforce;

import com.identity4j.connector.principal.IdentityImpl;

/**
 * Identity represented in Salesforce data store
 * 
 * @author gaurav
 *
 */
public class SalesforceIdentity extends IdentityImpl{

	private static final long serialVersionUID = 4512400124762759579L;
	
	public SalesforceIdentity(String principalName) {
		super(principalName);
	}

	public SalesforceIdentity(String guid, String principalName) {
		super(guid, principalName);
	}

}
