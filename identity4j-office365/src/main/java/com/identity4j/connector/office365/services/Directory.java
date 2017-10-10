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
