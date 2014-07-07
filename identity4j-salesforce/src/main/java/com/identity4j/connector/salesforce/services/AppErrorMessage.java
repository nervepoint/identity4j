package com.identity4j.connector.salesforce.services;

import java.util.Arrays;


/**
 * Class encapsulates error messages sent by Active Directory Graph API. The
 * JSON error object is mapped to this class.
 * 
 * @author gaurav
 * 
 */
public class AppErrorMessage {

	public String errorCode;
	public String message;
	public String[] fields;
	
	@Override
	public String toString() {
		return "AppErrorMessage [errorCode=" + errorCode + ", message="
				+ message + ", fields=" + Arrays.toString(fields) + "]";
	}
	
	
}
