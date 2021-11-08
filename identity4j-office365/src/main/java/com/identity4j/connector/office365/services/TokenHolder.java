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
import com.identity4j.connector.office365.services.token.handler.DirectoryDataServiceAuthorizationHelper;

public class TokenHolder{
	
	/**
	 * Get or refresh a valid token to authenticate REST Service calls.
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public static ADToken refreshToken(ADToken token, Office365Configuration configuration) throws IOException{
		ADToken aadjwtToken = DirectoryDataServiceAuthorizationHelper.
		getOAuthAccessTokenFromACS(configuration.getTenantDomainName(),
				configuration.getGraphPrincipalId(), 
				configuration.getGraphAPIAuthorityURI(), 
				configuration.getAppPrincipalId(),
				configuration.getSymmetricKey(),
				new String[] {configuration.getGraphAPIDefaultScope()});
		if(token == null) {
			return aadjwtToken;
		}
		else {
			token.from(aadjwtToken);
			return token;
		}
	}
}
