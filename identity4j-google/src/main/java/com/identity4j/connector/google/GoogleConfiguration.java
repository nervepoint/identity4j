package com.identity4j.connector.google;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.util.MultiMap;

/**
 * Configuration class provides access to properties configured
 * 
 * @author gaurav
 *
 */
public class GoogleConfiguration extends AbstractConnectorConfiguration{
	
	public static final String GOOGLE_USERNAME = "googleUsername";
	public static final String GOOGLE_SERVICE_ACCOUNT_ID = "googleServiceAccountId";
	public static final String GOOGLE_PRIVATE_KEY_ENCODED = "googlePrivateKeyEncoded";
	public static final String GOOGLE_CUSTOMER_ID = "googleCustomerId";
	public static final String GOOGLE_CUSTOMER_DOMAIN ="googleCustomerDomain";
	public static final String GOOGLE_FETCH_ROLES ="googleFetchRoles";
	public static final String GOOGLE_FETCH_DELAY ="googleRequestInterval";
	
	public GoogleConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	@Override
	public String getUsernameHint() {
		return null;
	}

	@Override
	public String getHostnameHint() {
		return null;
	}
	
	/**
	 * Google apps user name on whose behalf actions will be taken 
	 */
	public String getGoogleUsername(){
		return configurationParameters.getString(GOOGLE_USERNAME);
	}
	
	/**
	 * Google apps service id who has been given consent to perform operations
	 * on behalf of admin user. 
	 */
	public String getGoogleServiceAccountId(){
		return configurationParameters.getString(GOOGLE_SERVICE_ACCOUNT_ID);
	}
	
	/**
	 * Service id private key encoded as base 64 
	 */
	public String getGooglePrivateKeyEncoded(){
		return configurationParameters.getString(GOOGLE_PRIVATE_KEY_ENCODED);
	}
	
	/**
	 * Google apps customer id. 
	 */
	public String getGoogleCustomerId(){
		return configurationParameters.getString(GOOGLE_CUSTOMER_ID);
	}
	
	/**
	 * Google apps customer domain 
	 */
	public String getGoogleCustomerDomain(){
		return configurationParameters.getString(GOOGLE_CUSTOMER_DOMAIN);
	}

	public boolean getFetchRoles() {
		return configurationParameters.getBooleanOrDefault(GOOGLE_FETCH_ROLES, false);
	}

	public Integer getRequestInterval() {
		return configurationParameters.getIntegerOrDefault(GOOGLE_FETCH_DELAY, 50);
	}
}
