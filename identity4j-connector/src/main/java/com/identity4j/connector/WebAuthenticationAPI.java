package com.identity4j.connector;

import java.io.IOException;
import java.util.Map;

public interface WebAuthenticationAPI<T extends ConnectorConfigurationParameters> {
	public enum Status {
		STARTED, OPENED, COMPLETE 
	}
	
	public enum ReturnStatus {
		UNKNOWN, AUTHENTICATED, FAILED_TO_AUTHENTICATE 
	}
	String getUsername();
	
	String getId();
	
	String open(T parameters, String returnTo);
	
	String getState();
	
	Status getStatus();
	
	ReturnStatus validate(Map<String, String[]> returnParameters) throws IOException;
}
