package com.identity4j.connector.office365;

/*
 * #%L
 * Identity4J OFFICE 365
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
