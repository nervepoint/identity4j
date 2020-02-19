package com.identity4j.remote;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.SocketFactory;

import org.freedesktop.dbus.exceptions.DBusException;

import com.identity4j.connector.Connector;
import com.identity4j.connector.ConnectorBuilder;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.Count;
import com.identity4j.connector.IdentityProcessor;
import com.identity4j.connector.PasswordCreationCallback;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.WebAuthenticationAPI;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.Role;
import com.identity4j.remote.dbus.ConnectorBuilderDBus;
import com.identity4j.remote.dbus.ConnectorDBus;
import com.identity4j.remote.dbus.DBusIdentity;
import com.identity4j.util.MultiMap;
import com.identity4j.util.passwords.PasswordCharacteristics;

public class RemoteConnector implements Connector<RemoteConnectorConfiguration> {

	private ConnectorBuilderDBus builder;
	private long id;
	private RemoteConnectorConfiguration parameters;
	private ConnectorDBus connector;

	public RemoteConnector() {
	}

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<? extends PasswordCharacteristics> getPasswordPolicies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identity logon(String username, char[] password)
			throws PrincipalNotFoundException, InvalidLoginCredentialsException, ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebAuthenticationAPI startAuthentication() throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkCredentials(String username, char[] password, IdentityProcessor... processors)
			throws ConnectorException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changePassword(String username, String guid, char[] oldPassword, char[] password)
			throws InvalidLoginCredentialsException, PrincipalNotFoundException, ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPassword(String username, String guid, char[] password, boolean forcePasswordChangeAtLogon)
			throws InvalidLoginCredentialsException, PrincipalNotFoundException, ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPassword(String username, String guid, char[] password, boolean forcePasswordChangeAtLogon,
			PasswordResetType resetType)
			throws InvalidLoginCredentialsException, PrincipalNotFoundException, ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<Identity> allIdentities() throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultIterator<Identity> allIdentities(String tag) throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countIdentities() throws ConnectorException {
		return connector.CountIdentities();
	}

	@Override
	public Count<Long> countIdentities(String tag) throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isIdentityNameInUse(String identityName) throws ConnectorException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Identity getIdentityByName(String identityName) throws PrincipalNotFoundException, ConnectorException {
		DBusIdentity map = connector.GetIdentityByName(identityName);
		IdentityImpl idImpl = new IdentityImpl(map.PrincipalName());
		return idImpl;
	}

	@Override
	public Iterator<Role> allRoles() throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultIterator<Role> allRoles(String tag) throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countRoles() throws ConnectorException {
		return connector.CountRoles();
	}

	@Override
	public Count<Long> countRoles(String tag) throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRoleNameInUse(String roleName) throws ConnectorException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Role getRoleByName(String roleName) throws PrincipalNotFoundException, ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void open(RemoteConnectorConfiguration parameters) {
		try {
			this.parameters = parameters;
			builder = parameters.getDBus().getRemoteObject(ConnectorBuilderDBus.class.getPackage().getName(), "/ConnectorBuilder",
					ConnectorBuilderDBus.class);
			MultiMap prms = new MultiMap(parameters.getConfigurationParameters());
			if(!prms.containsKey(ConnectorBuilder.CONFIGURATION_CLASS)) {
				Class<?> clazz = parameters.getDelegate().getClass();
				if(clazz == null)
					throw new IllegalArgumentException("Cannot determine remote connector configuration class.");
				prms.put(ConnectorBuilder.CONFIGURATION_CLASS, new String[] { clazz.getName() });
			}
			id = builder.Create(MultiMap.toMap(prms));
			connector = parameters.getDBus().getRemoteObject(ConnectorBuilderDBus.class.getPackage().getName(), "/Connector/" + id, ConnectorDBus.class);
		} catch (DBusException dbe) {
			throw new ConnectorException(dbe);
		}
	}

	@Override
	public void close() throws IOException {
		builder.Destroy(id);
	}

	@Override
	public void reopen() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Identity createIdentity(Identity identity, char[] password) throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identity createIdentity(Identity identity, PasswordCreationCallback passwordCallback, boolean forceChange)
			throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIdentity(String principleName) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public Role createRole(Role role) throws ConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateRole(Role role) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteRole(String principleName) throws ConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void lockIdentity(Identity identity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockIdentity(Identity identity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableIdentity(Identity identity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableIdentity(Identity identity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void install(Map<String, String> properties) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketFactory(SocketFactory socketFactory) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public RemoteConnectorConfiguration getConfiguration() {
		return parameters;
	}

}
