/* HEADER */
package com.identity4j.connector.vfs;

/*
 * #%L
 * Identity4J VFS
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


import java.net.URI;
import java.net.URISyntaxException;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.util.MultiMap;

public abstract class AbstractVFSConfiguration extends AbstractConnectorConfiguration {

	public static final String KEY_URI = "uri";
    public AbstractVFSConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

    public abstract String getUri();

	@Override
	public String getUsernameHint() {
		try {
			URI uri = new URI(getUri());
			return uri.getUserInfo();
		} catch (URISyntaxException e) {
			return null;
		}
	}

	@Override
	public String getHostnameHint() {
		try {
			URI uri = new URI(getUri());
			return uri.getHost();
		} catch (URISyntaxException e) {
			return null;
		}
	}
}