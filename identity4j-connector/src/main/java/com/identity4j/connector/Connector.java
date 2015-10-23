/* HEADER */
package com.identity4j.connector;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
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
public interface Connector {

	Set<ConnectorCapability> getCapabilities();
	
	/**
	 * Get the default password policy for this connector. If the connector
	 * cannot provide this information, <code>null</code> will be returned.
	 * 
	 * @return default password policy
	 */
	PasswordCharacteristics getPasswordCharacteristics();

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
	WebAuthenticationAPI<? extends ConnectorConfigurationParameters> startAuthentication() throws ConnectorException;

	/**
	 * Check the given credentials but do not actually logon.
	 * 
	 * @param username
	 * @param password
	 * @return <tt>true</tt> is returned on success and <tt>false</tt> on
	 *         failure.
	 * @throws ConnectorException
	 */
	boolean checkCredentials(String username, char[] password) throws ConnectorException;

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
	 * @return {@link Identity}s
	 * @throws ConnectorException
	 */
	Iterator<Identity> allIdentities() throws ConnectorException;

	/**
	 * Count identities.
	 * 
	 * @return
	 * @throws ConnectorException
	 */
	long countIdentities() throws ConnectorException;

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
	 * @return Roles
	 * @throws ConnectorException
	 */
	Iterator<Role> allRoles() throws ConnectorException;

	/**
	 * Count identities.
	 * 
	 * @return
	 * @throws ConnectorException
	 */
	long countRoles() throws ConnectorException;

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
	void open(ConnectorConfigurationParameters parameters);

	/**
	 * Close the connector, releasing any resources currently in use.
	 */
	void close();

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
}