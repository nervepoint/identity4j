/* HEADER */
package com.identity4j.connector;

import java.io.Closeable;

/*
 * #%L
 * Identity4J Connector
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


import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.SocketFactory;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.passwords.PasswordCharacteristics;

/**
 * <p>
 * Implementations of this interface will provide basic identity and role
 * related services such as retrieving a identity account, retrieving a role,
 * authenticating a identity etc.
 * 
 * <p>
 * Some implementations will now support account creation or password changing
 * and as such should return appropriate values for
 * {@link #supportsAccountCreation()} and {@link #supportsPasswordChange()}.
 */
public interface Connector<P extends ConnectorConfigurationParameters> extends Closeable {
    
    /**
     * Used as a hint to {@link Connector#setPassword(Principal, char[], boolean, PasswordResetType)} and
     * {@link Connector#setPassword(String, String, char[], boolean, PasswordResetType)} to tell the 
     * connector what sort of reset it is. For example, Active Directory could use this to determine if
     * to attempt to enforce password on resets
     *
     */
    public enum PasswordResetType {
        /**
         * The reset is administrative. For example, should ignore password history
         */
        ADMINISTRATIVE, 
        /**
         * The reset is on behalf of a user, and additional restrictions should apply where
         * possible. E.g Active Directory might try to apply password history. 
         */
        USER
    }

    /**
     * Get the capabilities. Return <code>null</code> if the capabilities are no different
     * than the default returned by the {@link ConnectorConfigurationParameters} implementation.
     *  
     * @return 
     */
	Set<ConnectorCapability> getCapabilities();
	
	/**
	 * Get the default password policy for this connector. If the connector
	 * cannot provide this information, <code>null</code> will be returned.
	 * 
	 * @return default password policy
	 */
	PasswordCharacteristics getPasswordCharacteristics();
	
	
	/**
	 * Get all the available password policies for this connector.
	 * @return
	 */
	Iterator<? extends PasswordCharacteristics> getPasswordPolicies();

	/**
	 * Authenticates the given credentials, returning the identity if
	 * successful.
	 * 
	 * @param username
	 * @param password
	 * @return Identity
	 * @throws PrincipalNotFoundException
	 * @throws InvalidLoginCredentialsException
	 * @throws ConnectorException
	 */
	Identity logon(String username, char[] password) throws PrincipalNotFoundException, InvalidLoginCredentialsException,
			ConnectorException;

	/**
	 * Start authenticating a user using an authentication API.
	 * 
	 * @param username
	 * @param password
	 * @return <tt>true</tt> is returned on success and <tt>false</tt> on
	 *         failure.
	 * @throws ConnectorException
	 */
	WebAuthenticationAPI startAuthentication() throws ConnectorException;

	/**
	 * Check the given credentials but do not actually logon.
	 * 
	 * @param username
	 * @param password
	 * @return <tt>true</tt> is returned on success and <tt>false</tt> on
	 *         failure.
	 * @throws ConnectorException
	 */
	boolean checkCredentials(String username, char[] password, IdentityProcessor... processors) throws ConnectorException;

	/**
	 * Change your password. This method is used by an identity to change their
	 * own password.
	 * 
	 * @param guid
	 * @param oldPassword
	 * @param password
	 * @throws InvalidLoginCredentialsException
	 * @throws PrincipalNotFoundException
	 * @throws ConnectorException
	 */
	void changePassword(String username, String guid, char[] oldPassword, char[] password) throws InvalidLoginCredentialsException,
			PrincipalNotFoundException, ConnectorException;

	/**
	 * Set a {@link Identity}s password. This is used by an administrator.
	 * 
	 * @param guid
	 * @param password
	 * @param forcePasswordChangeAtLogon
	 * @throws InvalidLoginCredentialsException
	 * @throws PrincipalNotFoundException
	 * @throws ConnectorException
	 */
	void setPassword(String username, String guid, char[] password, boolean forcePasswordChangeAtLogon)
			throws InvalidLoginCredentialsException, PrincipalNotFoundException, ConnectorException;

