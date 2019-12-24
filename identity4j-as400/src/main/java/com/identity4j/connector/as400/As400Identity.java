package com.identity4j.connector.as400;

/*
 * #%L
 * Identity4J AS400
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
