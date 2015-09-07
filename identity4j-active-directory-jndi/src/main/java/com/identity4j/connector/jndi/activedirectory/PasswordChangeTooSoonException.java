package com.identity4j.connector.jndi.activedirectory;

import java.util.Date;

import com.identity4j.connector.exception.ConnectorException;

public class PasswordChangeTooSoonException extends ConnectorException {

	Date lastPasswordChange;
	public PasswordChangeTooSoonException(Date lastPasswordChange) {
		this.lastPasswordChange = lastPasswordChange;
	}

	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}
	
	private static final long serialVersionUID = 7540522250199953817L;

}
