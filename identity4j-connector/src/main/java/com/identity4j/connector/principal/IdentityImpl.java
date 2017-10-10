/* HEADER */
package com.identity4j.connector.principal;

/*
 * #%L
 * Identity4J Connector
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



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.identity4j.connector.Media;

public class IdentityImpl extends AbstractPrincipal implements Identity {

	private static final long serialVersionUID = -8740460330836000188L;

	private String fullName;
	private Date lastSignOnDate;
	private final Collection<Role> roles = new ArrayList<Role>();
	private PasswordStatus passwordStatus = new PasswordStatus();
	private AccountStatus accountStatus = new AccountStatus();
	private String otherName;
	private Map<Media, String> contactDetails = new HashMap<Media, String>();

	public IdentityImpl(String principalName) {
		this(null, principalName);
	}

	public IdentityImpl(String guid, String principalName) {
		super(guid, principalName);
	}

	public final String getFullName() {
		return fullName;
	}

	public final void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public final Date getLastSignOnDate() {
		return lastSignOnDate;
	}

	public final void setLastSignOnDate(Date lastSignOnDate) {
		this.lastSignOnDate = lastSignOnDate;
	}

	public final boolean memberOf(Role role) {
		if (roles.isEmpty()) {
			return false;
		}

		for (Role assignedRole : roles) {
			if (assignedRole.getGuid().equals(role.getGuid())) {
				return true;
			}
		}
		return false;
	}

	public final Role[] getRoles() {
		return roles.toArray(new Role[roles.size()]);
	}

	public final synchronized void setRoles(Role[] roles) {
		for (Role role : roles) {
			if (role == null) {
				throw new IllegalArgumentException("Role array may not contain null roles");
			}
		}
		this.roles.clear();
		this.roles.addAll(Arrays.asList(roles));
	}

	/**
	 * @param role
	 */
	public final void addRole(Role role) {
		if (role == null) {
			throw new IllegalArgumentException("May not add null role");
		}
		roles.add(role);
	}

	/**
	 * @param role
	 */
	public final void removeRole(Role role) {
		if (role == null) {
			throw new IllegalArgumentException("May not remove null role");
		}
		roles.remove(role);
	}

	/**
	 * Set the roles
	 * 
	 * @param roles roles
	 */
	public final void setRoles(Collection<? extends Role> roles) {
		for (Role role : roles) {
			if (role == null) {
				throw new IllegalArgumentException("Role array may not contain null roles");
			}
		}
		this.roles.clear();
		this.roles.addAll(roles);
	}

	@Override
	public final boolean equals(Object obj) {
		if (!(obj instanceof Identity)) {
			return false;
		}
		Identity identity = (Identity) obj;
		return getGuid().equals(identity.getGuid());
	}

	public final int compareTo(Identity identity) {
		return getPrincipalName().compareTo(identity.getPrincipalName());
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

	@Override
	public PasswordStatus getPasswordStatus() {
		return passwordStatus;
	}

	@Override
	public void setPasswordStatus(PasswordStatus passwordStatus) {
		this.passwordStatus = passwordStatus;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[passwordStatus='").append(getPasswordStatus() == null ? "" : getPasswordStatus().toString());
		builder.append("', lastSignOnDate='").append(getLastSignOnDate() == null ? "" : getLastSignOnDate().toString());
		builder.append("', roles='").append(roles == null ? "" : roles).append("']");
		return builder.toString();
	}

	public String getOtherName() {
		return otherName;
	}

	public void setOtherName(String otherName) {
		this.otherName = otherName;
	}

	@Override
	public String getAddress(Media media) {
		return contactDetails.get(media);
	}

	public void setAddress(Media media, String value) {
		if (value == null) {
			contactDetails.remove(media);
		} else {
			contactDetails.put(media, value);
		}
	}
}