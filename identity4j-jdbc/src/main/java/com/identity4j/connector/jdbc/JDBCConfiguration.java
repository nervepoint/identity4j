package com.identity4j.connector.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
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
    public static final String SQL_IDENTITY_TABLE_ENABLED = "sqlIdentityTableEnabled";
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

    // Role Identity Table
    public static final String SQL_ROLE_IDENTITY_TABLE = "sqlRoleIdentityTable";
    public static final String SQL_ROLE_IDENTITY_TABLE_IDENTITY_GUID = "sqlRoleIdentityTableIdentityGuid";
    public static final String SQL_ROLE_IDENTITY_TABLE_ROLE_GUID = "sqlRoleIdentityTableRoleGuid";
    public static final String SQL_ROLE_IDENTITY_TABLE_SELECT_BY_IDENTITY = "sqlRoleIdentityTableSelectByIdentity";

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
        String content = configurationParameters.getStringOrFail(key);
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

    protected String replaceTokens(String str, Principal identity) {
        str = str.replace("${guid}", escapeString(identity.getGuid()));
        str = str.replace("${principalName}", escapeString(identity.getPrincipalName()));
        if (identity instanceof Identity) {
            Identity id = (Identity) identity;
            Map<String, String[]> attributes = id.getAttributes();
            if (attributes != null) {
                for (Map.Entry<String, String[]> en : attributes.entrySet()) {
                    str = replaceToken(str, "${" + en.getKey() + "}", escapeString(StringUtil.getValue(en.getValue())));
                }
            }
        }
        return replaceTokens(str);
    }

    protected String replaceToken(String str, String token, String value) {
        return str.replace(token, escapeString(value));
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

    protected String escapeString(String string) {
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
        buf.append("?");
        buf.append(getJDBUrlProperties(false));
        return buf.toString();
    }

    public String generateSafeJDBCUrl() {

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
        buf.append(getJDBUrlProperties(true));
        return buf.toString();
    }
    
    public abstract String getJDBUrlProperties(boolean safe);

    public abstract String getJDBCDriverName();

    public abstract Integer getDefaultPort();

    public abstract String getDriverClassName();

    public String getSql(String sql) {
        return replaceTokens(sql);
    }

    public String getSelectIdentitiesSQL() {
        return replaceTokens(
            configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_SELECT, "SELECT * FROM ${identityTable}"));
    }

    public String getUpdateSQL(Identity identity) {
        String sql = replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_UPDATE, ""));
        sql = replaceTokens(sql, identity);
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

    public String getDeleteSQL(String principalName) {
        return replaceToken(
            replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_DELETE,
                "DELETE FROM ${identityTable} WHERE ${identityTablePrincipalName} = '${principalName}'")),
            "${principalName}", principalName);
    }

    public String getSelectIdentitySQL(String principalName) {
        return replaceToken(
            replaceTokens(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_IDENTITY_SELECT,
                "SELECT * FROM ${identityTable} WHERE ${identityTablePrincipalName} = '${principalName}'")),
            "${principalName}", principalName);
    }

    public String getSelectIdentitiesRolesSQL(Identity identity) {
        return replaceTokens(configurationParameters.getStringOrDefault(SQL_ROLE_IDENTITY_TABLE_SELECT_BY_IDENTITY,
            "SELECT * FROM ${roleIdentityTable} WHERE ${roleIdentityTable.identityGuid} = ${guid}"), identity);
    }

    public String getRoleIdentityTable() {
        return configurationParameters.getString(SQL_ROLE_IDENTITY_TABLE);
    }

    public String getRoleTable() {
        return configurationParameters.getString(SQL_ROLE_TABLE);
    }

    public String getSelectRolesSQL() {
        return replaceTokens(configurationParameters.getStringOrDefault(SQL_ROLE_TABLE_SELECT, "SELECT * FROM ${roleTable}"));
    }

    public String getSelectPasswordSQL(Identity identity, String encodedPassword, String password) {
        return replaceToken(replaceToken(replaceTokens(configurationParameters.getString(SQL_PASSWORD_SELECT), identity),
            "${encodedPassword}", password), "${password}", password);
    }

    public String getUpdatePasswordSQL(Identity identity, String encodedPassword, String password) {
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

    public String getIdentityEnabledColumn() {
        return configurationParameters.getString(SQL_IDENTITY_TABLE_ENABLED);
    }

    public String getIdentityEnabledValue() {
        return configurationParameters.getString(SQL_IDENTITY_TABLE_ENABLED_VALUE);
    }

    public String getIdentityDisabledValue() {
        return configurationParameters.getString(SQL_IDENTITY_TABLE_DISABLED_VALUE);
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
