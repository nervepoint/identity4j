package com.identity4j.connector.aws;

import com.identity4j.connector.principal.IdentityImpl;

public class AwsIdentity extends IdentityImpl {

	private static final long serialVersionUID = 6337139802482886153L;
	
	public AwsIdentity(String guid, String principalName) {
		super(guid, principalName);
	}
	
	public AwsIdentity(String principalName) {
		this(null, principalName);
	}

}
