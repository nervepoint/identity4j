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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.Connector.PasswordResetType;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.IOUtil;
import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

public abstract class JDBCConfiguration extends AbstractConnectorConfiguration {

	public static final String CHARSET = "sqlCharset";

	public static final String JDBC_HOSTNAME = "jdbcHostname";
	public static final String JDBC_PORT = "jdbcPort";
	public static final String JDBC_USERNAME = "jdbcUsername";
	public static final String JDBC_PASSWORD = "jdbcPassword";
	public static final String JDBC_DATABASE = "jdbcDatabase";

	// Identity Table
	public static final String SQL_IDENTITY_TABLE = "sqlIdentityTable";
	public static final String SQL_IDENTITY_TABLE_SELECT = "sqlIdentityTableSelect";
	public static final String SQL_IDENTITY_TABLE_IDENTITY_SELECT = "sqlIdentityTableIdentitySelect";
	public static final String SQL_IDENTITY_TABLE_DELETE = "sqlIdentityTableDelete";
	public static final String SQL_IDENTITY_TABLE_CREATE = "sqlIdentityTableCreate";
	public static final String SQL_IDENTITY_TABLE_UPDATE = "sqlIdentityTableUpdate";

	// Identity Columns
	public static final String SQL_IDENTITY_TABLE_GUID = "sqlIdentityTableGuid";
	public static final String SQL_IDENTITY_TABLE_PRINCIPAL_NAME = "sqlIdentityTablePrincipalName";
	public static final String SQL_IDENTITY_TABLE_FULLNAME = "sqlIdentityTableFullname";
	public static final String SQL_IDENTITY_TABLE_OTHER_NAME = "sqlIdentityTableOtherName";
	public static final String SQL_IDENTITY_TABLE_EMAIL = "sqlIdentityTableEmail";
	public static final String SQL_IDENTITY_TABLE_MOBILE = "sqlIdentityTableMobile";
	public static final String SQL_IDENTITY_TABLE_LAST_SIGNON = "sqlIdentityTableLastSignon";
	public static final String SQL_IDENTITY_TABLE_LAST_PASSWORD_CHANGE = "sqlIdentityTableLastPasswwordChange";
	public static final String SQL_IDENTITY_TABLE_ENABLED = "sqlIdentityTableEnabled";
	public static final String SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE = "sqlIdentityTableForcePasswordChange";
	public static final String SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE_VALUE = "sqlIdentityTableForcePasswordValue";
	public static final String SQL_IDENTITY_TABLE_NO_FORCE_PASSWORD_CHANGE_VALUE = "sqlIdentityTableNoForcePasswordValue";
	public static final String SQL_IDENTITY_TABLE_ENABLED_VALUE = "sqlIdentityTableEnabledValue";
	public static final String SQL_IDENTITY_TABLE_DISABLED_VALUE = "sqlIdentityTableDisableValue";
	public static final String SQL_IDENTITY_TABLE_LOCKED = "sqlIdentityTableLocked";
	public static final String SQL_IDENTITY_TABLE_LOCKED_VALUE = "sqlIdentityTableLockedValue";
	public static final String SQL_IDENTITY_TABLE_UNLOCKED_VALUE = "sqlIdentityTableUnlockedValue";

	// Password
	public static final String SQL_IDENTITY_TABLE_PASSWORD = "sqlIdentityTablePassword";
	public static final String SQL_IDENTITY_PASSWORD_ENCODING = "passwordEncoding";
	public static final String SQL_PASSWORD_SELECT = "sqlPasswordSelect";
	public static final String SQL_PASSWORD_UPDATE = "sqlPasswordUpdate";

