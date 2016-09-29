package com.identity4j.connector.jndi.activedirectory;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.identity4j.connector.exception.ConnectorException;

public class PasswordChangeTooSoonException extends ConnectorException {

	Date lastPasswordChange;
	public PasswordChangeTooSoonException(Date lastPasswordChange) {
		super("Password change not allowed due to minimum age policy. Last change was " +  new SimpleDateFormat("yyyy-MM-dd HH:mm").format(lastPasswordChange));
		this.lastPasswordChange = lastPasswordChange;
	}

	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}
	
	private static final long serialVersionUID = 7540522250199953817L;

}
