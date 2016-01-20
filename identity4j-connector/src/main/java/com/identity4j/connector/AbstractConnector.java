/* HEADER */
package com.identity4j.connector;

import java.util.Iterator;
import java.util.Map;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.passwords.PasswordCharacteristics;
import com.identity4j.util.validator.ValidationContext;

public abstract class AbstractConnector implements Connector, ValidationContext {

	private ConnectorConfigurationParameters parameters;

	public PasswordCharacteristics getPasswordCharacteristics() {
		throw new UnsupportedOperationException();
	}
	
	public Iterator<? extends PasswordCharacteristics> getPasswordPolicies() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WebAuthenticationAPI<?> startAuthentication() throws ConnectorException {
		throw new UnsupportedOperationException("This connector does not support integrated web authentication.");
	}

	@Override
	public long countIdentities() throws ConnectorException {
		return count(allIdentities());
	}

	@Override
	public long countRoles() throws ConnectorException {
		return count(allRoles());
	}

	protected long count(Iterator<? extends Principal> it) {
		long count = 0;
		for (; it.hasNext(); count++) {
			it.next();
		}
		return count;
	}

	public final Identity logon(String username, char[] password) throws PrincipalNotFoundException,
			InvalidLoginCredentialsException, ConnectorException {
		Identity identity = getIdentityByName(username);
		if (identity == null) {
			throw new InvalidLoginCredentialsException("Invalid username or password: '" + username + "'");
		}
		assertValidCredentials(identity, password);
		return identity;
	}

	private void assertValidCredentials(Identity identity, char[] password) throws ConnectorException,
			InvalidLoginCredentialsException {
		if (!areCredentialsValid(identity, password)) {
			throw new InvalidLoginCredentialsException("Invalid username or password: '" + identity.getPrincipalName() + "'");
		}
	}

	public final boolean checkCredentials(String username, char[] password) throws ConnectorException {
		if (!isIdentityNameInUse(username)) {
			return false;
		}
		Identity identity = getIdentityByName(username);
		return areCredentialsValid(identity, password);
	}

