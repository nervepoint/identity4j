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
import com.identity4j.connector.salesforce.services.token.handler.SalesforceAuthorizationHelper;
import com.identity4j.connector.salesforce.services.token.handler.Token;

/**
 * 
 * Token Handler encapsulates current active token used by Services to make REST call.
 * 
 * @author gaurav
 *
 */
class TokenHolder{
	
	private Token token;
	
	private TokenHolder(){}

	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final TokenHolder INSTANCE = new TokenHolder();
	}
 
	public static TokenHolder getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	public Token getToken() {
		return token;
	}

	/**
	 * Fetches a valid token to authenticate REST Service calls.
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public void initToken(SalesforceConfiguration configuration) throws IOException{
		token = SalesforceAuthorizationHelper.getInstance().login(
				configuration.getAdminId(), configuration.getAdminPassword(),
				configuration.getAdminSecretKey());
	}

}