	// Role Table
	public static final String SQL_ROLE_TABLE = "sqlRoleTable";
	public static final String SQL_ROLE_TABLE_SELECT = "sqlRoleTableSelect";
	public static final String SQL_ROLE_TABLE_GUID = "sqlRoleTableGuid";
	public static final String SQL_ROLE_TABLE_PRINCIPAL_NAME = "sqlRoleTablePrincipalName";
	public static final String SQL_ROLE_TABLE_DELETE = "sqlRoleTableDelete";
	public static final String SQL_ROLE_TABLE_CREATE = "sqlRoleTableCreate";
	public static final String SQL_ROLE_TABLE_UPDATE = "sqlRoleTableUpdate";

	// Role Identity Table
	public static final String SQL_ROLE_IDENTITY_TABLE = "sqlRoleIdentityTable";
	public static final String SQL_ROLE_IDENTITY_TABLE_IDENTITY_GUID = "sqlRoleIdentityTableIdentityGuid";
	public static final String SQL_ROLE_IDENTITY_TABLE_ROLE_GUID = "sqlRoleIdentityTableRoleGuid";
	public static final String SQL_ROLE_IDENTITY_TABLE_SELECT_BY_IDENTITY = "sqlRoleIdentityTableSelectByIdentity";
	public static final String SQL_ROLE_IDENTITY_REVOKE_FROM_ROLE = "sqlRoleIdentityTableRevokeFromRole";
	public static final String SQL_ROLE_IDENTITY_GRANT_TO_ROLE = "sqlRoleIdentityTableGrantToRole";

	// Attributes
	public static final String IDENTITY_ATTRIBUTE_FIELDS = "identityAttributeFields";
	public static final String IDENTITY_ATTRIBUTE_I18N = "identityAttributeI18n";

	public JDBCConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	@Override
	public InputStream getAdditionalIdentityAttributes() throws IOException {
		String attributeFieldsContent = getAttributeFieldsContent();
		return StringUtil.isNullOrEmpty(attributeFieldsContent) ? null
				: new ByteArrayInputStream(attributeFieldsContent.getBytes());
	}

	@Override
	public ResourceBundle getAdditionalResources(Locale locale) throws IOException {
		String i18nContent = getI18nContent();
		return StringUtil.isNullOrEmpty(i18nContent) ? null
				: new AttributeResourceBundle(new ByteArrayInputStream(i18nContent.getBytes()));
	}

	/**
	 * Get the identity attributes I18n content.
	 * 
	 * @return identity attributes I18n content
	 */
	public String getI18nContent() throws IOException {
		return getContent(IDENTITY_ATTRIBUTE_I18N);
	}

	protected String getContent(String key) throws IOException {
		String content = configurationParameters.getString(key);
		if (content.startsWith("// res://")) {
			return IOUtil.getStringFromResource(this.getClass(), content.substring(3));
		}
		return content;
	}

	public String getUsernameHint() {
		return null;
	}

	public String getHostnameHint() {
		return null;
	}

	public String replaceTokens(String str, Principal identity) {
		str = str.replace("${guid}", escapeString(identity.getGuid()));
		str = str.replace("${principalName}", escapeString(getProcessedPrincipalName(identity)));
		str = str.replace("#{guid}", escapeStringDouble(identity.getGuid()));
		str = str.replace("#{principalName}", escapeStringDouble(getProcessedPrincipalName(identity)));
		str = replaceAttributeTokens(str, identity);
		return replaceTokensDouble(replaceTokens(str));
	}

    protected String getProcessedPrincipalName(Principal identity) {
        return identity.getPrincipalName();
    }

	public String replaceAttributeTokens(String str, Principal identity) {
		Map<String, String[]> attributes = identity.getAttributes();
		if (attributes != null) {
			for (Map.Entry<String, String[]> en : attributes.entrySet()) {
				str = replaceToken(str, "${" + en.getKey() + "}", escapeString(StringUtil.getValue(en.getValue())));
				str = replaceToken(str, "#{" + en.getKey() + "}",
						escapeStringDouble(StringUtil.getValue(en.getValue())));
			}
		}
		return str;
	}

