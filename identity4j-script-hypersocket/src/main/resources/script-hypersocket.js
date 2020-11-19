
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
	var response = httpClient.get(parameters.getLogonSchemeUri());
	if(!response || response.status() != 200) 
		throw "Failed to logon. " + response.status();
	response.release(); // consume response
}

/**
 * Invoked when connector is closed. 
 * 
 * @param parameters connector configuration
 */
function onClose() {
	var response = httpClient.get("api/logoff");
	if(!response || response.status() != 200) 
		log.warn("Failed to logoff. " + response.status());
}

/**
 * Get an iterator of all available identities. 
 * 
 * @return all identities (java.util.Iterator<com.identity4j.connector.principal.Role>)).
 */
function allRoles() {
	var roles = new ArrayList();
	var offset = 0;
	var limit = 100;
	while(true) {
		var response = httpClient.get("api/currentRealm/groups/table?sort=name&order=asc&offset=" + offset + "&limit=" + limit);
		if(response) {
			try {
				if(response.status() == 200) {
					var json = response.toJSON();
					
					// First row is total
					var rows = json.get("rows");
					
					var it = rows.iterator();
					if(!it.hasNext())
						// eof
						break;
					
					while(it.hasNext()) {
						roles.add(parseRole(it.next()));
						offset++;
					}
				}
				else
					throw "Failed to get roles. Response code " + response.status();
			}
			finally {
				// Must always release the response
				response.release();
			}
		}
		else 
			throw "Failed to get roles.";
	}
	
	return roles.iterator();
} 

/**
 * Get an iterator of all available identities. 
 * 
 * @return all identities (java.util.Iterator<com.identity4j.connector.principal.Identity>)).
 */
function allIdentities() {
	var users = new ArrayList();
	var offset = 0;
	var limit = 100;
	while(true) {
		log.info("Getting identities from offset of " + offset);
		var response = httpClient.get("api/currentRealm/users/table?sort=name&order=asc&offset=" + offset + "&limit=" + limit);
		if(response) {
			try {
				if(response.status() == 200) {
					var json = response.toJSON();
					var rows = json.get("rows");
					var it = rows.iterator();
					if(!it.hasNext())
						// eof
						break;
					
					while(it.hasNext()) {
						var entry = it.next();
						var identity = parseIdentity(entry);
						users.add(identity);
						offset++;
					}
					
				}
				else
					throw "Failed to get roles. Response code " + response.status();
			}
			finally {
				// Must always release the response
				response.release();
			}
		}
		else 
			throw "Failed to get roles.";
	}
	
	return users.iterator();
} 

/**
 * Count how many users there are. This function is optional, but recommended
 * for performance reasons. 
 * 
 * @return number of identities
 */
function countIdentities() {
	var offset = 0;
	var limit = 100;
	var response = httpClient.get("api/currentRealm/users/table?sort=name&order=asc&offset=" + offset + "&limit=" + limit);
	if(response) {
		try {
			if(response.status() == 200) {
				return response.toJSON().get("total").getAsInt();
			}
			throw "Failed to get count identities. " + response.status() + ". " + response.statusError();
		}
		finally {
			response.release();
		}
	}
	throw "Failed to get count identities."; 
}

function parseRole(entry) {
	return new RoleImpl(entry.get("id").getAsString(), entry.get("name").getAsString());
}

