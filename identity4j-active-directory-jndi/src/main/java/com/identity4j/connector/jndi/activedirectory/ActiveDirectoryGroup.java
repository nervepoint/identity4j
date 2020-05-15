/* HEADER */
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


import javax.naming.Name;

import com.identity4j.connector.principal.RoleImpl;

public class ActiveDirectoryGroup extends RoleImpl {
	private static final long serialVersionUID = -7675417973639150879L;
	private final Name dn;
	private final Long rid;
    private final String[] memberOf;
    private final String[] member;
	
	public ActiveDirectoryGroup(String guid, String samAccountName, Name dn, byte[] sid,  String[] memberOf, String[] member) {
		super(guid, samAccountName);
		this.dn = dn;
        this.member = member;
        this.memberOf = memberOf;
		rid = ActiveDirectoryUtils.getRIDFromSID(sid);
	}

    public final String[] getMember() {
        return member;
    }

    public final String[] getMemberOf() {
    	return memberOf;
    }

	/**
	 * Get the dn
	 * 
	 * @return
	 */
	public final Name getDn() {
		return dn;
	}

	/**
	 * Users, computers, and groups (collectively known as "security principals"
	 * ) that are stored in Active Directory are assigned Security Identifiers
	 * (SIDS), which are unique alphanumeric numeric strings that map to a
	 * single object in the domain. SIDS consist of a domain-wide SID
	 * concatenated with a monotonically-increasing relative identifier (RID)
	 * that is allocated by each Windows 2000 domain controller in the domain.
	 * 
	 * http://support.microsoft.com/kb/305475
	 * 
	 * @return rid
	 */
	public final Long getRid() {
		return rid;
	}

}