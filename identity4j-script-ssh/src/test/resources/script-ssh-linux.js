/*
 * #%L
 * Identity4J Scripted SSH Connector
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
importClass(java.lang.System);
importPackage(com.identity4j.connector.principal); 
importPackage(com.identity4j.util.passwords); 
importClass(com.identity4j.connector.principal.AccountStatusType);  
importClass(com.identity4j.util.Util); 
importClass(com.identity4j.util.StringUtil);
importPackage(com.maverick.ssh);
importPackage(com.sshtools.net);
importPackage(com.sshtools.ssh);
importClass(java.text.SimpleDateFormat);

/**
 * This script is intended for use with the SSH connector.
 * It provides user and password management on UNIX like
 * hosts via SSH.
 *
 * Simple commands are used to load identities and roles
 * from the /etc/passwd, /etc/shadow and /etc/group files.
 * 
 * Password validation creates a new SSH session and relies
 * on the SSH refusing incorrect credentials.
 *  
 * Password setting and changing is performed using the 
 * 'passwd' command and requires that the server account
 * is root.
 * 
 * The following beans available are in SSH connector
 * scripts.
 * 
 * log          (org.apache.commons.logging.Log)
 * connector    (com.nervepoint.connector.script.ScriptConnector)
 * shell        (com.maverick.ssh.Shell)
 * sshClient    (com.maverick.ssh.SshClient)
 * config       (com.nervepoint.connector.ssh.SshConfiguration)
 * connector    (com.maverick.ssh.SshConnector)
 */

var users = new ArrayList();
var roles = new ArrayList();
var roleMap = new HashMap();
var userRoleMap = new HashMap();
var minUid = config.getConfigurationParameters().getIntegerOrDefault('ssh.minUid', 0);
var maxUid = config.getConfigurationParameters().getIntegerOrDefault('ssh.maxUid', 10000000);

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
    var now = System.currentTimeMillis();
    fetchRoles();
    fetchIdentities();
    // TODO stop storing all the identities in memory, i.e. implement am iterator
    return users.iterator(); 
} 

/**
 * Count how many users there are. This function is optional, but recommended
 * for performance reasons. 
 * 
 * @return number of identities
 */
function countIdentities() {
    return count("/etc/passwd");
}

/**
 * Count how many roles there are. This function is optional, but recommended
 * for performance reasons. 
 * 
 * @return number of roles
 */
function countRoles() {
    return count("/etc/group");
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
    var identity;
    var line;
    var group;
    
    // Get the main identity details
    log.debug('Executing process');
    var process = sshClient.executeCommand('grep \'^' + name + ':\' /etc/passwd');
    try {
        log.debug('Executed process');
        line = process.readLine();
        log.debug('Read ' + line);
        if(line) {
            identity = parseIdentity(line);
            var uid = parseInt(identity.getGuid());
            if(uid >= minUid && uid <= maxUid) {
            	group = line.split(":")[3];
            }
            else {
            	return null;
            }
        }
        else {
            return null;
        }
    }
    finally {
        while( process.readLine() != null);
    }
    
    // Add the primary role given its ID
    if(group) {
        log.debug('Executing group process');
        process = sshClient.executeCommand('grep \'^.*:.*:' + group + ':\' /etc/group');
        try {
            log.debug('Executed group process');
            line = process.readLine();
            log.debug('Read ' + line);
            if(line) {
                identity.addRole(parseRole(line));
            }
        }
        finally {
            while( process.readLine() != null);
        }
    }
    
    // Add the secondary roles. The grep narrows it down, but more processing is needed
    log.debug('Executing roles process');
    process = sshClient.executeCommand('grep \'^.*:.*:.*:.*' + identity.getPrincipalName() + '\' /etc/group');
    log.debug('Executed roles process');
    while(true) { 
        line = process.readLine();
        log.debug('Read ' + line);
        if(!line) {
            break;
        }
        
        // Get an exact check on whether the password is in the comma separated list
        var inGroups = (line.split(':')[3]).split(',');
        for(i = 0 ; i < inGroups.length; i++) {
            if(inGroups[i] == identity.getPrincipalName()) {
                identity.addRole(parseRole(line));
                break;
            }
        }
    }
    
    loadLastLogin(identity);
    loadExpiry(identity);
    
    return identity;
}

