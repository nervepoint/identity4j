package com.identity4j.connector.salesforce.services;

import java.io.IOException;

import com.identity4j.connector.salesforce.SalesforceConfiguration;
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
	
	private Directory(){}
	
	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final Directory INSTANCE = new Directory();
	}
 
	public static Directory getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	/**
	 * Initializes all the REST API services and requests JWT token for the service classes to 
	 * use while making REST calls
	 * 
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public void init(SalesforceConfiguration configuration) throws IOException{
		httpRequestHandler = new HttpRequestHandler();
		
		TokenHolder.getInstance().initToken(configuration);
		
		groupService = new GroupService(httpRequestHandler, configuration);
		userServices = new UserService(httpRequestHandler,configuration,groupService);
		
		
	}

	public UserService users() {
		return userServices;
	}
	
	public GroupService groups(){
		return groupService; 
	}
}
