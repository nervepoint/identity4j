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

public interface MySqlUsersConstants {

	//Empty String
	String EMPTY_STRING = "";
	
	//GRANTS access key for attributes map in identity
	String USER_ACCESS = "userAccess";
	
	//MY SQL User Table Columns
	String USER_TABLE_USER_COLUMN = "User";
	String USER_TABLE_HOST_COLUMN = "Host";
	
	//REG EX values
	String GRANT_MATCHER = "(?i)GRANT\\s+";
	String PASSWORD_MATCHER = "\\s+IDENTIFIED BY PASSWORD '.*?'";
	
	//NEW LINE
	String NEW_LINE = "\r\n";
	
	//GRANT containing Password
	String IDENTIFIED_BY_PASSWORD = "IDENTIFIED BY PASSWORD";
	String _TO_ = " TO ";
	
}