	/**
	 * Set a {@link Identity}s password. This is used by an administrator.
	 * 
	 * @param guid
	 * @param password
	 * @param forcePasswordChangeAtLogon
	 * @param resetType
	 * @throws InvalidLoginCredentialsException
	 * @throws PrincipalNotFoundException
	 * @throws ConnectorException
	 */
	void setPassword(String username, String guid, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType resetType)
			throws InvalidLoginCredentialsException, PrincipalNotFoundException, ConnectorException;

	/**
	 * @return {@link Identity}s
	 * @throws ConnectorException
	 */
	Iterator<Identity> allIdentities() throws ConnectorException;
	
	/**
	 * @param tag tag, used to retrieve results changed since last known tag. The
	 * returned specialisation of {@link Iterator} will supply the new tag.
	 * 
	 * @return {@link Identity}s
	 * @throws ConnectorException
	 */
	ResultIterator<Identity> allIdentities(String tag) throws ConnectorException;

	/**
	 * Count identities.
	 * 
	 * @return
	 * @throws ConnectorException
	 */
	long countIdentities() throws ConnectorException;

	/**
	 * Count identities.
	 * 
	 * @param tag tag, used to retrieve results changed since last known tag. The
	 * returned {@link Count} will supply the new tag.
	 * @return
	 * @throws ConnectorException
	 */
	Count<Long> countIdentities(String tag) throws ConnectorException;

	/**
	 * Does the {@link Identity} name already belong to another {@link Identity}
	 * ?
	 * 
	 * @param identityName
	 * @return <tt>true</tt> if an {@link Identity} already matches the supplied
	 *         {@link Identity} name.
	 * @throws ConnectorException
	 */
	boolean isIdentityNameInUse(String identityName) throws ConnectorException;

	/**
	 * Get the account details that belong to the given {@link Identity}
	 * identity name.
	 * 
	 * @param identityName
	 * @return identity
	 * @throws PrincipalNotFoundException if the identity could not be found
	 * @throws ConnectorException on all other errors
	 */
	Identity getIdentityByName(String identityName) throws PrincipalNotFoundException, ConnectorException;

	/**
	 * Get an account from its GUID.
	 * 
	 * @param identityGuid
	 * @return
	 * @throws PrincipalNotFoundException
	 * @throws ConnectorException
	 */
	Identity getIdentityByGuid(String identityGuid) throws PrincipalNotFoundException, ConnectorException;
	
	/**
	 * @return Roles
	 * @throws ConnectorException
	 */
	Iterator<Role> allRoles() throws ConnectorException;

	/**
	 * @param tag tag, used to retrieve results changed since last known tag. The
	 * returned specialisation of {@link Iterator} will supply the new tag.
	 * @return Roles
	 * @throws ConnectorException
	 */
	ResultIterator<Role> allRoles(String tag) throws ConnectorException;

	/**
	 * Count identities.
	 * 
	 * @return
	 * @throws ConnectorException
	 */
	long countRoles() throws ConnectorException;
	
	/**
	 * Count identities.
	 * 
	 * @param tag tag, used to retrieve results changed since last known tag. The
	 * returned {@link Count} will supply the new tag.
	 * @return
	 * @throws ConnectorException
	 */
	Count<Long> countRoles(String tag) throws ConnectorException;
	
	/**
	 * Does the role name already belong to another role?
	 * 
	 * @param roleName
	 * @return <tt>true</tt> if a role already matches the supplied role name.
	 * @throws ConnectorException
	 */
	boolean isRoleNameInUse(String roleName) throws ConnectorException;

	/**
	 * Get a single role given its name
	 * 
	 * @param roleName
	 * @return role
	 * @throws PrincipalNotFoundException
	 * @throws ConnectorException
	 */
	Role getRoleByName(String roleName) throws PrincipalNotFoundException, ConnectorException;

