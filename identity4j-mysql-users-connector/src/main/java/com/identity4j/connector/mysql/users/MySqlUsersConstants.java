package com.identity4j.connector.mysql.users;

public interface MySqlUsersConstants {

	//Empty String
	String EMPTY_STRING = "";
	
	//GRANTS access key for attributes map in identity
	String USER_ACCESS = "USER_ACCESS";
	
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
