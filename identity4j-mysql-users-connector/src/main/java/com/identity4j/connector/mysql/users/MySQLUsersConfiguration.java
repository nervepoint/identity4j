package com.identity4j.connector.mysql.users;

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
import com.identity4j.util.MultiMap;

public class MySQLUsersConfiguration extends JDBCConfiguration{
	
	private static final String AMPERSAND = "@";
	
	//Enable Disable feature enable
	public static final String IDENTITY_ENABLE_DISABLE_FEATURE = "identityEnableDisableFeature";
	public static final String IDENTITY_ENABLE_DISABLE_FLAG = "identityEnableDisableFlag";
	
	// Identity Table
	public static final String SQL_IDENTITIES_TABLE_SELECT = "sqlIdentitiesTableGrant";
	public static final String SQL_IDENTITY_TABLE_GRANT = "sqlIdentityTableGrant";
	public static final String SQL_IDENTITY_TABLE_REVOKE = "sqlIdentityTableRevoke";
	public static final String SQL_IDENTITY_TABLE_GRANT_SHOW = "sqlIdentityTableGrantShow";
	public static final String SQL_IDENTITY_TABLE_ENABLE_DISABLE = "sqlIdentityTableEnableDisable";
	
	//Password
	public static final String SQL_PASSWORD_SET = "sqlPasswordSet";
	
	//Flush Privileges
	public static final String SQL_FLUSH_PRIVILEGES = "sqlFlushPrivileges";

	public MySQLUsersConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
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
		if(safe) {
			buf.append("********");
		} else {
			buf.append(configurationParameters.getString(JDBC_PASSWORD));
		}
		return buf.toString();
	}
	
	public String getSelectIdentitySQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_SELECT,
				"SELECT * FROM mysql.user WHERE ( User = ? or User = ? ) and Host = ?");
	}
	
	@Override
	public String getSelectIdentitiesSQL() {
		return configurationParameters.getStringOrDefault(SQL_IDENTITIES_TABLE_SELECT,
				"SELECT * FROM mysql.user");
	}
	
	public String getGrantIdentitySQL(String access,String user,String host){
		return String.format(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_GRANT,
				"GRANT %s TO '%s'@'%s'"),access,user,host);
	}
	
	public String getRevokeIdentitySQL(String access,String user,String host){
		return String.format(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_REVOKE,
				"REVOKE %s FROM '%s'@'%s'"),access,user,host);
	}
	
	public String getGrantShowIdentitySQL(){
		return String.format(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_GRANT_SHOW,
				"SHOW GRANTS FOR ?@?"));
	}
	
	public String getCreateIdentitySQL(String user,String host,String password){
		return String.format(configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_CREATE,
				"CREATE USER '%s'@'%s' IDENTIFIED BY '%s'"),user,host,password);
	}
	
	public String getDeleteIdentitySQL(){
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_DELETE,
				"DROP USER ?@?");
	}
	
	public String getEnableDisableIdentitySQL(){
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_ENABLE_DISABLE,
				"UPDATE mysql.user set Host = ? where User = ? and Host = ?");
	}
	
	public String getFlushPrivilegesSQL(){
		return configurationParameters.getStringOrDefault(SQL_IDENTITY_TABLE_ENABLE_DISABLE,
				"FLUSH PRIVILEGES");
	}
	
	public String getSelectPasswordSQL() {
		return configurationParameters.getStringOrDefault(SQL_PASSWORD_SELECT,
				"SELECT * FROM mysql.user WHERE PASSWORD = PASSWORD(?) AND User = ? AND Host = ?");
	}

	public String getPasswordSetSQL() {
		return configurationParameters.getStringOrDefault(SQL_PASSWORD_SET,
				"SET PASSWORD FOR ?@? = PASSWORD(?)");
	}
	
	public boolean getIdentityEnableDisableFeature(){
		return Boolean.parseBoolean(configurationParameters.getStringOrDefault(IDENTITY_ENABLE_DISABLE_FEATURE,"false"));
	}

	public String getDisableFlag() {
		return configurationParameters.getStringOrDefault(IDENTITY_ENABLE_DISABLE_FLAG,"dis!");
	}
	
	public String getDisabledIdentityPrincipalName(String user,String host){
		return user + AMPERSAND + getDisableFlag() + host;
	}
	
	public String getEnabledIdentityPrincipalName(String user,String host){
		return user + AMPERSAND + host;
	}

}
