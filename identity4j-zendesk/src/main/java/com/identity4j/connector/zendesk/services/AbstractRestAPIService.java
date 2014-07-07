package com.identity4j.connector.zendesk.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.codehaus.jackson.type.TypeReference;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.http.request.HttpRequestHandler.HTTPHook;
import com.identity4j.util.http.response.HttpResponse;
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
	protected final HTTPHook HEADER_HTTP_HOOK = new HTTPHook() {
		@Override
		public void apply(HttpRequestBase httpRequestBase) {
			setAuthHeaders(httpRequestBase);
			httpRequestBase.setHeader(ZendeskConfiguration.CONTENT_TYPE,ZendeskConfiguration.contentTypeJSON);
		}
	};
	
	AbstractRestAPIService(HttpRequestHandler httpRequestHandler,ZendeskConfiguration serviceConfiguration){
		this.httpRequestHandler = httpRequestHandler;
		this.serviceConfiguration = serviceConfiguration;
	}
	
	/**
	 * Utility function to set authorization with current JWT token.
	 * @param request
	 */
	protected void setAuthHeaders(HttpRequestBase request) {
		if(TokenHolder.getInstance().getToken().hasPassed(Integer.parseInt(serviceConfiguration.getOAuthTokenValidMinutes()))){
			try{
				TokenHolder.getInstance().initToken(serviceConfiguration);
			}catch(Exception e){
				throw new ConnectorException("Problem in getting new token.",e);
			}
		}
		request.setHeader(ZendeskConfiguration.AUTHORIZATION_HEADER,
				TokenHolder.getInstance().getToken().getBearerAccessToken());
		request.setHeader(ZendeskConfiguration.ACCEPT,ZendeskConfiguration.contentTypeJSON);
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
				getObject(new TypeReference<List<AppErrorMessage>>() {}, httpResponse.getData().toString());
		throw new ConnectorException(appErrorMessages.toString());
	}
}
