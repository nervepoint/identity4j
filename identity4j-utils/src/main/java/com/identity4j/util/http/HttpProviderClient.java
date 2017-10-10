package com.identity4j.util.http;

/*
 * #%L
 * Identity4J Utils
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

import java.util.Collection;

public interface HttpProviderClient {
	public enum CertificateRequirements {
		STRICT,LOOSE,DEFAULT,NONE
	}

	HttpResponse get(String uri, HttpPair... header) throws HttpException;
	
	HttpResponse post(String uri, Collection<HttpPair> parameters, HttpPair... header) throws HttpException;

	HttpResponse post(String uri, String data, HttpPair... header);

	HttpResponse post(String uri, HttpData data, HttpPair... header);

	HttpResponse patch(String uri, String data, HttpPair... headers);

	HttpResponse put(String uri, String data, HttpPair... headers);

	HttpResponse put(String uri, HttpData data, HttpPair... headers);

	HttpResponse delete(String uri, HttpPair... headers);
	
	void setCertificateRequirements(CertificateRequirements certRequirments);

	void setSocketTimeout(int ms);

	void setConnectTimeout(int ms);

	void setConnectionRequestTimeout(int ms);
	
}
