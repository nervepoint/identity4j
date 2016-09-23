/* HEADER */
package com.identity4j.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import com.identity4j.util.MultiMap;

/**
 */
public interface ConnectorConfigurationParameters {
	
	InputStream getAdditionalIdentityAttributes() throws IOException;

	ResourceBundle getAdditionalResources(Locale locale) throws IOException;

	String getUsernameHint();

	String getHostnameHint();

	MultiMap getConfigurationParameters();

	Collection<String> getIdentityAttributesToRetrieve();

	void setIdentityAttributesToRetrieve(Collection<String> identityAttributesToRetrieve);

    String getProvisionAttributeForPrincipalName();

}