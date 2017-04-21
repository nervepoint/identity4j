package com.identity4j.connector.zendesk.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.json.JsonMapperService;

/**
 * Abstract class for holding common utility functions by all Zendesk REST API
 * Service class.
 * 
 * @author gaurav
 *
 */
public abstract class AbstractRestAPIService {

	protected HttpRequestHandler httpRequestHandler;
	protected ZendeskConfiguration serviceConfiguration;
	
	AbstractRestAPIService(HttpRequestHandler httpRequestHandler,ZendeskConfiguration serviceConfiguration){
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
		if(TokenHolder.getInstance().getToken().hasPassed(Integer.parseInt(serviceConfiguration.getOAuthTokenValidMinutes()))){
			try{
				TokenHolder.getInstance().initToken(serviceConfiguration);
			}catch(Exception e){
				throw new ConnectorException("Problem in getting new token.",e);
			}
		}
		h.add(new HttpPair(ZendeskConfiguration.AUTHORIZATION_HEADER,
				TokenHolder.getInstance().getToken().getBearerAccessToken()));
		h.add(new HttpPair(ZendeskConfiguration.CONTENT_TYPE, ZendeskConfiguration.contentTypeJSON));
		return h;
	}
	
	/**
	 * Utility function to construct REST API service urls.
	 * @param path
	 * @return
	 */
	protected URI constructURI(String path) {
		try {
			return new URI(ZendeskConfiguration.PROTOCOL_NAME,
					String.format(serviceConfiguration.getRestHost(),serviceConfiguration.getSubDomain()), 
					String.format(
							serviceConfiguration.getRestPath(),
							serviceConfiguration.getRestApiVersion(), path
							),
					null, null);
		} catch (URISyntaxException e) {
			throw new ConnectorException(e.getMessage(),e);
		}
	}
	
	
	/**
	 * Utility function to construct REST API service urls.
	 * @param path
	 * @return
	 */
	protected URI constructURI(String path,String queryString) {
		try {
			return new URI(ZendeskConfiguration.PROTOCOL_NAME,
					String.format(serviceConfiguration.getRestHost(),serviceConfiguration.getSubDomain()), 
					String.format(
							serviceConfiguration.getRestPath(),
							serviceConfiguration.getRestApiVersion(), path
							),
							queryString, null);
		} catch (URISyntaxException e) {
			throw new ConnectorException(e.getMessage(),e);
		}
	}
	
	
	/**
	 * Utility function to construct error object from response error JSON.
	 * @param httpResponse
	 */
	protected void throwAppException(HttpResponse httpResponse) {
		List<AppErrorMessage> appErrorMessages = JsonMapperService.getInstance().
				getObject(new TypeReference<List<AppErrorMessage>>() {}, httpResponse.contentString());
		throw new ConnectorException(appErrorMessages.toString());
	}
}