function parseIdentity(entry) {
	var id = entry.get("id").getAsInt();
	var gen = entry.get("groups");
	
	/* If we have groups attribute, use that, otherwise we need to explicity get the user again to get
	 * the groups. This is only true if we are iterating users on earlier HS versions.
	 */
	if(!entry.has("groups")) {
		log.debug('Getting identity by ID ' + id);
	    var response = httpClient.get("api/currentRealm/user/" + id);
		if(response) {
			try {
				if(response.status() == 200) {
					gen = response.toJSON().get("groups");
				}
				else if(response.status() == 404) {
					throw "UnsupportedOperationException";
				}
				else
					throw "Failed to get identity by name. Response code " + response.status();
			}
			finally {
				response.release();
			}
		}
		else 
			throw "Failed to get identity by name."; 
	}

	var identity = new IdentityImpl(entry.get("id").getAsString(), entry.get("name").getAsString());
	if(entry.has("fullname"))
		identity.setFullName(entry.get("fullname").getAsString());
	var es = entry.entrySet();
	var it = es.iterator();
	while(it.hasNext()) {
		var e = it.next();
		if(!e.getValue().isJsonNull() && e.getKey() != 'resolvedPasswordPolicy' && e.getKey() != 'groups')
			identity.setAttribute(e.getKey(), e.getValue().getAsString());
	}
	
	//https://portal.hypersocket.net/hypersocket/api/currentRealm/groups/user/851979
	if(gen.isJsonNull()) {
		// HS does not populate this :(
		log.debug('Getting groups by user ID ' + id);
	    var response = httpClient.get("api/currentRealm/groups/user/" + id);
		if(response) {
			try {
				if(response.status() == 200) {
					var json = response.toJSON();
					if(json.get("success").getAsBoolean()) {
						var resources = json.get("resources");
						var it = resources.iterator();
						while(it.hasNext()) {
							identity.addRole(parseRole(it.next()));
						}
					}
					else
						throw json.get("message").getAsString();
				}
				else if(response.status() == 404) {
					throw "UnsupportedOperationException";
				}
				else
					throw "Failed to get identity by name. Response code " + response.status();
			}
			finally {
				response.release();
			}
		}
		else 
			throw "Failed to get identity by name."; 
	}
	else {
		// TODO For when HS does return groups in user details
		it = gen.getAsJsonArray().iterator();
		while(it.hasNext()) {
			identity.addRole(connector.getRoleByName(it.next()));
		}
	}
		
	return identity;
}

/**
 * Get a role given its principal name or thrown an exception if
 * no such role exists. 
 * 
 * @return name role name
 * @throws PrincipalNotFoundFound
 */
