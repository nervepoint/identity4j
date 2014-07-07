package com.identity4j.connector.jdbc;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.principal.Identity;
import com.identity4j.util.MultiMap;

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
	public static final String SQL_IDENTITY_TABLE_DELETE = "sqlIdentityTableDelete";
	public static final String SQL_IDENTITY_TABLE_CREATE = "sqlIdentityTableCreate";
	
	// Identity Columns
	public static final String SQL_IDENTITY_TABLE_GUID = "sqlIdentityTableGuid";
	public static final String SQL_IDENTITY_TABLE_PRINCIPAL_NAME = "sqlIdentityTablePrincipalName";
	public static final String SQL_IDENTITY_TABLE_FULLNAME = "sqlIdentityTableFullname";
	public static final String SQL_IDENTITY_TABLE_OTHER_NAME = "sqlIdentityTableOtherName";
	public static final String SQL_IDENTITY_TABLE_EMAIL = "sqlIdentityTableEmail";
	public static final String SQL_IDENTITY_TABLE_MOBILE = "sqlIdentityTableMobile";
	public static final String SQL_IDENTITY_TABLE_LAST_SIGNON = "sqlIdentityTableLastSignon";
	
	// Password
	public static final String SQL_IDENTITY_TABLE_PASSWORD = "sqlIdentityTablePassword";
	public static final String SQL_IDENTITY_PASSWORD_ENCODING = "passwordEncoding";
	public static final String SQL_PASSWORD_SELECT = "sqlPasswordSelect";
	
	// Role Table
	public static final String SQL_ROLE_TABLE = "sqlRoleTable";
	public static final String SQL_ROLE_TABLE_SELECT = "sqlRoleTableSelect";
	public static final String SQL_ROLE_TABLE_SELECT_BY_IDENTITY = "sqlRoleTableSelectByIdentity";
	
	// Role Columns
	public static final String SQL_ROLE_TABLE_IDENTITY_GUID = "sqlRoleTableIdentityGuid";
	public static final String SQL_ROLE_TABLE_GUID = "sqlRoleTableGuid";
	public static final String SQL_ROLE_TABLE_PRINCIPAL_NAME = "sqlRoleTablePrincipalName";
	
	public JDBCConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	public String getUsernameHint() {
		return null;
	}

	public String getHostnameHint() {
		return null;
	}
	
	protected String replaceTokens(String str, Identity identity) {
		str = str.replace("${identityGuidValue}", identity.getGuid());
		return replaceTokens(str);
	}
	
	protected String replaceToken(String str, String token, String value) {
		return str.replace(token, value);
	}
	
	protected String replaceTokens(String str) {
		
		str = str.replace("${identityTable}", configurationParameters.getString(SQL_IDENTITY_TABLE));
		str = str.replace("${roleTable}", configurationParameters.getString(SQL_ROLE_TABLE));
		str = str.replace("${roleIdentityGuid}", configurationParameters.getString(SQL_ROLE_TABLE_IDENTITY_GUID));
		return str;
	}
	
	/**
	 * Override this if you need to generate a different JDBC URL format. 
	 * 
	 * The default format is jdbc://<hostname>:<port>/<database>?<properties>
	 * 
	 * Where <properties> is the return value of {@link getJDBCUrlProperties}
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
		buf.append("?");
		buf.append(getJDBUrlProperties());
		return buf.toString();
	}
	
	public abstract String getJDBUrlProperties();

	public abstract String getJDBCDriverName();
	
	public abstract Integer getDefaultPort();

	public abstract String getDriverClassName();

	public String getSelectIdentitiesSQL() {
		return replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_SELECT,
				"SELECT * FROM ${identityTable}"));
	}

	public String getSelectIdentitiesRolesSQL(Identity identity) {
		return replaceTokens(configurationParameters.getStringOrDefault(SQL_ROLE_TABLE_SELECT_BY_IDENTITY,
				"SELECT * FROM ${roleTable} WHERE ${roleIdentityGuid} = ${identityGuidValue}"), identity);
	}


	public String getSelectRolesSQL() {
		return replaceTokens(configurationParameters.getStringOrDefault(SQL_ROLE_TABLE_SELECT,
				"SELECT * FROM ${roleTable}"));
	}
	
	public String getSelectPasswordSQL(Identity identity, String password) {
		return replaceToken(replaceTokens(configurationParameters.getString(SQL_PASSWORD_SELECT), identity),
				"${password}", password);
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
