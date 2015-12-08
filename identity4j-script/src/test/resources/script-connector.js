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
	
	