	public String replaceRoleTokens(String str, Principal identity) {
		str = str.replace("${roleGuid}", escapeString(identity.getGuid()));
		str = str.replace("${rolePrincipalName}", escapeString(getProcessedPrincipalName(identity)));
		str = str.replace("#{roleGuid}", escapeStringDouble(identity.getGuid()));
		str = str.replace("#{rolePrincipalName}", escapeStringDouble(getProcessedPrincipalName(identity)));
		return replaceTokensDouble(replaceTokens(str));
	}

	protected String replaceToken(String str, String token, String value) {
		return str.replace(token, escapeString(value));
	}

	protected String replaceTokenDouble(String str, String token, String value) {
		return str.replace(token, escapeStringDouble(value));
	}

	protected String replaceTokensDouble(String str) {

		/*
		 * "System object names like schema names, table names and field names
		 * should be wrapped in double quotes “ ” whereas as you can see below,
		 * data values that have character data types inserted MUST always be
		 * wrapped in single quotes ‘ ’ whereas number do not require any quotes
		 * at all."
		 * 
		 * http://teachmehana.com/insert-statement-sql-script-sap-hana/
		 * 
		 * So this does the same as JDBConfiguration.replaceTokens() but instead
		 * escapes double quotes. Ideally this should be handled in the abstract
		 * (note that for this to happen it must be capable of having both types
		 * of escaping in a single SQL statement)
		 */
		for (String key : configurationParameters.keySet()) {
			String akey = key;
			if (akey.startsWith("sql")) {
				akey = akey.substring(3);
			}
			akey = Character.toLowerCase(akey.charAt(0)) + akey.substring(1);
			str = str.replace("#{" + akey + "}", escapeStringDouble(configurationParameters.getString(key)));
		}

		str = str.replace("#{roleIdentityTable.identityGuid}",
				escapeStringDouble(configurationParameters.getString(SQL_ROLE_IDENTITY_TABLE_IDENTITY_GUID)));
		str = str.replace("#{roleIdentityTable.roleGuid}",
				escapeStringDouble(configurationParameters.getString(SQL_ROLE_IDENTITY_TABLE_ROLE_GUID)));
		return str;
	}

	protected String replaceTokens(String str) {

		/*
		 * Replace all of the connector attributes allowing SQL fragments to use
		 * the column / table names.
		 * 
		 * Note, most actual attribute name start with 'sql', this is stripped.
		 */
		for (String key : configurationParameters.keySet()) {
			String akey = key;
			if (akey.startsWith("sql")) {
				akey = akey.substring(3);
			}
			akey = Character.toLowerCase(akey.charAt(0)) + akey.substring(1);
			str = str.replace("${" + akey + "}", escapeString(configurationParameters.getString(key)));
		}

		str = str.replace("${roleIdentityTable.identityGuid}",
				escapeString(configurationParameters.getString(SQL_ROLE_IDENTITY_TABLE_IDENTITY_GUID)));
		str = str.replace("${roleIdentityTable.roleGuid}",
				escapeString(configurationParameters.getString(SQL_ROLE_IDENTITY_TABLE_ROLE_GUID)));
		return str;
	}

	public static String escapeStringDouble(String string) {
		return string == null ? "" : string.replace("\"", "\"\"");
	}

	public static String escapeString(String string) {
		return string == null ? "" : string.replace("'", "''");
	}

	/**
	 * Get the identity attributes content.
	 * 
	 * @return identity attributes content
	 */
	public String getAttributeFieldsContent() throws IOException {
		return getContent(IDENTITY_ATTRIBUTE_FIELDS);
	}

