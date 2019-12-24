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
