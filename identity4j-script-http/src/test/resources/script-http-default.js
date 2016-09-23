
importPackage(java.util);
importClass(java.lang.System);
importPackage(com.identity4j.connector.principal); 
importPackage(com.identity4j.util.passwords); 
importClass(com.identity4j.connector.principal.AccountStatusType);  
importClass(com.identity4j.util.Util); 
importClass(com.identity4j.util.StringUtil);
importClass(java.text.SimpleDateFormat);

// Public API functions
/**
 * Invoked when connector is opened. 
 * 
 * @param parameters connector configuration
 */
function onOpen(parameters) {
}

/**
 * Invoked when connector is closed. 
 * 
 * @param parameters connector configuration
 */
function onClose() {
}

/**
 * Get an iterator of all available identities. 
 * 
 * @return all identities (java.util.Iterator<com.nervepoint.connector.principal.Identity>)).
 */
function allIdentities() {
	var response = httpClient.get("allIdentities");
	if(response) {
		try {
			if(response.status() == 200) {
				var json = response.toJSON();
				var it = json.entrySet().iterator();
				var users = new ArrayList();
				while(it.hasNext()) {
					var entry = it.next();
					var userId = entry.getKey();
					var obj = json.get(userId);
					var identity = new IdentityImpl(obj.get("guid").getAsString(), userId);
					identity.fullName = obj.get("fullName").getAsString(); 
					users.add(identity);
				}
				return users.iterator();
			}
			else
				throw "Failed to get identities. Response code " + response.status();
		}
		finally {
			// Must always release the response
			response.release();
		}
	}
	else 
		throw "Failed to get identities.";
} 

/**
 * Count how many users there are. This function is optional, but recommended
 * for performance reasons. 
 * 
 * @return number of identities
 */
function countIdentities() {
	var response = httpClient.get("countIdentities");
	if(response) {
		try {
			if(response.status() == 200) {
				return response.toJSON().get("count").getAsInt();
			}
			else
				throw "Failed to get count identities. Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to get count identities."; 
}

/**
 * Get an identity given its principal name or thrown an exception if
 * no such identity exists. 
 * 
 * @return identity identity (com.nervepoint.connector.principal.Identity).
 * @throws PrincipalNotFoundFound
 */
function getIdentityByName(name) {
    log.debug('Getting identity by name ' + name);
    var response = httpClient.get("getIdentityByName/" + encodeURIComponent(name));
	if(response) {
		try {
			if(response.status() == 200) {
				var json = response.toJSON();
				var identity = new IdentityImpl(json.get("guid").getAsString(), json.get("principalName").getAsString());
				identity.fullName = json.get("fullName").getAsString();
				return identity;
			}
			else if(response.status() == 404) {
				return null;
			}
			else
				throw "Failed to get get identity by name. Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to get count identities."; 
}

/**
 * Test if credentials are valid. 
 * 
 * @param identity identity (com.nervepoint.connector.principal.Identity)
 * @param password password (java.lang.String)
 * @return all roles (boolean true or false).
 */
function areCredentialsValid(identity, password) {
	log.debug('Checking credentials of ' + identity);
    var response = httpClient.post("checkCredentials/" + encodeURIComponent(identity.getPrincipalName()), { "password" : password });
	if(response) {
		try {
			if(response.status() == 200) {
				var json = response.toJSON();
				if(json.has("forceChange") && json.get("forceChange").getAsBoolean()) {
					throw PasswordChangeRequiredException;
				}
				return true;
			}
			else if(response.status() == 401 || response.status() == 404) {
				return false;
			}
			else
				throw "Failed to get get identity by name. Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to get check credentials."; 
}

/**
 * Sets a user's password. 
 * 
 * @param identity identity (com.nervepoint.connector.principal.Identity)
 * @param password new password (java.lang.String)
 * @param forceChangeAtNextLogon force password change on logon (boolean))
 * @return false if password change is not supported
 */
function setPassword(identity, password, forceChangeAtNextLogon) { 
	log.debug('Setting credentials of name ' + identity);
    var response = httpClient.post("setPassword/" + encodeURIComponent(identity.getPrincipalName()), { "newPassword" : password, "force" :  forceChangeAtNextLogon });
	if(response) {
		try {
			if(response.status() != 200)
				throw "Failed to set credentials. Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to get set credentials."; 
}
