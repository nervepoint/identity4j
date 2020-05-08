package com.identity4j.connector.sap.users;

import com.identity4j.connector.Connector;

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

import com.identity4j.connector.Connector.PasswordResetType;
import com.identity4j.connector.jdbc.JDBCConfiguration;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.MultiMap;
import com.identity4j.util.crypt.impl.PlainEncoder;

/**
 * Configuration class provides access to properties configured
 */
public class SAPUsersConfiguration extends JDBCConfiguration {

	public final static String AUTH_MODE = "sap.authMode";

	public final static String SAP_DROP_USER_CASCADE = "sap.dropUserCascade";

	public enum AuthMode {
		JDBC
	}

	public SAPUsersConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
		if (!configurationParameters.containsKey(SQL_IDENTITY_PASSWORD_ENCODING)) {
			configurationParameters.set(SQL_IDENTITY_PASSWORD_ENCODING, PlainEncoder.ID);
		}
		if (!configurationParameters.containsKey(SQL_ROLE_TABLE_GUID)) {
			configurationParameters.set(SQL_ROLE_TABLE_GUID, "ROLE_ID");
		}
		if (!configurationParameters.containsKey(SQL_ROLE_TABLE_PRINCIPAL_NAME)) {
			configurationParameters.set(SQL_ROLE_TABLE_PRINCIPAL_NAME, "ROLE_NAME");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_GUID)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_GUID, "USER_ID");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_PRINCIPAL_NAME)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_PRINCIPAL_NAME, "USER_NAME");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE)) {
			configurationParameters.set(SQL_IDENTITY_TABLE, "SYS.USERS");
		}
		if (!configurationParameters.containsKey(SQL_ROLE_TABLE)) {
			configurationParameters.set(SQL_ROLE_TABLE, "SYS.ROLES");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_LAST_SIGNON)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_LAST_SIGNON, "LAST_SUCCESSFUL_CONNECT");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_ENABLED)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_ENABLED, "USER_DEACTIVATED");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_ENABLED_VALUE)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_ENABLED_VALUE, "FALSE");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_DISABLED_VALUE)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_DISABLED_VALUE, "TRUE");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE, "PASSWORD_CHANGE_NEEDED");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE_VALUE)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE_VALUE, "TRUE");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_NO_FORCE_PASSWORD_CHANGE_VALUE)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_NO_FORCE_PASSWORD_CHANGE_VALUE, "FALSE");
		}
		if (!configurationParameters.containsKey(SQL_IDENTITY_TABLE_LAST_PASSWORD_CHANGE)) {
			configurationParameters.set(SQL_IDENTITY_TABLE_LAST_PASSWORD_CHANGE, "LAST_PASSWORD_CHANGE_TIME");
		}
		if (!configurationParameters.containsKey(SQL_ROLE_IDENTITY_TABLE_SELECT_BY_IDENTITY)) {
			configurationParameters.set(SQL_ROLE_IDENTITY_TABLE_SELECT_BY_IDENTITY,
					"SELECT * FROM \"PUBLIC\".\"EFFECTIVE_ROLES\", \"PUBLIC\".\"ROLES\"  where \"PUBLIC\".\"EFFECTIVE_ROLES\".\"USER_NAME\" = '${principalName}' AND \"PUBLIC\".\"EFFECTIVE_ROLES\".\"ROLE_NAME\" = \"PUBLIC\".\"ROLES\".\"ROLE_NAME\";");
		}

		// http://www.bestsaphanatraining.com/how-to-manage-sap-hana-roles-and-privileges.html#a-hana-privilege-grant
		if (!configurationParameters.containsKey(SQL_ROLE_IDENTITY_GRANT_TO_ROLE)) {
			configurationParameters.set(SQL_ROLE_IDENTITY_GRANT_TO_ROLE,
					"GRANT ${rolePrincipalName} TO ${principalName};");
		}
		if (!configurationParameters.containsKey(SQL_ROLE_IDENTITY_REVOKE_FROM_ROLE)) {
			configurationParameters.set(SQL_ROLE_IDENTITY_REVOKE_FROM_ROLE,
					"REVOKE ${rolePrincipalName} FROM ${principalName};");
		}

	}

	public final static String SAP_SYSNR = "sap.instance";
	public final static String SAP_MULTI_TENANT = "sap.multiTenant";

	@Override
	public String getJDBCDriverName() {
		return "sap";
	}

	@Override
	public Integer getDefaultPort() {
		// https://archive.sap.com/discussions/thread/3764026
		return Integer.parseInt(String.format("3%02d%d", getInstance(), isMultiTenant() ? 13 : 15));
	}

	public int getInstance() {
		return getConfigurationParameters().getIntegerOrDefault(SAP_SYSNR, 0);
	}

	public boolean isMultiTenant() {
		return getConfigurationParameters().getBooleanOrDefault(SAP_MULTI_TENANT, false);
	}

	@Override
	public String getDriverClassName() {
		return "com.sap.db.jdbc.Driver";
	}

	@Override
	public String getJDBUrlProperties(boolean safe) {
		return null;
	}

	@Override
	public String getSql(String sql) {
		return replaceTokensDouble(super.getSql(sql));
	}

	public AuthMode getAuthMode() {
		return AuthMode.valueOf(configurationParameters.getStringOrDefault(AUTH_MODE, AuthMode.JDBC.name()));
	}

	public boolean isDropUserCascade() {
		return configurationParameters.getBooleanOrDefault(SAP_DROP_USER_CASCADE, true);
	}

	@Override
	public String getUpdatePasswordSQL(Identity identity, String encodedPassword, String password,
			boolean forcePasswordChangeAtLogon, PasswordResetType type) {
		// https://help.sap.com/viewer/4fe29514fd584807ac9f2a04f6754767/2.0.01/en-US/20d3459f75191014a7bbeb670bad8850.html
		return replaceToken(replaceToken(replaceTokensDouble(replaceTokens(
				configurationParameters.getStringOrDefault(SQL_PASSWORD_UPDATE,
						"ALTER USER ${principalName} PASSWORD \"${password}\" NO FORCE_FIRST_PASSWORD_CHANGE"),
				identity)), "${encodedPassword}", encodedPassword), "${password}", password);
	}

	@Override
	public String getSelectRolesSQL() {
		return replaceTokens(
				configurationParameters.getStringOrDefault(SQL_ROLE_TABLE_SELECT, "SELECT * FROM SYS.ROLES"));
	}

	// @Override
	// public String getIdentityLockedColumn() {
	// // TODO Auto-generated method stub
	// return super.getIdentityLockedColumn();
	// }
	//
	// @Override
	// public String getIdentityLockedValue() {
	// // TODO Auto-generated method stub
	// return super.getIdentityLockedValue();
	// }
	//
	// @Override
	// public String getIdentityUnlockedValue() {
	// // TODO Auto-generated method stub
	// return super.getIdentityUnlockedValue();
	// }

	@Override
	public String getDeleteSQL(String principalName) {
		return replaceToken(
				replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_DELETE,
						"DROP USER ${principalName}" + (isDropUserCascade() ? "" : " CASCADE"))),
				"${principalName}", principalName);
	}

	@Override
	public String getCreateSQL(Identity identity, String encodedPassword, String password) {
		// https://help.sap.com/viewer/4fe29514fd584807ac9f2a04f6754767/2.0.00/en-US/20d5ddb075191014b594f7b11ff08ee2.html
		// TODO there are many other types of authentication methods that may be
		// specified, not sure whether they should be implemented yet

		/*
		 * NOTE: The availabillity of FORCE_FIRST_PASSWORD_CHANGE indicates that
		 * we need to expand the I4J API a little to allow for this at creation
		 * time. For now, we will set SAP users as having
		 * NO_FORCE_FIRST_PASSWORD_CHANGE to match NAM behavior.
		 */
		String sql = replaceTokensDouble(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_CREATE,
				"CREATE user ${principalName} password \"#{password}\" NO FORCE_FIRST_PASSWORD_CHANGE"));
		sql = replaceTokens(sql, identity);
		sql = replaceTokenDouble(sql, "#{password}", password);
		return sql;
	}

	@Override
	public String getCreateRoleSQL(Role role) {
		String sql = replaceTokensDouble(
				configurationParameters.getStringOrDefault(SQL_ROLE_TABLE_CREATE, "CREATE ROLE ${principalName}"));
		sql = replaceTokens(sql, role);
		return sql;
	}

	@Override
	public String getDeleteRoleSQL(String principalName) {
		return replaceToken(replaceTokens(
				configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_DELETE, "DROP ROLE ${principalName}")),
				"${principalName}", principalName);
	}

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return SAPUsersConnector.class;
	}

    protected String getProcessedPrincipalName(Principal identity) {
        return identity.getPrincipalName().toUpperCase();
    }
}
