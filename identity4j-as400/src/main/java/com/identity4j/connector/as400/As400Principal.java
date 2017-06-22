package com.identity4j.connector.as400;

import com.ibm.as400.access.User;
import com.identity4j.connector.principal.Principal;

public interface As400Principal extends Principal {

	User getNativeUser();

}