/**
 * Get a role given its principal name or thrown an exception if
 * no such role exists. 
 * 
 * @return role role (com.nervepoint.connector.principal.Role).
 */
function getRoleByName(name) {
    var process = sshClient.executeCommand('grep \'^' + name + ':\' /etc/group');
    try {
        var line = process.readLine();
        if(!line) {
            return null;
        }
        return parseRole(line);
    }
    finally {
        while( process.readLine() != null);
    }
}

/**
 * Get an iterator of all available roles. 
 * 
 * @return all roles (java.util.Iterator<com.nervepoint.connector.principal.Role>)).
 */
function allRoles() {
    log.debug("Retrieving all roles");
    var now = System.currentTimeMillis();
    fetchRoles();
    return roles.iterator(); 
}

/**
 * Update a user.
 * 
 * @param identity identity (com.nervepoint.connector.principal.Identity)
 */
function updateIdentity(identity)  {
    if(execCommand('which usermod') == 0) {
    	var oldIdentity = getIdentityByName(identity.getPrincipalName());
    	
        // Ubuntu
        log.debug("Use usermod");
        
        var cmd = 'usermod';
        var differs = false;
        
        // Shell
        if(Util.differs(identity.getAttribute('shell'), oldIdentity.getAttribute('shell'))) {
            cmd += ' --shell "';
            cmd += identity.getAttribute('shell');
            cmd += '"';
            differs = true;
        }

        // Home
        if(Util.differs(identity.getAttribute('home'), oldIdentity.getAttribute('home'))) {
            cmd += ' --home "';
            cmd += identity.getAttribute('home');
            cmd += '"';
            if (config.getConfigurationParameters().getBooleanOrDefault('ssh.deleteUserHomeOnDeleteAccount',false)) {
                cmd += ' --move-home';
            }
            differs = true;
        }
        
        // UID
        if(Util.differs(identity.getAttribute('uid'), oldIdentity.getAttribute('uid'))) {
            cmd += ' --uid "';
            cmd += identity.getAttribute('uid');
            cmd += '"';
            differs = true;
        }
        
        // GECOS
        if(Util.differs(identity.getFullName(), oldIdentity.getFullName()) || 
        		Util.differs(identity.getAttributeOrDefault('building', ''), oldIdentity.getAttributeOrDefault('building', '')) || 
        		Util.differs(identity.getAttributeOrDefault('officePhone', ''), oldIdentity.getAttributeOrDefault('officePhone', '')) || 
        		Util.differs(identity.getAttributeOrDefault('otherContact', ''), oldIdentity.getAttributeOrDefault('otherContact', ''))) {
	        cmd += ' --comment "';
	        cmd += StringUtil.nonNull(identity.getFullName());
	        cmd += ',';
	        cmd += identity.getAttributeOrDefault('building', '');
	        cmd += ',';
	        cmd += identity.getAttributeOrDefault('officePhone', '');
	        cmd += ',';
	        cmd += identity.getAttributeOrDefault('otherContact', '');
	        cmd += '"';
            differs = true;
        }
        
        // Username
        if(differs) {
	        cmd += ' ' + identity.getPrincipalName();
	
	        var ret = execCommand(cmd);
	        if(ret != 0) {
	            throw "usermod ('" + cmd + "') exited with non-zero status.";
	        }
    	}
    }
}

/**
 * Create a new user.
 * 
 * @param identity identity (com.nervepoint.connector.principal.Identity)
 * @param password (java.lang.String) 
 */
