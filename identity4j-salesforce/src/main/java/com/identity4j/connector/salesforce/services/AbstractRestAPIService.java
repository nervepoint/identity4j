package com.identity4j.connector.salesforce.services;

/*
 * #%L
 * Identity4J Salesforce
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
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.salesforce.SalesforceConfiguration;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.json.JsonMapperService;

/**
 * Abstract class for holding common utility functions by all Active Directory
 * Graph API Service class.
 * 
 * @author gaurav
 *
 */
public abstract class AbstractRestAPIService {

	protected HttpRequestHandler httpRequestHandler;
	protected SalesforceConfiguration serviceConfiguration;

	AbstractRestAPIService(HttpRequestHandler httpRequestHandler, SalesforceConfiguration serviceConfiguration) {
		this.httpRequestHandler = httpRequestHandler;
		this.serviceConfiguration = serviceConfiguration;
	}

	/**
	 * Utility function to set authorization with current JWT token.
	 * 
	 * @param request
	 */
	protected List<HttpPair> getHeaders() {
		List<HttpPair> h = new LinkedList<HttpPair>();
		if (TokenHolder.getInstance().getToken().willExpireIn(2)) {
			try {
				TokenHolder.getInstance().initToken(serviceConfiguration);
			} catch (Exception e) {
				throw new ConnectorException("Problem in getting new token.", e);
			}
		}
		h.add(new HttpPair(SalesforceConfiguration.AUTHORIZATION_HEADER,
				TokenHolder.getInstance().getToken().getBearerAccessToken()));
		h.add(new HttpPair(SalesforceConfiguration.CONTENT_TYPE, SalesforceConfiguration.contentTypeJSON));
		return h;
	}

	/**
	 * Utility function to construct REST API service urls.
	 * 
	 * @param path
	 * @return
	 */
	protected URI constructURI(String path) {
		try {
			return new URI(SalesforceConfiguration.PROTOCOL_NAME, serviceConfiguration.getRestHost(),
					String.format(serviceConfiguration.getRestPath(), serviceConfiguration.getRestApiVersion(), path),
					null, null);
		} catch (URISyntaxException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * Utility function to construct REST API SOQL urls.
	 * 
	 * @param path
	 * @return
	 */
	protected URI constructSOQLURI(String queryString) {
		try {
			return new URI(SalesforceConfiguration.PROTOCOL_NAME, serviceConfiguration.getRestHost(), String
					.format(serviceConfiguration.getRestSOQLQueryPath(), serviceConfiguration.getRestApiVersion()),
					queryString, null);
		} catch (URISyntaxException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * Utility function to construct error object from response error JSON.
	 * 
	 * @param httpResponse
	 */
	protected void throwAppException(HttpResponse httpResponse) {
		List<AppErrorMessage> appErrorMessages = JsonMapperService.getInstance()
				.getObject(new TypeReference<List<AppErrorMessage>>() {
				}, httpResponse.contentString());
		throw new ConnectorException(appErrorMessages.toString());
	}
}
