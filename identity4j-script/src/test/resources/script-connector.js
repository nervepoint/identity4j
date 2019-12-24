/*
 * #%L
 * Identity4J Scripted Connectors
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
importPackage(java.util); 
importPackage(com.identity4j.connector.principal); 
importPackage(com.identity4j.connector.exception);
	
user1 = new IdentityImpl('1','user1'); 
user2 = new IdentityImpl('2','user2'); 
user3 = new IdentityImpl('2','user3'); 
	
role1 = new RoleImpl('1','role1'); 
	
users = Arrays.asList([user1,user2,user3]); 
roles = Arrays.asList([role1]); 
	
currentPassword = 'qwqwqw';
	
function allIdentities() { 
	return users.iterator(); 
} 
	
function allRoles() { 
	return roles.iterator(); 
	}

function areCredentialsValid(identity, password) { 
	return users.contains(identity) && password == currentPassword; 
}

function setPassword(identity, password, forceChangeAtNextLogon) { 
	if(password.length > 14) { 
		throw ConnectorException; 
	} 
	currentPassword = password;
	if(forceChangeAtNextLogon) { 
		identity.setPasswordStatus(new PasswordStatus(null, null, PasswordStatusType.changeRequired));
	}
}
	
	