	/**
	 * @param parameters
	 */
	void open(P parameters);

	/**
	 * Get the configuration used for this connector, as provided in {@link #open(ConnectorConfigurationParameters)}.
	 * @param parameters
	 */
	P getConfiguration();

	/**
	 * Close and re-open the connector, using the same configuration as was used
	 * on when originally opening.
	 */
	void reopen();

	/**
	 * Get if the connector is open.
	 * 
	 * @return open
	 */
	boolean isOpen();

	/**
	 * 
	 * @return
	 */
	boolean isReadOnly();

	/**
	 * Create an identity in remote system
	 * 
	 * @param identity
	 * @param password
	 * @return new identity
	 * @throws ConnectorException
	 */
	Identity createIdentity(Identity identity, char[] password) throws ConnectorException;
	
	
	/**
	 * Default implementation simply calls PasswordCreationCallback and passes result into createIdentity(Identity,char[])
	 * 
	 * @param identity
	 * @param passwordCallback
	 * @throws ConnectorException
	 */
	public Identity createIdentity(Identity identity, PasswordCreationCallback passwordCallback, boolean forceChange) throws ConnectorException;
	
	/**
	 * Update an identity's details.
	 * 
	 * @param identity identity to update
	 * @throws ConnectorException
	 */
	void updateIdentity(Identity identity) throws ConnectorException;

	/**
	 * Delete an identity form remote system
	 * 
	 * @param principleName
	 * @throws ConnectorException
	 */
	void deleteIdentity(String principleName) throws ConnectorException;

	/**
	 * Create a role in remote system
	 * 
	 * @param identity
	 * @param password
	 * @return new identity
	 * @throws ConnectorException
	 */
	Role createRole(Role role) throws ConnectorException;

	/**
	 * Update an role's details.
	 * 
	 * @param role role to update
	 * @throws ConnectorException
	 */
	void updateRole(Role role) throws ConnectorException;

	/**
	 * Delete a role form remote system
	 * 
	 * @param principleName
	 * @throws ConnectorException
	 */
	void deleteRole(String principleName) throws ConnectorException;

	/**
	 * Lock an identity
	 * 
	 * @param identity identity of account to lock
	 * @throws UnsupportedOperationExcetion if not support
	 */
	void lockIdentity(Identity identity);

	/**
	 * Unlock an identity
	 * 
	 * @param identity identity of account to unlock
	 * @throws UnsupportedOperationExcetion if not supported
	 */
	void unlockIdentity(Identity identity);

	/**
	 * Disable an identity
	 * 
	 * @param identity identity of account to disable
	 * @throws UnsupportedOperationExcetion if not support
	 */
	void disableIdentity(Identity identity);

	/**
	 * Enable an identity
	 * 
	 * @param identity identity of account to enable
	 * @throws UnsupportedOperationExcetion if not supported
	 */
	void enableIdentity(Identity identity);

	/**
	 * If supported by the connector, this method should configure the backend
	 * so as to best support Nervepoint. For example, in the case of Active
	 * Directory, this should install an object into the Active Directory so
	 * clients may locate the Nervepoint server to perform password resets over
	 * the remote access API.
	 * 
	 * @param properties any connector specific properties that may be required.
	 * @throws UnsupportedOperationExcetion if not supported
	 */
	void install(Map<String, String> properties) throws Exception;

	/**
	 * Set the {@link SocketFactory} to use for this connector (if the connector
	 * uses sockets and supports this feature).
	 * 
	 * @param socketFactory socket factory
	 */
	void setSocketFactory(SocketFactory socketFactory);
	
	/**
	 * Get an attribute previously set on this connector.
	 * @param name
	 * @return
	 */
	Object getAttribute(String name);
	
	/**
	 * Store a value against the connector.
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, Object value);

}