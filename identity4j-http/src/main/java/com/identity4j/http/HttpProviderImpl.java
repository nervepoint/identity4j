package com.identity4j.http;

/*
 * #%L
 * Identity4J default HTTP implementation.
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

import com.identity4j.util.http.HttpProvider;
import com.identity4j.util.http.HttpProviderClient;

/**
 * Quite a dumb {@link HttpProvider} based on Apache HTTP that doesn't do any
 * connection pooling or anything smart with clients.
 */
public class HttpProviderImpl implements HttpProvider {

	@Override
	public HttpProviderClient getClient(String urlStr) {
		return getClient(urlStr, null, null, null);
	}

	@Override
	public HttpProviderClient getClient(String urlStr, String username, char[] password, String realm) {
		return new HttpClientImpl(urlStr, username, password, realm);
	}

}