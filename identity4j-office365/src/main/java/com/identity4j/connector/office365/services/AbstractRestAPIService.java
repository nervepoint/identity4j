package com.identity4j.connector.office365.services;

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
