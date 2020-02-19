package com.identity4j.connector;

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

/**
 * Describe individual capabilities a connector may have. This allows UI
 * components to determine what fields and actions etc to make available.
 */
public enum ConnectorCapability {
	/**
	 * The connector is a 'system' template, i.e. is created for internal use of
	 * some sort. This is a hint to the UI to prevent certain actions.
	 */
	system,
	/**
	 * The connector supports account locking
	 */
	accountLocking,
	/**
	 * The connector supports password changing
	 */
	passwordChange,
	/**
	 * The connector supports password setting
	 */
	passwordSet,
	/**
	 * The connector supports account creation
	 */
	createUser,
	/**
	 * The connector supports account update
	 */
	updateUser,
	/**
	 * The connector allows its accounts details to be viewed
	 */
	viewUser,
	/**
	 * The connector supports account deletion
	 */
	deleteUser,
	/**
	 * The connector supports a full name.
	 */
	hasFullName,
	/**
	 * The connector supports an email address.
	 */
	hasEmail,
	/**
	 * The connector allows setting of the identities user name (all identities
	 * must have a user name so there is no matching <code>get</code>
	 * capability)
	 */
	setIdentityName,
	/**
	 * The connector allows setting of the identities role name (all roles must
	 * have a role name so there is no matching <code>get</code> capability)
	 */
	setRoleName,
	/**
	 * The connector allows a GUID to be chosen at account creation time.
	 */
	createIdentityGUID,
	/**
	 * The connector requires that a GUID is chosen
	 */
	requireGUID,
	/**
	 * The connector allows a GGUID to be chosen at role creation time.
	 */
	createRoleGUID,
	/**
	 * The connector allows setting of the GUID. This should be rare
	 */
	setIdentityGUID,
	/**
	 * The connector allows setting of the GGUID. This should be rare
	 */
	setRoleGUID,
	/**
	 * The connector supports the concept of roles.
	 */
	roles,
	/**
	 * The connector supports the concept of authentication
	 */
	authentication,
	/**
	 * The connector supports retrieval of identities
	 */
	identities,
	/**
	 * The connector tracks the users last password change date time
	 */
	tracksLastPasswordChange,
	/**
	 * The connector tracks a users last login date time
	 */
	tracksLastSignOnDate,
	/**
	 * The connector supports the concept of a primary role
	 */
	primaryRole,
	/**
	 * The connector supports role creation
	 */
	createRole,
	/**
	 * The connector supports role update
	 */
	updateRole,
	/**
	 * The connector supports delete deletion
	 */
	deleteRole,
	/**
	 * The connector allows its role details to be viewed
	 */
	viewRole,
	/**
	 * The connector has a password policy that may be retrieved
	 */
	hasPasswordPolicy,
	/**
	 * Case insensitive principal names
	 */
	caseInsensitivePrincipalNames,
	/**
	 * The connector supports account disable
	 */
	accountDisable,
	/**
	 * The connector supports forcing password change next time an identity logs
	 * in
	 */
	forcePasswordChange,
	/**
	 * The connector supports a web based authentication API such as OAuth
	 */
	webAuthentication,
	/**
	 * The connector supports roles within roles
	 */
	childRoles,
	/**
	 * The connector has additional native identity attributes
	 */
	identityAttributes,
	/**
	 * The connector has additional native role attributes
	 */
	roleAttributes,
	/**
	 * The connector has allows arbitrary identity attributes
	 */
	customIdentityAttributes,
	/**
	 * The connector has allows arbitrary role attributes
	 */
	customRoleAttributes,
	/**
	 * The connector has the concept of a 'tag'. Typically, this may be a timestamp of when
	 * user or group details last changed. Client code can query the current tag and compare it against
	 * the last stored tag. If it has change, then the new details can be retrieved, otherwise
	 * nothing is to be done and the client code can use cached details.
	 */
	tag
}
