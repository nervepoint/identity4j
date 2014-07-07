package com.identity4j.connector.mysql;

import com.identity4j.connector.jdbc.JDBCConfiguration;
import com.identity4j.util.MultiMap;

public class MySQLConfiguration extends JDBCConfiguration {

	public MySQLConfiguration(MultiMap configurationParameters) {
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
	public String getJDBUrlProperties() {
		StringBuilder buf = new StringBuilder();
		buf.append("user=");
		buf.append(configurationParameters.getString(JDBC_USERNAME));
		buf.append("&");
		buf.append("password=");
		buf.append(configurationParameters.getString(JDBC_PASSWORD));
		return buf.toString();
	}

}
