package com.identity4j.connector.mysql.users;

import java.sql.SQLException;

/*
 * #%L
 * Identity4J MySQL Users Connector
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

import com.identity4j.connector.jdbc.JDBCConfiguration;
import com.identity4j.connector.jdbc.NamedParameterStatement;
import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

public class MySQLUsersConfiguration extends JDBCConfiguration {

	public enum SchemaVersion {
		PRE_5_7, POST_5_7, AUTO
	}

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

	//
	public static final String MYSQL_VERSION = "mysqlVersion";

	public MySQLUsersConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	public SchemaVersion getMysqlVersion() {
		return SchemaVersion
				.valueOf(configurationParameters.getStringOrDefault(MYSQL_VERSION, SchemaVersion.AUTO.name()));
	}

	public void setMysqlVersion(SchemaVersion schemaVersion) {
		configurationParameters.set(MYSQL_VERSION, schemaVersion.name());
	}

	@Override
	public String getIdentityPasswordEncoding() {
		return "plain";
	}

	@Override
	public String getJDBCDriverName() {
		return "mysql";
	}

	@Override
	public Integer getDefaultPort() {
		return new Integer(3306);
	}

	@Override
	public String getDriverClassName() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public String getJDBUrlProperties(boolean safe) {
		StringBuilder buf = new StringBuilder();
		buf.append("user=");
		buf.append(configurationParameters.getString(JDBC_USERNAME));
		buf.append("&");
		buf.append("password=");
		if (safe) {
			buf.append("********");
		} else {
			buf.append(configurationParameters.getString(JDBC_PASSWORD));
		}
		return buf.toString();
	}

	public String getSelectIdentitySQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_SELECT,
				"SELECT * FROM mysql.user WHERE User = ? and  ( Host = ? or Host = ? ) ");
	}

	@Override
	public String getSelectIdentitiesSQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITIES_TABLE_SELECT, "SELECT * FROM mysql.user");
	}

	public NamedParameterStatement prepareSelectIdentity(NamedParameterStatement statement, String user, String host)
			throws SQLException {
		statement.parse(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_SELECT,
				"SELECT * FROM mysql.user WHERE user = :user AND ( host = :host OR host = :disabledHost OR ( :host = '%' AND host = '' ) )"));
		statement.setString("user", user);
		statement.setString("host", normalizeHost(host));
		statement.setString("disabledHost", getDisabledIdentityHostName(normalizeHost(host)));
		return statement;
	}

	public NamedParameterStatement prepareGrantIdentity(NamedParameterStatement statement, String access, String user,
			String host) throws SQLException {
		statement.parse(
				configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_GRANT, "GRANT :access TO :user@:host")
						.replace(":access", access));
		statement.setString("user", user);
		statement.setString("host", normalizeHost(host));
		return statement;
	}

	public NamedParameterStatement prepareRevokeIdentity(NamedParameterStatement statement, String access, String user,
			String host) throws SQLException {
		statement.parse(
				configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_GRANT, "REVOKE :access FROM :user@:host")
						.replace(":access", access));
		statement.setString("user", user);
		statement.setString("host", normalizeHost(host));
		return statement;
	}

	public NamedParameterStatement prepareShowGrants(NamedParameterStatement statement, String user, String host)
			throws SQLException {
		statement.parse(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_GRANT_SHOW,
				"SHOW GRANTS FOR :user@:host"));
		statement.setString("user", user);
		statement.setString("host", host);
		return statement;
	}

	public NamedParameterStatement prepareCreateIdentity(NamedParameterStatement statement, String user, String host,
			String password) throws SQLException {
		if ((StringUtil.isNullOrEmpty(host))) {
			statement.parse(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_GRANT,
					"CREATE USER :user IDENTIFIED BY :password"));
			statement.setString("user", user);
			statement.setString("password", new String(password));
		} else {
			statement.parse(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_GRANT,
					"CREATE USER :user@:host IDENTIFIED BY :password"));
			statement.setString("host", host);
			statement.setString("user", user);
			statement.setString("password", new String(password));
		}
		return statement;
	}

	public String getDeleteIdentitySQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_DELETE, "DROP USER ?@?");
	}

	public String getEnableDisableIdentitySQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_ENABLE_DISABLE,
				"UPDATE mysql.user set Host = ? where User = ? and Host = ?");
	}

	public String getFlushPrivilegesSQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_ENABLE_DISABLE, "FLUSH PRIVILEGES");
	}

	public String getSelectPasswordSQL(SchemaVersion schemeVersion) {
		switch (schemeVersion) {
		case PRE_5_7:
			return configurationParameters.getStringOrDefault(SQL_PASSWORD_SELECT,
					"SELECT * FROM mysql.user WHERE PASSWORD = PASSWORD(?) AND User = ? AND Host = ?");
		case POST_5_7:
			return configurationParameters.getStringOrDefault(SQL_PASSWORD_SELECT,
					"SELECT * FROM mysql.user WHERE plugin = 'mysql_native_password' AND authentication_string = PASSWORD(?) AND User = ? AND Host = ?");
		default:
			throw new UnsupportedOperationException();
		}
	}

	public String getPasswordSetSQL(SchemaVersion version) {
		return configurationParameters.getStringOrDefault(SQL_PASSWORD_SET, "SET PASSWORD FOR ?@? = PASSWORD(?)");
	}

	public boolean getIdentityEnableDisableFeature() {
		return Boolean
				.parseBoolean(configurationParameters.getStringOrDefault(IDENTITY_ENABLE_DISABLE_FEATURE, "true"));
	}

	public String getDisableFlag() {
		return configurationParameters.getStringOrDefault(IDENTITY_ENABLE_DISABLE_FLAG, "dis!");
	}

	public String getDisabledIdentityHostName(String host) {
		return getDisableFlag() + host;
	}

	public String getDisabledIdentityPrincipalName(String user, String host) {
		if (StringUtil.isNullOrEmpty(host))
			return user + AMPERSAND + getDisableFlag();
		else
			return user + AMPERSAND + getDisableFlag() + host;
	}

	public String getEnabledIdentityPrincipalName(String user, String host) {
		if (StringUtil.isNullOrEmpty(host))
			return user;
		else
			return user + AMPERSAND + host;
	}

	private String normalizeHost(String host) {
		return StringUtil.isNullOrEmpty(host) ? "%" : host;
	}

}