function createIdentity(identity, password)  {
    if(execCommand('which adduser') == 0) {
        // Ubuntu
        log.debug("Use (Debian style) adduser");
        
        var cmd = 'adduser';
        
        // Shell
        if(!StringUtil.isNullOrEmpty(identity.getAttribute('shell'))) {
            cmd += ' --shell "';
            cmd += identity.getAttribute('shell');
            cmd += '"';
        }

        // Home
        if(!StringUtil.isNullOrEmpty(identity.getAttribute('home'))) {
            cmd += ' --home "';
            cmd += identity.getAttribute('home');
            cmd += '"';
        }
        
        // GECOS
        cmd += ' --gecos "';
        cmd += StringUtil.nonNull(identity.getFullName());
        cmd += ',';
        cmd += identity.getAttributeOrDefault('building', '');
        cmd += ',';
        cmd += identity.getAttributeOrDefault('officePhone', '');
        cmd += ',';
        cmd += identity.getAttributeOrDefault('otherContact', '');
        cmd += '"';
        
        /* Disable the password by default to prevent the prompt, we'll set it using
         * the setPassword() function afterwards
         */ 
        cmd += ' --disabled-password';
        
        // Username
        cmd += ' ' + identity.getPrincipalName();

        var ret = sudoCommand(cmd);
        if(ret != 0) {
            throw "adduser exited with non-zero status.";
        }
        else {
            setPassword(identity, password, false);
        }
    }
}


/**
 * Delete a user.
 * 
 * @param principalName
 *            (java.lang.String)
 */
function deleteIdentity(principalName) {
    if(execCommand('which deluser') == 0) {
        var cmd = 'deluser';
    
        // Shell
        if (config.getConfigurationParameters().getBooleanOrDefault('ssh.deleteUserHomeOnDeleteAccount',false)) {
            var identity = getIdentityByName(principalName);
            var home = identity.getAttribute('home');
            if(home && home.length > 0 && execCommand('test -d "' + home + '"') == 0) {
                log.info('Deleting home directory for ' + principalName);
                cmd += ' --remove-home';
            }
            else {
                log.warn('Home directory ' + home + ' does not exist on this server, no deletion of home.');
            }
        }
        cmd += ' ' + principalName;
    
        var ret = sudoCommand(cmd);
        if (ret != 0) {
            throw "deluser exited with non-zero status.";
        } 
    }
}

/**
 * Test if credentials are valid. 
 * 
 * @param identity identity (com.nervepoint.connector.principal.Identity)
 * @param password password (java.lang.String)
 * @return all roles (boolean true or false).
 */
function areCredentialsValid(identity, password) {
    var transport = new SocketTransport(config.getHost(), config.getPort());
    var clientTest = sshProtocolConnector.connect(transport, identity.getPrincipalName(), true);
    try {
        pwd = new PasswordAuthentication();
        pwd.setPassword(password);
        if (clientTest.authenticate(pwd) == SshAuthentication.COMPLETE) {
            return true;
        } else {
            return false;
        }
    }
    finally {
        clientTest.disconnect();
    }
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

    if(password.length > 14) { 
        throw ConnectorException; 
    }
    

    /* If chpasswd is available, use this. It means we don't have to use 
     * the passwd command and expect
     */
    var result = false;
    if(sshClient.sudoCommand('which chpasswd').drainAndWaitForExit() == 0) {
        log.debug("Executing chpasswd");
        var process = sshClient.executeCommand('chpasswd');
        try {
            process.typeAndReturn(identity.getPrincipalName() + ":" + password);
            process.type(4);
            log.debug("Executed chpasswd");
            result = true;
        }
        finally {
            process.drainAndWaitForExit();
        }
    }
    else {  
        // We need to know the auth token PAM will display (by default this is UNIX)
        var characteristics = getPasswordCharacteristics();

        log.debug("Executing password");
        var process = sshClient.sudoCommand('passwd \'' + identity.getPrincipalName() + '\'');
        try {
            if(characteristics.isUseCracklib()) {
                /* Chat seems to be different when cracklib is in use, there are probably other
                 * variations too
                 */
                process.expect('New password: ', false, 10000);
                process.typeAndReturn(password);
                process.expect('Retype new password: ', false, 10000);
            }
            else {
                process.expect('new ' + characteristics.getAuthToken() + ' password: ', false, 10000);
                process.typeAndReturn(password);
                process.expect('new ' + characteristics.getAuthToken() + ' password: ', false, 10000);
            }
            process.typeAndReturn(password);
            process.expect('password updated successfully');
            result = true;
        }
        finally {
            process.drainAndWaitForExit();
        }
    }
    
    
    if(forceChangeAtNextLogon && sshClient.executeCommand('which chage').drainAndWaitForExit() == 0) {
        var process = sshClient.executeCommand('chage -d 0 \'' + identity.getPrincipalName() + '\'');
        process.drainAndWaitForExit();
    }
    
    return result;
}

