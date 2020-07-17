/* HEADER */
package com.identity4j.connector;

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


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.SocketFactory;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.passwords.PasswordCharacteristics;
import com.identity4j.util.validator.ValidationContext;

public abstract class AbstractConnector<P extends ConnectorConfigurationParameters> implements Connector<P>, ValidationContext {

	private P parameters;
	private Map<String,Object> attributes = new HashMap<String,Object>();
	
	public PasswordCharacteristics getPasswordCharacteristics() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ResultIterator<Identity> allIdentities(String tag) throws ConnectorException {
		Iterator<Identity> it = allIdentities();
		return new ResultIterator<Identity>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Identity next() {
				return it.next();
			}

			@Override
			public String tag() {
				/* Tag not support, return null */
				return null;
			}
		};
	}

	@Override
	public ResultIterator<Role> allRoles(String tag) throws ConnectorException {
		Iterator<Role> it = allRoles();
		return new ResultIterator<Role>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Role next() {
				return it.next();
			}

			@Override
			public String tag() {
				/* Tag not support, return null */
				return null;
			}
		};
	}

	@Override
	public void setSocketFactory(SocketFactory socketFactory) {
		throw new UnsupportedOperationException();
	}

	public Iterator<? extends PasswordCharacteristics> getPasswordPolicies() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WebAuthenticationAPI startAuthentication() throws ConnectorException {
		throw new UnsupportedOperationException("This connector does not support integrated web authentication.");
	}

	@Override
	public long countIdentities() throws ConnectorException {
		return count(allIdentities());
	}

	@Override
	public Count<Long> countIdentities(String tag) throws ConnectorException {
		/* No tags supported by default so return null */ 
		return new Count<>(countIdentities(), null);
	}

	@Override
	public long countRoles() throws ConnectorException {
		return count(allRoles());
	}

	@Override
	public Count<Long> countRoles(String tag) throws ConnectorException {
		/* No tags supported by default so return null */ 
		return new Count<>(countRoles(), null);
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

	public boolean supportsOptimisedCheckCredentials() {
		return false;
	}
	
	public boolean checkCredentialsOptimised(String username, String remoteIdentifier, char[] password) throws ConnectorException {
		throw new UnsupportedOperationException();
	}
	
	public final boolean checkCredentials(String username, char[] password, IdentityProcessor... processors) throws ConnectorException {
		
		/**
		 * Optimised by LDP to only retrieve Identity once.
		 */
		try {
			Identity identity = getIdentityByName(username);
			boolean valid = areCredentialsValid(identity, password);
			if(valid) {
				for(IdentityProcessor processor : processors) {
					processor.processIdentity(identity, this);
				}
			}
			return valid;
		} catch (PrincipalNotFoundException e) {
			return false;
		}
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
			System.out.println(">> " + identity.getPrincipalName() + "/" + identity.getGuid() + " against " + name);
			if (identity.getPrincipalName().equals(name)) {
				return identity;
			}
		}
		throw new PrincipalNotFoundException(name + " not found.");
	}

	/**
	 * Very inefficient default implementation. It is highly recommended sub-classes override this.
	 */
	@Override
	public Identity getIdentityByGuid(String guid) throws PrincipalNotFoundException, ConnectorException {
		for (Iterator<Identity> identityIterator = allIdentities(); identityIterator.hasNext();) {
			Identity identity = identityIterator.next();
			System.out.println(">> " + identity.getPrincipalName() + "/" + identity.getGuid() + " against " + guid);
			if (identity.getGuid().equals(guid)) {
				return identity;
			}
		}
		throw new PrincipalNotFoundException(guid + " not found.");
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
	 * create new identity
	 * 
	 * @param identity
	 * @param password
	 * @throws ConnectorException
	 */
	public Identity createIdentity(Identity identity, PasswordCreationCallback passwordCallback, boolean forceChange) throws ConnectorException {
		Identity newIdentity = createIdentity(identity, null);
		setPassword(newIdentity.getPrincipalName(), newIdentity.getGuid(), passwordCallback.createPassword(newIdentity), forceChange);
		return newIdentity;
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

	public final void open(P parameters) throws ConnectorException {
		this.parameters = parameters;
		onOpen(parameters);
		if (!isOpen()) {
			throw new ConnectorException("Connector should be open but was closed");
		}
	}
	
	public final P getConfiguration() {
		return parameters;
	}

	protected abstract void onOpen(P parameters) throws ConnectorException;

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
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public void setAttribute(String name, Object val) {
		attributes.put(name, val);
	}
}