package com.identity4j.connector.sap.users;

/*
 * #%L
 * Identity4J SAP Users
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.Media;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.jdbc.JDBCConfiguration;
import com.identity4j.connector.jdbc.JDBCConnector;
import com.identity4j.connector.jdbc.JDBCIdentity;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.util.StringUtil;
import com.identity4j.util.passwords.DefaultPasswordCharacteristics;
import com.identity4j.util.passwords.PasswordCharacteristics;

public class SAPUsersConnector extends JDBCConnector<SAPUsersConfiguration> {

	public final static Log LOG = LogFactory.getLog(SAPUsersConnector.class);

	public final static String ATTR_EMAIL_ADDRESS = "EMAIL ADDRESS";
	public final static String ATTR_LOCALE = "LOCALE";
	public final static String ATTR_TIME_ZONE = "TIME ZONE";
	public final static String ATTR_RSERVE_REMOTE_SOURCES = "RSERVE REMOTE SOURCES";
	public final static String ATTR_STATEMENT_MEMORY_LIMIT = "STATEMENT MEMORY LIMIT";
	public final static String ATTR_STATEMENT_THREAD_LIMIT = "STATEMENT THREAD LIMIT";

	public final static List<String> NATIVE_ATTRIBUTES = Arrays.asList(new String[] { ATTR_EMAIL_ADDRESS });

	private static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(
			Arrays.asList(new ConnectorCapability[] { ConnectorCapability.passwordChange,
					ConnectorCapability.passwordSet, ConnectorCapability.createUser, ConnectorCapability.deleteUser,
					ConnectorCapability.updateUser, ConnectorCapability.authentication, ConnectorCapability.identities,
					ConnectorCapability.hasPasswordPolicy, ConnectorCapability.tracksLastPasswordChange,
					ConnectorCapability.accountLocking, ConnectorCapability.accountDisable, ConnectorCapability.roles,
					ConnectorCapability.forcePasswordChange, ConnectorCapability.createRole,
					ConnectorCapability.identityAttributes, ConnectorCapability.roleAttributes,
                    ConnectorCapability.caseInsensitivePrincipalNames }));

	@Override
	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		switch (((SAPUsersConfiguration) configuration).getAuthMode()) {
		case JDBC:
			try {
				Connection userConnection = DriverManager.getConnection(configuration.generateJDBCUrl(),
						identity.getPrincipalName(), password == null ? null : new String(password));
				userConnection.close();
				if (identity.getPasswordStatus().getType() == PasswordStatusType.changeRequired) {
					throw new PasswordChangeRequiredException();
				}
			} catch (SQLInvalidAuthorizationSpecException sqle) {
				LOG.debug(String.format("Invalid credentials.", identity.getPrincipalName()), sqle);
				return false;
			} catch (SQLException sqle) {
				throw new ConnectorException();
			}
			return true;
		default:
			return super.areCredentialsValid(identity, password);
		}
	}

	@Override
	public void enableIdentity(Identity identity) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String sql = configuration.getSql(
					configuration.replaceTokens(String.format("ALTER USER ${principalName} ACTIVATE"), identity));
			statement = connect.prepareStatement(sql);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
	}

	@Override
	public void disableIdentity(Identity identity) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String sql = configuration.getSql(
					configuration.replaceTokens(String.format("ALTER USER ${principalName} DEACTIVATE"), identity));
			statement = connect.prepareStatement(sql);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
	}

	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		ResultSet resultSet = null;
		try {
			Identity oldIdentity = getIdentityByName(identity.getPrincipalName());
			connect.setAutoCommit(false);
			PreparedStatement statement = connect.prepareStatement(String.format("ALTER USER %s CLEAR ALL PARAMETERS", identity.getPrincipalName()));
			try {
				statement.executeUpdate();
			} finally {
				statement.close();
			}
			for(Map.Entry<String, String[]> en : identity.getAttributes().entrySet()) {
				statement = connect.prepareStatement(String.format("ALTER USER %s SET PARAMETER '%s' = '%s'", identity.getPrincipalName(), JDBCConfiguration.escapeString(en.getKey()), JDBCConfiguration.escapeString(StringUtil.toDefaultString(en.getValue()))));
				try {
					statement.executeUpdate();
				} finally {
					statement.close();
				}
			}
			updateIdentityRoles(oldIdentity, identity);
			connect.commit();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			try {
				connect.setAutoCommit(true);
			} catch (SQLException e) {
			}
			closeResultSet(resultSet);
		}
	}

	@Override
	public void unlockIdentity(Identity identity) throws ConnectorException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String sql = configuration.getSql(configuration
					.replaceTokens(String.format("ALTER USER ${principalName} DROP CONNECT ATTEMPTS"), identity));
			statement = connect.prepareStatement(sql);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

	}

	@Override
	protected Identity createIdentity(ResultSet resultSet) throws SQLException {
		Identity identity = super.createIdentity(resultSet);
		PreparedStatement statement = null;
		try {
			String sql = String.format("SELECT * FROM \"SYS\".\"USER_PARAMETERS\" WHERE USER_NAME = ?");
			statement = connect.prepareStatement(sql);
			statement.setString(1, identity.getPrincipalName());
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String n = resultSet.getString("PARAMETER");
				String v = resultSet.getString("VALUE");
				if (n.equals(ATTR_EMAIL_ADDRESS)) {
					JDBCIdentity jdbci = (JDBCIdentity) identity;
					jdbci.setAddress(Media.email, v);
				} else {
					identity.setAttribute(n, v);
				}
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
		return identity;
	}

	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
			PasswordResetType type) throws ConnectorException {
		super.setPassword(identity, password, forcePasswordChangeAtLogon, type);
		if (forcePasswordChangeAtLogon) {
			Statement statement = null;
			ResultSet resultSet = null;
			try {
				statement = connect.createStatement();
				statement.executeUpdate("ALTER USER " + identity.getPrincipalName() + " FORCE PASSWORD CHANGE");
			} catch (SQLException e) {
				throw new ConnectorException(e);
			} finally {
				closeStatement(statement);
				closeResultSet(resultSet);
			}
		}

	}

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
		DefaultPasswordCharacteristics dpc = new DefaultPasswordCharacteristics();

		// password_layout = A1a*
		// password_lock_time = 1440 (minutes) TODO not sure what 'lock
		// indefinitelyt' results as
		// password_expiry_warning_time = 14 (days)
		// password_lock_for_system_user
		// *last_used_passwords = 5 (history size)
		// *minimal_password_length = min length
		// minimum_password_lifetime = min password lifetime (days)
		// maximum_password_lifetime = max password life 182
		// maximum_unused_productive_password_lifetime = 365 days (maximum
		// duration of user inactivity)

		dpc.setHistorySize(5);
		dpc.setMaximumSize(16); // TODO IS there a maxmium?
		dpc.setMinimumDigits(0);
		dpc.setMinimumLowerCase(0);
		dpc.setMinimumSymbols(0);
		dpc.setMinimumUpperCase(0);

		/* Seems to be everything except double quotes */
		dpc.setSymbols(new char[] {'!', '$', '\u00a3', '%', '^', '&', '*', '(', ')', '_', '-', '=', '+', '[',
		                    '{', ']', '}', ':', ';', '\'', '@', '~', '#', ',', '<', '.', '>', '|', '\\', '?' });

		// Good source -
		// https://blogs.sap.com/2013/05/15/hana-password-security/

		// TODO There is also "login" in the user profile parameters -
		// http://myhelp.uky.edu/bw/en/22/41c43ac23cef2fe10000000a114084/content.htm
		// TODO also mention of system profile?
		// https://www.guru99.com/how-to-set-password-restrictions.html

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connect.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM PUBLIC.M_PASSWORD_POLICY");
			while (resultSet.next()) {
				String k = resultSet.getString("PROPERTY");
				if (k.equalsIgnoreCase("last_used_passwords")) {
					dpc.setHistorySize(resultSet.getInt("VALUE"));
				} else if (k.equalsIgnoreCase("minimal_password_length")) {
					dpc.setMinimumSize(resultSet.getInt("VALUE"));
				} else if (k.equalsIgnoreCase("password_layout")) {
					String v = resultSet.getString("VALUE");
					dpc.setMinimumDigits(v.contains("1") ? 1 : 0);
					dpc.setMinimumUpperCase(v.contains("A") ? 1 : 0);
					dpc.setMinimumLowerCase(v.contains("a") ? 1 : 0);
					dpc.setMinimumSymbols(v.contains("*") ? 1 : 0);
				}
			}
		} catch (SQLException e) {
			throw new ConnectorException(e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}

		return dpc;
	}

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}

}