/**
 * Unlock an account.  
 * 
 * @param identity identity object
 */
function unlockIdentity(identity) {
    /* Nervepoint's concept of an account lock actually
     * corresponds to a password lock on Unix. According the man page for
     * passwd, an account lock is achieved by making the account expired.
     * We don't really want to allow self service account unlock to unexpire
     * accounts, so just work with the password lock
     */
    sudoCommand('passwd -qu \'' + identity.getPrincipalName() + '\'');
}

/**
 * Enable an account. 
 * 
 * @param identity identity object 
 */
function enableIdentity(identity) {
    sudoCommand('usermod --unlock --expiredate \'\' \'' + identity.getPrincipalName() + '\'');
}

/**
 * Disable an account.  
 * 
 * @param identity identity object 
 */
function disableIdentity(identity) {
    sudoCommand('usermod --lock --expiredate 1970-01-01 \'' + identity.getPrincipalName() + '\'');
}

/**
 * Get the password characteristics. If this is not possible, just return
 * null and the defaults will be used.
 * 
 * @return password characteristics
 */
function getPasswordCharacteristics() {
    
    // Some explanation - http://www.itworld.com/endpoint-security/275056/how-enforce-password-complexity-linux
    
    var c = new UNIXPasswordCharacteristics();
    c.setMinimumSize(6);
    c.setDictionaryWordsAllowed(true);
    c.setContainUsername(true);
    c.setUseCracklib(false);
    
    
    // Look for PAM configuration
    var file = null;
    if(execCommand('test -f /etc/pam.d/common-password') == 0) {
        // Ubuntu
        log.debug("Use (Debian style) PAM from " + file);
        file = '/etc/pam.d/common-password';
    }
    else if(execCommand('test -f /etc/pam.d/system-auth') == 0) {
        // Redhat
        log.debug("Use (Redhat style) PAM from " + file);
        file = '/etc/pam.d/system-auth';
    }
    
    if(file) {

        var lcredit = 1;
        var ucredit = 1;
        var dcredit = 1;
        var ocredit = 1;
        var minlength = -1;
        var maxlength = -1;
        var entries = 0;
        var authToken = 'UNIX';
        var historySize = 0;
        
        // Unix
        var process = sshClient.executeCommand('grep "password.*pam_unix.so" ' + file);
        var scheme = 'des';
        while(line = process.readLine()) {
            line = line.trim();
            if(!line.startsWith("#")) {
                entries++;
                var elements = line.split(" ");
                for(i = 0; i < elements.length; i++) {
                    if(elements[i].startsWith('remember')) {
                        historySize = parsePamVal(elements[i]);
                    }
                    if(elements[i].startsWith('min') || elements[i].startsWith('minlen')) {
                        minlength = parsePamVal(elements[i]);
                    }
                    if(elements[i].startsWith('max') || elements[i].startsWith('maxlen')) {
                        maxlength = parsePamVal(elements[i]);
                    }
                    if(elements[i] == 'md5' || elements[i] == 'sha256' || elements[i] == 'sha512' || elements[i] == 'bigcrypt' || elements[i] == 'blowfish') {
                        scheme = elements[i];
                    }
                }
            }
        }
        if(entries > 0) {
            log.debug("Found pam_unix minlength = " + minlength + " maxlenght = " + maxlength + " scheme = " + scheme);
            c.setHistorySize(historySize);
            if(minlength > -1) {
                c.setMinimumSize(minlength);
            }
            if(maxlength > -1) {
                c.setMaximumSize(maxlength);
            }
            else if(scheme == 'des') {
                c.setMaximumSize(8);
            }
        }
        
        // Cracklib
        process = sshClient.executeCommand('grep "password.*pam_cracklib.so" ' + file);
        entries = 0;
        while(line = process.readLine()) {
            line = line.trim();
            if(!line.startsWith("#")) {
                entries++;
                var elements = line.split(" ");
                for(i = 0; i < elements.length; i++) {
                    if(elements[i].startsWith('lcredit')) {
                        lcredit = parsePamVal(elements[i]);
                    }
                    if(elements[i].startsWith('ucredit')) {
                        ucredit = parsePamVal(elements[i]);
                    }
                    if(elements[i].startsWith('dcredit')) {
                        dcredit = parsePamVal(elements[i]);
                    }
                    if(elements[i].startsWith('ocredit')) {
                        ocredit = parsePamVal(elements[i]);
                    }
                    if(elements[i].startsWith('minlen')) {
                        minlength = parsePamVal(elements[i]);
                    }
                    if(elements[i].startsWith('authtok_type')) {
                        authToken = parsePamVal(elements[i]);
                    }               
                    
                    
                    if(elements[i].startsWith('reject_username')) {
                        // TODO this would actually prevent username in reverse as well but
                        // NAM doesn't yet support this check
                        c.setContainUsername(false);
                    }
                }
            }
        }
        
        if(entries > 0) {
            // The very existence of cracklib signals dictionary words are not allowed
            c.setDictionaryWordsAllowed(false);
            c.setAuthToken(authToken);
            c.setUseCracklib(true);
            
            c.setMinimumSize(6); // Minimum required by cracklib
            var requiredMatches = 4;
            log.debug("Found pam_cracklib minlength = " + minlength + " ocredit = " + ocredit + " lcredit = " + lcredit + " ucredit = " + ucredit  + " dcredit = " +dcredit);
            
            /* When the credit numbers are negative, these are fixed minimum character counts
             * that must match. When negative, they are suppose to be optional but credit towards
             * the minimum length. We can't really do that, so they are not optional.
             */
            if(lcredit < 0) {
                c.setMinimumLowerCase(Math.abs(lcredit));
            }
            else if(lcredit > 0) {
                c.setMinimumLowerCase(lcredit);
                minlength = minlength - lcredit;
            } else {
                requiredMatches--;
            }
            
            if(ucredit < 0) {
                c.setMinimumUpperCase(Math.abs(ucredit));
            }
            else if(ucredit > 0) {
                c.setMinimumUpperCase(ucredit);
                minlength = minlength - ucredit;
            } else {
                requiredMatches--;
            }
            
            if(dcredit < 0) {
                c.setMinimumDigits(Math.abs(dcredit));
            }
            else if(dcredit > 0) {
                c.setMinimumDigits(dcredit);
                minlength = minlength - dcredit;
            } else {
                requiredMatches--;
            }
            
            if(ocredit < 0) {
                c.setMinimumSymbols(Math.abs(ocredit));
            }
            else if(ocredit > 0) {
                c.setMinimumSymbols(ocredit);
                minlength = minlength - ocredit;
            } else {
                requiredMatches--;
            }

            c.setRequiresMatches(requiredMatches);
            if(minlength > c.getMinimumSize()) {
                c.setMinimumSize(minlength);
            }
            log.debug("Final pam_cracklib minlength = " + minlength);
        }
    }
    else {
        log.debug("No password rules were retreived from PAM");
    }
    
    return c;
}

