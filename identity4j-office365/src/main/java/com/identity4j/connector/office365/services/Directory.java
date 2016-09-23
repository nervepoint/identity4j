package com.identity4j.connector.office365.services;

import java.io.IOException;

import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.services.token.handler.ADToken;
import com.identity4j.util.http.request.HttpRequestHandler;

/**
 * Directory class initializes all the service class used for making REST calls.
 * 
 * @author gaurav
 *
 */
public class Directory {
	private HttpRequestHandler httpRequestHandler;
	private UserService userServices;
	private GroupService groupService; 
	
	
	/**
	 * Initializes all the REST API services and requests JWT token for the service classes to 
	 * use while making REST calls
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public void init(Office365Configuration configuration) throws IOException{
		httpRequestHandler = new HttpRequestHandler();
		ADToken token = TokenHolder.refreshToken(null, configuration);
		userServices = new UserService(token, httpRequestHandler,configuration);
		groupService = new GroupService(token, httpRequestHandler, configuration);
	}

	public UserService users() {
		return userServices;
	}
	
	public GroupService groups(){
		return groupService;
	}
}