function getRoleByName(name) {
    log.debug('Getting identity by name ' + name);
    var response = httpClient.get("api/currentRealm/group/byName/" + encodeURIComponent(name));
	if(response) {
		try {
			if(response.status() == 200) {
				var json = response.toJSON();
				if(json.get("success").getAsBoolean()) {
					var entry = json.get("resource");
					return parseRole(entry);
				}
				else
					throw json.get("message").getAsString();
			}
			else if(response.status() == 404) {
				throw "UnsupportedOperationException";
			}
			else
				throw "Failed to get role by name. Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to get role by name."; 
}

/**
 * Get an identity given its principal name or thrown an exception if
 * no such identity exists. 
 * 
 * @return name identity name
 * @throws PrincipalNotFoundFound
 */
function getIdentityByName(name) {
    log.debug('Getting identity by name ' + name);
    var response = httpClient.get("api/currentRealm/user/byName/" + encodeURIComponent(name));
	if(response) {
		try {
			if(response.status() == 200) {
				var json = response.toJSON();
				if(json.get("success").getAsBoolean()) {
					var entry = json.get("resource");
					return parseIdentity(entry);
				}
				else
					throw json.get("message").getAsString();
			}
			else if(response.status() == 404) {
				throw "UnsupportedOperationException";
			}
			else
				throw "Failed to get identity by name. Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to get identity by name."; 
}

/**
 * Test if credentials are valid. 
 * 
 * @param identity identity (com.identity4j.connector.principal.Identity)
 * @param password password (java.lang.String)
 * @return all roles (boolean true or false).
 */
function areCredentialsValid(identity, password) {
	log.debug('Checking credentials of ' + identity);
	var client = httpProvider.getClient(config.getUrl(), identity.getPrincipalName(), password.toCharArray(), null);
	var response = client.get("api/logon/system");
	try {
		if(!response || response.status() != 200) 
			return false;
	}
	finally {
		response.release();
	}
	response = client.get("api/logoff");
	try {
		if(!response || response.status() != 200) 
			log.warn("Failed to logon. " + response.status());
	}
	finally {
		response.release();
	}
	return true;
}

/**
 * Sets a user's password. 
 * 
 * @param identity identity (com.identity4j.connector.principal.Identity)
 * @param password new password (java.lang.String)
 * @param forceChangeAtNextLogon force password change on logon (boolean))
 * @return false if password change is not supported
 */
function setPassword(identity, password, forceChangeAtNextLogon) { 
	log.debug('Setting credentials of name ' + identity);
    var response = httpClient.post("api/currentRealm/user/credentials", { 
		"principalId" : identity.getGuid(), 
		"password" : password, 
		"force" :  forceChangeAtNextLogon 
    },{
		"Content-Type" : "application/json"
	});
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

/**
 * Update identity
 * 
 * @param identity identity (com.identity4j.connector.principal.Identity) 
 */
function updateIdentity(identity) {
	var props = [];
	var ens = identity.getAttributes().entrySet();
	var enit = ens.iterator();
	while(enit.hasNext()) {
		var en = enit.next();
		props.push({ id: en.getKey(), value: en.getValue() });
	}
	var response = httpClient.post("api/currentRealm/user", {
		"name" : identity.getPrincipalName(), 
		"password" : "", 
		"force" : false,
		"id" : java.lang.Integer.parseInt(identity.getGuid()), // javascript parseInt ends up as a float!!?
		"properties" : props
	},{
		"Content-Type" : "application/json"
	});
	//if(response) {
		try {
			if(response.status() == 200) {
				return ;
			}
			throw "Failed to update identity. " + response.status(); 
		}
		finally {
			response.release();
		}
	//}
	throw "Failed to update identity." + response.status(); 
}

/*
 * Delete role
 *
 * @param role role object
 */
function deleteRole(role) {
	log.debug('Delete role (hypersocket group) ' + role);
    var response = httpClient.delete("api/currentRealm/group/" + role.getGuid());
	if(response) {
		try {
			if(response.status() != 200)
				throw "Failed to delete role (hypersocket group). Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to delete role (hypersocket group)."; 
}

/*
 * Create role
 *
 * @param role role object
 */
function createRole(role) {
	log.debug('Create role (hypersocket group) ' + role);
    var response = httpClient.post("api/currentRealm/group", { 
		"name" : role.getPrincipalName(),
		"groups" : [],
		"users": [],
		"properties": []
    },{
		"Content-Type" : "application/json"
	});
	if(response) {
		try {
			if(response.status() != 200)
				throw "Failed to create role (hypersocket group). Response code " + response.status();
		}
		finally {
			response.release();
		}
	}
	else 
		throw "Failed to create role (hypersocket group)."; 
}

/**
 * Get the password characteristics. Use defaults in this case.
 */
function getPasswordCharacteristics() {
	/*
	var response = httpClient.get("api/passwordPolicys/policy/" + princId);
	if(!response || response.status() != 200) 
		log.warn("Failed to get password characteristics. " + response.status());
	else {
		var pc = new DefaultPasswordCharacteristics();
		var json = response.toJSON();
		var res = json.get("resource");
		if(res) {
			pc.setMinimumSize(json.get("minimumLength").getAsInt());
			pc.setMaximumSize(json.get("maximumLength").getAsInt());
			pc.setContainUsername(json.get("containUsername").getAsBoolean());
			pc.setDictionaryWordsAllowed(json.get("containDictionaryWord").getAsBoolean());
			pc.setRequiresMatches(json.get("minimumCriteriaMatches").getAsInt());
			pc.setMinimumDigits(json.get("minimumDigits").getAsInt());
			pc.setMinimumLowerCase(json.get("minimumLower").getAsInt());
			pc.setMinimumUpperCase(json.get("minimumUpper").getAsInt());
			pc.setMinimumSymbols(json.get("minimumSymbol").getAsInt());
			pc.setSymbols(json.get("validSymbols").getAsString().toCharArray());
			pc.setHistorySize(json.get("passwordHistory").getAsInt());
			pc.setVeryStrongFactor(json.get("veryStrongFactor").getAsInt());
			var es = res.entrySet();
			var it = es.iterator();
			while(it.hasNext()) {
				var e = it.next();
				var k = e.getKey();
				var v = e.getValue();
				if(e != 'minimumLength' && e != 'maximumLength' && 
						e != 'containUsername' && e != 'containDictionaryWord' &&
						e != 'minimumCriteriaMatches' && e != 'minimumDigits' &&
						e != 'minimumLower' && e != 'minimumUpper' &&
						e != 'minimumSymbol' && e != 'validSymbols' &&
						e != 'passwordHistory' && e != 'veryStrongFactor') {
					pc.getAttributes().put(k, v);
				}
			}
		}
		return pc;
	}
	*/
	return new DefaultPasswordCharacteristics();
}
