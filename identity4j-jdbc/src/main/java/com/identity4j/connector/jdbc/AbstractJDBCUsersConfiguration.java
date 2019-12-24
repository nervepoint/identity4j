package com.identity4j.connector.jdbc;

/*
 * #%L
 * Identity4J JDBC
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

import com.identity4j.util.MultiMap;

public abstract class AbstractJDBCUsersConfiguration extends JDBCConfiguration {

	private static final String AMPERSAND = "@";

	// Enable Disable feature enable
	public static final String IDENTITY_ENABLE_DISABLE_FEATURE = "identityEnableDisableFeature";
	public static final String IDENTITY_ENABLE_DISABLE_FLAG = "identityEnableDisableFlag";

	// Identity Table
	public static final String SQL_IDENTITIES_TABLE_SELECT = "sqlIdentitiesTableGrant";
	public static final String SQL_IDENTITY_TABLE_GRANT = "sqlIdentityTableGrant";
	public static final String SQL_IDENTITY_TABLE_REVOKE = "sqlIdentityTableRevoke";
	public static final String SQL_IDENTITY_TABLE_GRANT_SHOW = "sqlIdentityTableGrantShow";
	public static final String SQL_IDENTITY_TABLE_ENABLE_DISABLE = "sqlIdentityTableEnableDisable";

	// Password
	public static final String SQL_PASSWORD_SET = "sqlPasswordSet";

	// Flush Privileges
	public static final String SQL_FLUSH_PRIVILEGES = "sqlFlushPrivileges";

	public AbstractJDBCUsersConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	public String getSelectIdentitySQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_SELECT,
				"SELECT * FROM mysql.user WHERE ( User = ? or User = ? ) and Host = ?");
	}

	@Override
	public String getSelectIdentitiesSQL() {
		return configurationParameters.getStringOrFail(SQL_IDENTITIES_TABLE_SELECT);
	}

	public String getGrantIdentitySQL(String access, String user, String host) {
		return String.format(
				configurationParameters.getStringOrFail(SQL_IDENTITY_TABLE_GRANT), access,
				user, host);
	}

	public String getRevokeIdentitySQL(String access, String user, String host) {
		return String.format(
				configurationParameters.getStringOrFail(SQL_IDENTITY_TABLE_REVOKE),
				access, user, host);
	}

	public String getGrantShowIdentitySQL() {
		return String.format(
				configurationParameters.getStringOrFail(SQL_IDENTITY_TABLE_GRANT_SHOW));
	}

	public String getCreateIdentitySQL(String user, String host, String password) {
		return String.format(configurationParameters.getStringOrFail(SQL_IDENTITY_TABLE_CREATE), user, host, password);
	}

	public String getDeleteIdentitySQL() {
		return configurationParameters.getStringOrFail(SQL_IDENTITY_TABLE_DELETE);
	}

	public String getEnableDisableIdentitySQL() {
		return configurationParameters.getStringOrFail(SQL_IDENTITY_TABLE_ENABLE_DISABLE);
	}

	public String getFlushPrivilegesSQL() {
		return configurationParameters.getStringOrFail(SQL_IDENTITY_TABLE_ENABLE_DISABLE);
	}

	public String getSelectPasswordSQL() {
		return configurationParameters.getStringOrFail(SQL_PASSWORD_SELECT);
	}

	public String getPasswordSetSQL() {
		return configurationParameters.getStringOrFail(SQL_PASSWORD_SET);
	}

	public boolean getIdentityEnableDisableFeature() {
		return Boolean
				.parseBoolean(configurationParameters.getStringOrFail(IDENTITY_ENABLE_DISABLE_FEATURE));
	}

	public String getDisableFlag() {
		return configurationParameters.getStringOrFail(IDENTITY_ENABLE_DISABLE_FLAG);
	}

	public String getDisabledIdentityPrincipalName(String user, String host) {
		return user + AMPERSAND + getDisableFlag() + host;
	}

	public String getEnabledIdentityPrincipalName(String user, String host) {
		return user + AMPERSAND + host;
	}

}
