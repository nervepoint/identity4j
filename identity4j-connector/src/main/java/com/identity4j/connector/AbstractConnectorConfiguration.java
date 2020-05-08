/* HEADER */
package com.identity4j.connector;

/*
 * #%L
 * Identity4J Connector
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


import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import com.identity4j.util.MultiMap;

public abstract class AbstractConnectorConfiguration implements ConnectorConfigurationParameters {
	public static final String KEY_URI = "uri";

	protected final MultiMap configurationParameters;

	private Collection<String> identityAttributesToRetrieve;

	@Override
	public InputStream getAdditionalIdentityAttributes() throws IOException {
		return null;
	}

	@Override
	public ResourceBundle getAdditionalResources(Locale locale) throws IOException {
		return null;
	}

	public AbstractConnectorConfiguration(MultiMap configurationParameters) {
		this.configurationParameters = configurationParameters;
	}

	public void setIdentityAttributesToRetrieve(Collection<String> identityAttributesToRetrieve) {
		this.identityAttributesToRetrieve = identityAttributesToRetrieve;
	}

	@Override
	public Collection<String> getIdentityAttributesToRetrieve() {
		return identityAttributesToRetrieve;
	}

	@Override
	public final MultiMap getConfigurationParameters() {
		return configurationParameters;
	}

    @Override
    public String getProvisionAttributeForPrincipalName() {
        return null;
    }
}