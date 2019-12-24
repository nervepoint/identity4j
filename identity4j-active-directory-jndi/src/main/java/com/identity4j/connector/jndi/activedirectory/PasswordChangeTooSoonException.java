package com.identity4j.connector.jndi.activedirectory;

/*
 * #%L
 * Identity4J Active Directory JNDI
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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.identity4j.connector.exception.ConnectorException;

public class PasswordChangeTooSoonException extends ConnectorException {

	Date lastPasswordChange;
	public PasswordChangeTooSoonException(Date lastPasswordChange) {
		super("Password change not allowed due to minimum age policy. Last change was " +  new SimpleDateFormat("yyyy-MM-dd HH:mm").format(lastPasswordChange));
		this.lastPasswordChange = lastPasswordChange;
	}

	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}
	
	private static final long serialVersionUID = 7540522250199953817L;

}