	/**
	 * Override this if you need to generate a different JDBC URL format.
	 * 
	 * The default format is jdbc://<hostname>:<port>/<database>?<properties>
	 * 
	 * Where <properties> is the return value of {@link getJDBCUrlProperties}
	 * 
	 * @return
	 */
	public String generateJDBCUrl() {

		StringBuilder buf = new StringBuilder();
		buf.append("jdbc:");
		buf.append(getJDBCDriverName());
		buf.append("://");
		buf.append(configurationParameters.getString(JDBC_HOSTNAME));
		buf.append(":");
		buf.append(configurationParameters.getIntegerOrDefault(JDBC_PORT, getDefaultPort()));
		buf.append("/");
		buf.append(configurationParameters.getString(JDBC_DATABASE));
		String jdbUrlProperties = getJDBUrlProperties(false);
		if (!StringUtil.isNullOrEmpty(jdbUrlProperties)) {
			buf.append("?");
			buf.append(jdbUrlProperties);
		}
        return buf.toString();
	}

	public abstract String getJDBUrlProperties(boolean safe);

	public abstract String getJDBCDriverName();

	public abstract Integer getDefaultPort();

	public abstract String getDriverClassName();

	public String getJDBCUsername() {
		return configurationParameters.getStringOrNull(JDBC_USERNAME);
	}

	public char[] getJDBCPassword() {
		String pw = configurationParameters.getStringOrNull(JDBC_PASSWORD);
		return pw == null ? null : pw.toCharArray();
	}

	public String getSql(String sql) {
		return replaceTokens(sql);
	}

