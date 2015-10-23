package com.identity4j.connector;

public interface OAuth2<T extends ConnectorConfigurationParameters> extends WebAuthenticationAPI<T> {
	
	String getAuthorizeUrl();
}