// Helper functions

function count(file) {
    var process = sshClient.executeCommand('wc -l ' + file);
    try {
        line = process.readLine();
        if(line) {
            return parseInt(line.split(" ")[0]);
        }
    }
    finally {
        while( process.readLine() != null);
    }
    return -1;
}

function sudoCommand(cmd) {
    /* For commands where we don't care about out, we just want exit status,
     * we still need to drain the output
     */
    var process = sshClient.sudoCommand(cmd);
    while( process.readLine() != null);
    var val = process.getExitCode();
    return val;
}

function execCommand(cmd) {
    /* For commands where we don't care about out, we just want exit status,
     * we still need to drain the output
     */
    var process = sshClient.executeCommand(cmd);
    while( process.readLine() != null);
    var val = process.getExitCode();
    return val;
}

function parsePamVal(str) {
    var i = str.indexOf('=');
    if(i != -1) {
        return parseInt(str.substring(i + 1).trim());
    }
    return -1;
}

function parseChageVal(str) {
    var i = str.indexOf(':');
    if(i != -1) {
        return str.substring(i + 1).trim();
    }
    return "";
}

function fetchIdentities() {
    users.clear();
    var process = sshClient.executeCommand('cat /etc/passwd');
    while(line = process.readLine()) {
        try {
            var identity = parseIdentity(line);
            var uid = parseInt(identity.getGuid());
            if(uid >= minUid && uid <= maxUid) {
	            users.add(identity);
	
	            // Parse primary group
	            var elements = line.split(":");
	            var gid = elements[3];
	            var role = roleMap.get(gid);
	            if(role) {
	                identity.addRole(role);
	            }
	            
	            // Parse secondary groups
	            var userRoles = userRoleMap.get(identity.getPrincipalName());
	            if(userRoles) {
	                for(i = 0 ; i < userRoles.size(); i++) {
	                    identity.addRole(userRoles.get(i));
	                }
	            }
	
	            loadLastLogin(identity);
	            loadExpiry(identity);
            }
        }
        catch(e) {
            log.error('Error. '  +e.message);
        }
    }
}

