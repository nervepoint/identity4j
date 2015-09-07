/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

import javax.naming.Name;

import com.identity4j.connector.principal.RoleImpl;

public class ActiveDirectoryGroup extends RoleImpl {
	private static final long serialVersionUID = -7675417973639150879L;
	private final Name dn;
	private final Long rid;
	
	public ActiveDirectoryGroup(String guid, String samAccountName, Name dn, byte[] sid) {
		super(guid, samAccountName);
		this.dn = dn;
		rid = ActiveDirectoryUtils.getRIDFromSID(sid);
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