	public String getSelectIdentitiesSQL() {
		return replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_SELECT,
				"SELECT * FROM ${identityTable}"));
	}

	public String getUpdateSQL(Identity identity) {
		String sql = replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_UPDATE, ""));
		sql = replaceTokens(sql, identity);
		return sql;
	}

	public String getRevokeFromRoleSQL(Identity identity, Role role) {
		String sql = replaceTokens(configurationParameters.getStringOrDefault(SQL_ROLE_IDENTITY_REVOKE_FROM_ROLE, ""));
		sql = replaceTokens(sql, identity);
		sql = replaceRoleTokens(sql, role);
		return sql;
	}

	public String getGrantToRoleSQL(Identity identity, Role role) {
		String sql = replaceTokens(configurationParameters.getStringOrDefault(SQL_ROLE_IDENTITY_GRANT_TO_ROLE, ""));
		sql = replaceTokens(sql, identity);
		sql = replaceRoleTokens(sql, role);
		return sql;
	}

	public String getCreateSQL(Identity identity, String encodedPassword, String password) {
		String sql = replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_CREATE,
				"INSERT INTO ${identityTable} (${identityTablePrincipalName},${identityTablePassword}) VALUES ('${principalName}','${encodedPassword}')"));
		sql = replaceTokens(sql, identity);
		sql = replaceToken(sql, "${encodedPassword}", encodedPassword);
		sql = replaceToken(sql, "${password}", password);
		return sql;
	}

	public String getCreateRoleSQL(Role role) {
		String sql = replaceTokens(configurationParameters.getStringOrDefault(SQL_ROLE_TABLE_CREATE,
				"INSERT INTO ${roleTable} (${roleTablePrincipalName}) VALUES ('${principalName}')"));
		sql = replaceTokens(sql, role);
		return sql;
	}

	public String getDeleteSQL(String principalName) {
		return replaceToken(
				replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_DELETE,
						"DELETE FROM ${identityTable} WHERE ${identityTablePrincipalName} = '${principalName}'")),
				"${principalName}", principalName);
	}

	public String getDeleteRoleSQL(String principalName) {
		return replaceToken(
				replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_DELETE,
						"DELETE FROM ${roleTable} WHERE ${roleTablePrincipalName} = '${principalName}'")),
				"${principalName}", principalName);
	}

	public String getSelectIdentitySQL(String principalName) {
		return replaceToken(
				replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_IDENTITY_SELECT,
						"SELECT * FROM ${identityTable} WHERE ${identityTablePrincipalName} = '${principalName}'")),
				"${principalName}", principalName);
	}

	public String getSelectIdentitiesRolesSQL(Identity identity) {
		String str = configurationParameters.getString(SQL_ROLE_IDENTITY_TABLE_SELECT_BY_IDENTITY);
		if (StringUtil.isNullOrEmpty(str)) {
			if (getRoleTable().length() == 0 || getRoleIdentityTable().length() == 0)
				return "";
			str = "SELECT * FROM ${roleIdentityTable} WHERE ${roleIdentityTable.identityGuid} = ${guid}";
		}
		return replaceTokens(
				configurationParameters.getStringOrDefault(SQL_ROLE_IDENTITY_TABLE_SELECT_BY_IDENTITY, str), identity);
	}

	public String getRoleIdentityTable() {
		return configurationParameters.getString(SQL_ROLE_IDENTITY_TABLE);
	}

	public String getRoleTable() {
		return configurationParameters.getString(SQL_ROLE_TABLE);
	}

	public String getSelectRolesSQL() {
		return replaceTokens(
				configurationParameters.getStringOrDefault(SQL_ROLE_TABLE_SELECT, "SELECT * FROM ${roleTable}"));
	}

	public String getSelectPasswordSQL(Identity identity, String encodedPassword, String password) {
		return replaceToken(
				replaceToken(replaceTokens(configurationParameters.getString(SQL_PASSWORD_SELECT), identity),
						"${encodedPassword}", password),
				"${password}", password);
	}

	public String getUpdatePasswordSQL(Identity identity, String encodedPassword, String password,
			boolean forcePasswordChangeAtLogon, PasswordResetType type) {
		return replaceToken(replaceToken(replaceTokens(
				configurationParameters.getStringOrDefault(SQL_PASSWORD_UPDATE,
						"UPDATE ${identityTable}\n SET ${identityTablePassword} = '${encodedPassword}'\n  WHERE ${identityTablePrincipalName} = '${principalName}'"),
				identity), "${encodedPassword}", encodedPassword), "${password}", password);
	}

	public String getIdentityGuidColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_GUID);
	}

	public String getIdentityPrincipalNameColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_PRINCIPAL_NAME);
	}

	public String getIdentityEmailColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_EMAIL);
	}

	public String getIdentityMobileColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_MOBILE);
	}

	public String getIdentityFullnameColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_FULLNAME);
	}

	public String getIdentityOtherNameColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_OTHER_NAME);
	}

	public String getIdentityLastSignOnColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_LAST_SIGNON);
	}

	public String getIdentityLastPasswordChangeColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_LAST_PASSWORD_CHANGE);
	}

	public String getIdentityEnabledColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_ENABLED);
	}

	public String getIdentityEnabledValue() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_ENABLED_VALUE);
	}

	public String getIdentityDisabledValue() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_DISABLED_VALUE);
	}

	public String getIdentityForcePasswordChangeColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE);
	}

	public String getIdentityNoForcePasswordChangeValue() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_NO_FORCE_PASSWORD_CHANGE_VALUE);
	}

	public String getIdentityForcePasswordChangeValue() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_FORCE_PASSWORD_CHANGE_VALUE);
	}

	public String getIdentityLockedColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_LOCKED);
	}

	public String getIdentityLockedValue() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_LOCKED_VALUE);
	}

	public String getIdentityUnlockedValue() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_UNLOCKED_VALUE);
	}

	public String getIdentityPasswordColumn() {
		return configurationParameters.getString(SQL_IDENTITY_TABLE_PASSWORD);
	}

	public String getIdentityPasswordEncoding() {
		return configurationParameters.getString(SQL_IDENTITY_PASSWORD_ENCODING);
	}

	public String getRoleGuidColumn() {
		return configurationParameters.getString(SQL_ROLE_TABLE_GUID);
	}

	public String getRolePrincipalNameColumn() {
		return configurationParameters.getString(SQL_ROLE_TABLE_PRINCIPAL_NAME);
	}

	public String getCharset() {
		return configurationParameters.getStringOrDefault(CHARSET, "UTF-8");
	}

}