function fetchRoles() {
    userRoleMap.clear();
    roleMap.clear();
    roles.clear();
    var process = sshClient.executeCommand('cat /etc/group');
    while(line = process.readLine()) {
        try {
            var role = parseRole(line);
            roleMap.put(role.getGuid(), role);
            var elements = line.split(":");
            if(elements.length > 3) {
                var users = elements[3].split(':');
                for(i = 0 ; i < users.length; i++) {
                    var usersRoles = userRoleMap.get(users[i]);
                    if(!usersRoles) {
                        usersRoles = new ArrayList();
                        userRoleMap.put(users[i], usersRoles);
                    }
                    usersRoles.add(role);
                }
            }
            roles.add(role);
        }
        catch(e) {
            log.error('Error. '  +e.message);
        }
    }
    log.debug('Scanned group file');
}

function parseRole(line) {
    var elements = line.split(":");
    return new RoleImpl(elements[2], elements[0]);
}

function parseIdentity(line) {
    /* Parses a line from /etc/passwd and creates and IdentityImpl object
     * from it. 
     */
    
    var elements = line.split(":");
    var impl = new IdentityImpl(elements[2], elements[0]);
    
    // The service account should be marked as 'system'
    if(elements[0].equals(config.getServiceAccountUsername())) {
        impl.setSystem(true);
    }

    impl.setAttribute("userName", elements[0]);
    impl.setAttribute("home", elements[5]);
    impl.setAttribute("shell", elements[6]);

    // http://en.wikipedia.org/wiki/Gecos_field
    var gecosVal = elements[4] + '';
    var gecos = gecosVal.split(",");
    if(gecos.length == 4) { 
        impl.setFullName(gecos[0]);     
        impl.setAttribute("fullName", gecos[0]);
        impl.setAttribute("building", gecos[1]);
        impl.setAttribute("officePhone", gecos[2]);
        
        // The final field may have commas, so rebuild the split string
        var otherContact = '';
        for(var gi = 3; gi < gecos.length; gi++) {
            if(otherContact.length > 0) {
                otherContact += ',';
            }
            otherContact += gecos[gi];
        }
        impl.setAttribute("otherContact", otherContact);
    }
    else {  
        impl.setFullName(elements[4]);      
        impl.setAttribute("fullName", elements[4]);
    }
    
    return impl;
}

