package com.identity4j.connector;

import java.io.IOException;
import java.util.Map;

public interface WebAuthenticationAPI {
	public enum Status {
		STARTED, OPENED, COMPLETE 
	}
	
	public enum ReturnStatus {
		UNKNOWN, AUTHENTICATED, FAILED_TO_AUTHENTICATE 
	}
	String getUsername();
	
	String getId();
	
	String open(String returnTo);
	
	String getState();
	
	long getCreated();
	
	Status getStatus();
	
	ReturnStatus validate(Map<String, String[]> returnParameters) throws IOException;
}