	/**
	 * Checks that the supplied credentials are valid for authentication
	 * 
	 * @param identity
	 * @param password
	 * @return <tt>true</tt> if the credentials are valid
	 * @throws ConnectorException
	 */
	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		throw new UnsupportedOperationException("Are credentials valid is not supported");
	}

	public final void changePassword(String username, String guid, char[] oldPassword, char[] password)
			throws InvalidLoginCredentialsException, ConnectorException {
		Identity identity = getIdentityByName(username);
		assertGuid(identity, guid);
		try {
			assertValidCredentials(identity, oldPassword);
			assertPasswordChangeIsAllowed(identity, oldPassword, password);
		}
		catch(PasswordChangeRequiredException pcre) {
			// Not really surprising :)
		}
		
		changePassword(identity, oldPassword, password);
	}

	/**
	 * Default implementation simply delegates to
	 * {@link #setPassword(Identity, char[], boolean, PasswordResetType)}. Most connectors won't
	 * need to override this, but some (including Active Directory) work
	 * differently when the logged on as the actual identity whose password is
	 * being changed.
	 * 
	 * @param identity
	 * @param oldPassword
	 * @param password
	 */
	protected void changePassword(Identity identity, char[] oldPassword, char[] password) {
		setPassword(identity, password, false, PasswordResetType.USER);
	}

	protected void assertPasswordChangeIsAllowed(Identity identity, char[] oldPassword, char[] password) throws ConnectorException {
		// no implementation by default
	}


	public final void setPassword(String username, String guid, char[] password, boolean forcePasswordChangeAtLogon)
			throws PrincipalNotFoundException, InvalidLoginCredentialsException, ConnectorException {
		setPassword(username, guid, password, forcePasswordChangeAtLogon, PasswordResetType.ADMINISTRATIVE);
	}

	public final void setPassword(String username, String guid, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType resetType)
			throws PrincipalNotFoundException, InvalidLoginCredentialsException, ConnectorException {
		Identity identity = getIdentityByName(username);
		assertGuid(identity, guid);
		setPassword(identity, password, forcePasswordChangeAtLogon, resetType);
	}

	@Deprecated
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon) throws ConnectorException {
		throw new UnsupportedOperationException("Set password is not supported");
	}

	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType type) throws ConnectorException {
		setPassword(identity, password, forcePasswordChangeAtLogon);
	}

	protected final void assertGuid(Principal principal, String guid) {
		if (!principal.getGuid().equals(guid)) {
			throw new PrincipalNotFoundException("Principal '" + principal.getPrincipalName()
				+ "' found but GUID does not match: expected = '" + principal.getGuid() + "' actual = '" + guid + "'");
		}
	}

	public final boolean isIdentityNameInUse(String identityName) throws ConnectorException {
		try {
			getIdentityByName(identityName);
			return true;
		} catch (PrincipalNotFoundException infe) {
			return false;
		}
	}

	public final boolean isRoleNameInUse(String rolename) throws ConnectorException {
		try {
			getRoleByName(rolename);
			return true;
		} catch (PrincipalNotFoundException rnfe) {
			return false;
		}
	}

	/**
	 * Very inefficient default implementation. It is highly recommended
	 * sub-classes override this.
	 * 
	 * @param name
	 * @return identity
	 * @see {@link Connector#getIdentityByName(String)}
	 * @throws PrincipalNotFoundException
	 * @throws {@link ConnectorException}
	 */
	public Identity getIdentityByName(String name) throws PrincipalNotFoundException, ConnectorException {
		for (Iterator<Identity> identityIterator = allIdentities(); identityIterator.hasNext();) {
			Identity identity = identityIterator.next();
			if (identity.getPrincipalName().equals(name)) {
				return identity;
			}
		}
		throw new PrincipalNotFoundException(name + " not found.");
	}

	/**
	 * Very inefficient default implementation. It is highly recommended
	 * sub-classes override this.
	 * 
	 * @param name
	 * @return role
	 * @see {@link Connector#getIdentityByName(String)}
	 * @throws PrincipalNotFoundException
	 * @throws {@link ConnectorException}
	 */
	public Role getRoleByName(String name) throws PrincipalNotFoundException, ConnectorException {
		for (Iterator<Role> roleIterator = allRoles(); roleIterator.hasNext();) {
			Role role = roleIterator.next();
			if (role.getPrincipalName().equals(name)) {
				return role;
			}
		}
		throw new PrincipalNotFoundException(name + " not found.");
	}

	protected final <T extends Principal> T getPrincipal(String filter, Iterator<T> itr) {
		if (itr.hasNext()) {
			return itr.next();
		}
		throw new PrincipalNotFoundException("Principal not found for filter '" + filter + "'");
	}

	/**
	 * Default implementation simply returns null. Need to override this to
	 * create new identity
	 * 
	 * @param identity
	 * @param password
	 * @throws ConnectorException
	 */
	public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
		throw new UnsupportedOperationException("Create identity is not supported");
	}

	/**
	 * Default implementation simply returns null. Need to override this to
	 * update a identity
	 * 
	 * @param identity identity to update
	 * @throws ConnectorException
	 */
	public void updateIdentity(Identity identity) throws ConnectorException {
		throw new UnsupportedOperationException("Update identity is not supported");
	}

	/**
	 * Default implementation. Need to override this to delete a identity
	 * 
	 * @param identity identity to delete
	 * @throws ConnectorException
	 */
	public void deleteIdentity(String principalName) throws ConnectorException {
		throw new UnsupportedOperationException("Delete identity is not supported");
	}

	/**
	 * Default implementation. Need to override this to delete a identity
	 * 
	 * @param roleName role to delete
	 * @throws ConnectorException
	 */
	public void deleteRole(String principalName) throws ConnectorException {
		throw new UnsupportedOperationException("Delete role is not supported");
	}
	
	public Role createRole(Role role) throws ConnectorException {
		throw new UnsupportedOperationException("Create role is not supported");
	}

	@Override
	public void updateRole(Role role) throws ConnectorException {
		throw new UnsupportedOperationException("Update role is not supported");
	}
	
	public void lockIdentity(Identity identity) throws ConnectorException {
		throw new UnsupportedOperationException("Lock account is not supported");
	}

	@Override
	public void disableIdentity(Identity identity) {
		throw new UnsupportedOperationException("Disable account is not supported");
	}

	@Override
	public void enableIdentity(Identity identity) {
		throw new UnsupportedOperationException("Enable account is not supported");
	}

	public void unlockIdentity(Identity identity) throws ConnectorException {
		throw new UnsupportedOperationException("Unlock account is not supported");
	}

	public void open(ConnectorConfigurationParameters parameters) throws ConnectorException {
		this.parameters = parameters;
		onOpen(parameters);
		if (!isOpen()) {
			throw new ConnectorException("Connector should be open but was closed");
		}
	}

	protected abstract void onOpen(ConnectorConfigurationParameters parameters) throws ConnectorException;

	public void reopen() throws ConnectorException {
		close();
		open(parameters);
	}

	public void close() {
		onClose();
	}

	protected void onClose() {
		// nothing to do on close
	}

	@Override
	public void install(Map<String, String> properties) throws Exception {
		throw new UnsupportedOperationException();
	}
}