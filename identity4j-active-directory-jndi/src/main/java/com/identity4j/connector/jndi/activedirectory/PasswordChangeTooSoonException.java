package com.identity4j.connector.jndi.activedirectory;

import java.util.Date;

import org.apache.http.client.utils.DateUtils;

import com.identity4j.connector.exception.ConnectorException;

public class PasswordChangeTooSoonException extends ConnectorException {

	Date lastPasswordChange;
	public PasswordChangeTooSoonException(Date lastPasswordChange) {
		super("Password change not allowed due to minimum age policy. Last change was " + DateUtils.formatDate(lastPasswordChange, "yyyy-MM-dd HH:mm"));
		this.lastPasswordChange = lastPasswordChange;
	}

	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}
	
	private static final long serialVersionUID = 7540522250199953817L;

}
