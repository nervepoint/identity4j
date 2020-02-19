package com.identity4j.connector.google;

/*
 * #%L
 * Identity4J GOOGLE
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

import java.util.Arrays;
import java.util.Collection;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.Connector;
import com.identity4j.util.MultiMap;

/**
 * Configuration class provides access to properties configured
 * 
 * @author gaurav
 *
 */
public class GoogleConfiguration extends AbstractConnectorConfiguration{

	public static final String GOOGLE_OAUTH_CLIENT_ID = "googleOAuthClientID";
	public static final String GOOGLE_OAUTH_CLIENT_SECRET = "googleOAuthClientSecret";
	public static final String GOOGLE_USERNAME = "googleUsername";
	public static final String GOOGLE_SERVICE_ACCOUNT_JSON = "googleServiceAccountJson";
	public static final String GOOGLE_SERVICE_ACCOUNT_ID = "googleServiceAccountId";
	public static final String GOOGLE_PRIVATE_KEY_ENCODED = "googlePrivateKeyEncoded";
	public static final String GOOGLE_PRIVATE_KEY_PASSPHRASE = "googlePrivateKeyPassphrase";
	public static final String GOOGLE_CUSTOMER_ID = "googleCustomerId";
	public static final String GOOGLE_CUSTOMER_DOMAIN ="googleCustomerDomain";
	public static final String GOOGLE_FETCH_ROLES ="googleFetchRoles";
	public static final String GOOGLE_FETCH_DELAY ="googleRequestInterval";
	public static final String GOOGLE_INCLUDE_ORGUNITS ="googleIncludeOrgunits";
	public static final String GOOGLE_EXCLUDE_ORGUNITS ="googleExcludeOrgunits";
	
	
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
	 * Instead of using {@link #getGoogleServiceAccountId()}, {@link #getGooglePrivateKeyEncoded()}
	 * and {@link #getGoogleUsername()}, the JSON file generated from Google API console should
	 * now be used.
	 */
	public String getGoogleServiceAccountJson(){
		return configurationParameters.getString(GOOGLE_SERVICE_ACCOUNT_JSON);
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
	 * Service id private key passphrase 
	 */
	public String getGooglePrivatePassphrase(){
		return configurationParameters.getStringOrDefault(GOOGLE_PRIVATE_KEY_PASSPHRASE, "notasecret");
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
	
	/**
	 * Google OAuth client ID 
	 */
	public String getGoogleOAuthClientId(){
		return configurationParameters.getString(GOOGLE_OAUTH_CLIENT_ID);
	}
	
	/**
	 * Google OAuth client secret 
	 */
	public String getGoogleOAuthClientSecret(){
		return configurationParameters.getString(GOOGLE_OAUTH_CLIENT_SECRET);
	}

	public boolean getFetchRoles() {
		return configurationParameters.getBooleanOrDefault(GOOGLE_FETCH_ROLES, false);
	}

	public Integer getRequestInterval() {
		return configurationParameters.getIntegerOrDefault(GOOGLE_FETCH_DELAY, 50);
	}

	/**
	 * Get a list of Orgunits to include the search. If not specified, all orgunits are
	 * included..
	 * 
	 * @return orgunits to exclude
	 */
	public Collection<String> getIncludes() {
		String[] includes = configurationParameters.getStringArrayOrDefault(GOOGLE_INCLUDE_ORGUNITS, new String[0]);
		return Arrays.asList(includes.length == 1 && includes[0].equals("") ? new String[0] : includes);
	}
	
	/**
	 * Get a list of Orgunits to exclude from the search. If not specified, no orgunits are
	 * exluded.
	 * 
	 * @return orgunits to exclude
	 */
	public Collection<String> getExcludes() {
		String[] excludes = configurationParameters.getStringArrayOrDefault(GOOGLE_EXCLUDE_ORGUNITS, new String[0]);
		return Arrays.asList(excludes.length == 1 && excludes[0].equals("") ? new String[0] : excludes);
	}
	
	/**
	 * Set a list of Orgunits to include the search. If not specified, all orgunits are
	 * included.
	 * 
	 * @param include orgunits to include
	 */
	public void setIncludes(Collection<String> includes) {
		configurationParameters.put(GOOGLE_INCLUDE_ORGUNITS, includes.toArray(new String[0]));
	}
	

	
	/**
	 * Set a list of Orgunits to exclude from the search. If not specified, no orgunits are
	 * excluded.
	 * 
	 * @param exclude orgunits to exclude
	 */
	public void setExcludes(Collection<String> excludes) {
		configurationParameters.put(GOOGLE_INCLUDE_ORGUNITS, excludes.toArray(new String[0]));
	}

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return GoogleConnector.class;
	}
}
