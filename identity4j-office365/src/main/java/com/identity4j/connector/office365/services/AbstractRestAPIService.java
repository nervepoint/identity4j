package com.identity4j.connector.office365.services;

/*
 * #%L
 * Identity4J OFFICE 365
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.services.token.handler.ADToken;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.json.JsonMapperService;

/**
 * Abstract class for holding common utility functions by all Active Directory Graph API
 * Service class.
 * 
 * @author gaurav
 *
 */
public abstract class AbstractRestAPIService {

	protected HttpRequestHandler httpRequestHandler;
	protected Office365Configuration office365Configuration;
	protected ADToken token;
	
	AbstractRestAPIService(ADToken token, HttpRequestHandler httpRequestHandler,Office365Configuration serviceConfiguration){
		this.httpRequestHandler = httpRequestHandler;
		this.token = token;
		this.office365Configuration = serviceConfiguration;
	}
	
	/**
	 * Utility function to set authorization with current JWT token.
	 * @param request
	 */
	protected List<HttpPair> getHeaders() {
		List<HttpPair> h = new LinkedList<HttpPair>();
		if(token.willExpireIn(2)){
			try {
				TokenHolder.refreshToken(token, office365Configuration);
			} catch (IOException e) {
				throw new ConnectorException("Problem in getting new token.",e);
			}
		}
		h.add(new HttpPair(Office365Configuration.AUTHORIZATION_HEADER,	token.getBearerAccessToken()));
		h.add(new HttpPair(Office365Configuration.CONTENT_TYPE,Office365Configuration.contentTypeJSON));
		return h;
	}

	/**
	 * Utility function to construct REST API service urls.
	 * @param path
	 * @param queryOption
	 * @return
	 */
	protected URI constructURI(String path, String queryOption) {
		queryOption = queryOption == null ? office365Configuration.getApiVersion() :  queryOption + "&" + office365Configuration.getApiVersion();
		try {
			return new URI(Office365Configuration.PROTOCOL_NAME,office365Configuration.getRestServiceHost(), "/"	+ office365Configuration.getTenantDomainName() + path,	queryOption, null);
		} catch (URISyntaxException e) {
			throw new ConnectorException(e.getMessage(),e);
		}
	}
	
	/**
	 * Utility function to construct error object from response error JSON.
	 * @param httpResponse
	 */
	protected void throwAppException(HttpResponse httpResponse) {
		AppErrorMessage appErrorMessage = JsonMapperService.getInstance().getObject(AppErrorMessage.class, httpResponse.contentString().replaceAll("odata.error", "error"));
		throw new ConnectorException(appErrorMessage.getError().getCode() + ":" + appErrorMessage.getError().getMessage().getValue());
	}
}
