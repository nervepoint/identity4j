package com.identity4j.connector;

import com.identity4j.connector.principal.Identity;

public interface IdentityProcessor {

	void processIdentity(Identity identity, Connector con);
}