function loadExpiry(identity) {
    // Account Locked status is actually password locked status and comes from passwd command
    var process = sshClient.sudoCommand('passwd -S "' + identity.getPrincipalName() + '"');
    line = process.readLine();
    var locked = false;
    if(line && line.split(" ")[1] == 'L') {
        identity.getAccountStatus().setLocked(new java.util.Date(0));
        locked = true;
    }   
    
    // Account Expiry and others come from chage command 
    process = sshClient.sudoCommand('chage -l "' + identity.getPrincipalName() + '"');
    var dateFormat = new SimpleDateFormat('MMM dd, yyyyy');
    while(line = process.readLine()) {
        try {
            if(line.startsWith("Last password change")) {
                var v = parseChageVal(line);
                if(v == "password must be changed") {
                    identity.getPasswordStatus().setNeedChange(true);
                }
                else if(v != "never") {
                    identity.getPasswordStatus().setLastChange(dateFormat.parse(v));
                }
            }
            else if(line.startsWith("Password expires")) {
                var v = parseChageVal(line);
                if(v != "never" && v != "password must be changed") {
                    identity.getPasswordStatus().setExpire(dateFormat.parse(v));
                }
            }
            else if(line.startsWith("Account expires")) {
                var v = parseChageVal(line);
                if(v != "never" && v != "password must be changed") {
                    identity.getAccountStatus().setExpire(dateFormat.parse(v));
                    
                    /*
                     * Unix does not have an actual "disabled" status, the recommendation is
                     * to set it to locked and expired. So, we calculate the final status now,
                     * and set the disabled flag if locked and expired
                     */ 
                    identity.getAccountStatus().calculateType();
                    if(identity.getAccountStatus().getType().equals(AccountStatusType.expired) && locked) {
                        identity.getAccountStatus().setDisabled(true);
                    }
                }
            }
            else if(line.startsWith("Minimum number of days between password change")) {
                var lastChange = identity.getPasswordStatus().getLastChange();
                if(lastChange == null) {
                    lastChange = new java.util.Date(0);
                }
                identity.getPasswordStatus().setUnlocked(new java.util.Date(lastChange.getTime() + 
                        Util.daysToMillis(Number(parseChageVal(line)))));
            }
            else if(line.startsWith("Number of days of warning before password expires")) {
                var lastChange = identity.getPasswordStatus().getLastChange();
                if(lastChange == null) {
                    lastChange = new java.util.Date(0);
                }
                identity.getPasswordStatus().setWarn(new java.util.Date(lastChange.getTime() - 
                        Util.daysToMillis(Number(parseChageVal(line)))));
            }
            else if(line.startsWith("Maximum number of days between password change")) {
                var lastChange = identity.getPasswordStatus().getLastChange();
                if(lastChange == null) {
                    lastChange = new java.util.Date(0);
                }
                identity.getPasswordStatus().setWarn(new java.util.Date(lastChange.getTime() + 
                        Util.daysToMillis(Number(parseChageVal(line)))));
            }
            else if(line.startsWith("Password inactive")) {
                var v = parseChageVal(line);
                if(v != "never") {
                    identity.getPasswordStatus().setDisable(dateFormat.parse(v));
                }
            }
            
            identity.getPasswordStatus().calculateType();
            identity.getAccountStatus().calculateType();
            
            
        }
        catch(e) {
            log.error('Error. '  +e.message);
        }
    }
}

function loadLastLogin(identity) {
    var process = sshClient.executeCommand('lastlog -u "' + identity.getPrincipalName() + '"');
    var dateFormat = new SimpleDateFormat('EEE MMM dd HH:mm:ss ZZZZZ yyyyy');
    try {
        var line = process.readLine();
        if(line == null) {
            return;
        } 
        line = process.readLine();
        if(line && line.length() > 41) {
            s = line.substring(42).trim();
            if(!s.startsWith('**')) {
                try {
                    identity.setLastSignOnDate(dateFormat.parse(s));
                }
                catch(e) {
                }
            }
        }
    }
    finally {
        while( process.readLine() != null);
    }
}
