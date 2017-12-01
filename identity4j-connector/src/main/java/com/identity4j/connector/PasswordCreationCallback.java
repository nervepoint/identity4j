package com.identity4j.connector;

import com.identity4j.connector.principal.Identity;

public interface PasswordCreationCallback {

	char[] createPassword(Identity identity);

}
