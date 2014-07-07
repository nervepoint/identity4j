/* HEADER */
package com.identity4j.connector.principal;


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