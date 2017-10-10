package com.identity4j.connector.mysql;

/*
 * #%L
 * Identity4J MYSQL
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

}
