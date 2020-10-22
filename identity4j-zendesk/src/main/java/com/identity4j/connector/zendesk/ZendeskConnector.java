package com.identity4j.connector.zendesk;

/*
 * #%L
 * Identity4J Zendesk
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.zendesk.entity.Group;
import com.identity4j.connector.zendesk.entity.Groups;
import com.identity4j.connector.zendesk.entity.User;
import com.identity4j.connector.zendesk.entity.Users;
import com.identity4j.connector.zendesk.services.Directory;
import com.identity4j.connector.zendesk.services.token.handler.ZendeskAuthorizationHelper;
import com.identity4j.util.CollectionUtil;
import com.identity4j.util.passwords.PasswordCharacteristics;

/**
 * Zendesk connector makes use of Zendesk REST API to perform admin operations.
 * Connector enables CRUD operations on Users and can map them to Groups.
 * Users and Groups are referred as ZendeskIdentity and Role respectively in identity4j domain.
 * 
 * <p>
 * To map properties of User not supported by ZendeskIdentity we can make use attributes map.
 * <pre>
 * E.g. user.setAttribute("id", ".........");
 * </pre>
 * Here we are using attribute map with key "id" to store profile id of user.
 * </p>
 * 
 * <p>
 * The API can be referred from <a href="http://developer.zendesk.com/documentation/rest_api/introduction.html">Zendesk REST API</a>
 * </p>
 * 
 * @author gaurav
 *
 */
public class ZendeskConnector extends AbstractConnector<ZendeskConfiguration> {


	private Directory directory;
	private static final Log log = LogFactory.getLog(ZendeskConnector.class);
	
	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(Arrays.asList(new ConnectorCapability[] { 
			ConnectorCapability.passwordChange,
			ConnectorCapability.passwordSet,
			ConnectorCapability.createUser,
			ConnectorCapability.deleteUser,
			ConnectorCapability.updateUser,
			ConnectorCapability.hasFullName,
			ConnectorCapability.hasEmail,
			ConnectorCapability.roles,
			ConnectorCapability.createRole,
			ConnectorCapability.deleteRole,
			ConnectorCapability.updateRole,
			ConnectorCapability.authentication,
			ConnectorCapability.identities,
			ConnectorCapability.accountDisable
	}));

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}

	/**
	 * Check to see if connector is open
	 */
	@Override
	public boolean isOpen() {
		return directory != null;
	}

	/**
	 * Check to see connector if in read only mode
	 */
	@Override
	public boolean isReadOnly() {
		return false;
	}
	

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
		return null;
	}
	
	/**
	 * <p>
	 * Retrieves all the roles (groups) present in the Zendesk.
	 * <br/>
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	public Iterator<Role> allRoles() throws ConnectorException {
		Groups groups = directory.groups().all();
		List<Group> groupList = groups.getGroups();
		List<Role> roles = new ArrayList<Role>();
		
		if(groupList != null){
			for (Group group : groupList) {
				roles.add(ZendeskModelConvertor.getInstance().groupToRole(group));
			}
		}
		return roles.iterator();
	}

	/**
	 * <p>
	 * Creates a role in the Zendesk.
	 * <br/>
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalAlreadyExistsException if role with same email id/principal already present in Zendesk.
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	public Role createRole(Role role) throws ConnectorException {
		/**
		 * The Zendesk API fails to throw exception when role name is already in use.
		 */
		if(directory.groups().checkGroupExists(role.getPrincipalName())){
			throw new PrincipalAlreadyExistsException("Role name already exists " + role.getPrincipalName());
		}
		
		Group group = ZendeskModelConvertor.getInstance().roleToGroup(role);
		group = directory.groups().save(group);
		return ZendeskModelConvertor.getInstance().groupToRole(group);
	}

	
	/**
	 * <p>
	 * Updates a role in the Zendesk with specified changes.
	 * <br/>
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if role specified by email id/principal name not present in Zendesk.
	 * @throws ConnectorException for api, connection related errors or delete privilege not given to service id.
	 */
	@Override
	public void updateRole(Role role) throws ConnectorException {
		Group group = ZendeskModelConvertor.getInstance().roleToGroup(role);
		directory.groups().update(group);
	}

	/**
	 * <p>
	 * Deletes a role in the Zendesk with specified changes.
	 * <br/>
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if role specified by email id/principal name not present in Zendesk.
	 * @throws ConnectorException for api, connection related errors or delete privilege not given to service id.
	 */
	@Override
	public void deleteRole(String principleName) throws ConnectorException {
		Group group = directory.groups().getByName(principleName);
		directory.groups().delete(group.getId());
	}
	
	
	/**
	 * <p>
	 * Finds an identity by principal name supplied.
	 * <p>
	 * The extra attributes supported by graph e.g. role, organization_id are populated in identites's attributes map.
	 * <pre>
	 * identity.getAttribute("organization_id")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html">User REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if identity specified by principal name not present in Zendesk.
	 * @throws ConnectorException for api, connection related errors.
	 * 
	 * @return Identity instance found by the specified principal name.
	 */
	@Override
	public Identity getIdentityByName(String name)
			throws PrincipalNotFoundException, ConnectorException {
		User user = directory.users().getByName(name);
		return ZendeskModelConvertor.getInstance().convertZendeskUserToZendeskIdentity(user);
	}
	
	
	/**
	 * <p>
	 * Creates identity with specified roles and password provided.
	 * Role in Zendesk api is known as Group and is identified by unique guid.
	 * Role guid is auto generated number from Zendesk API.
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html">User REST operations</a>.
	 * </p>
	 * 
	 * <p>
	 * The extra attributes supported by graph e.g. role, organization_id are populated in identites's attributes map.
	 * <pre>
	 * identity.getAttribute("organization_id")
	 * </pre>
	 * </p>
	 * 
	 * @throws PrincipalAlreadyExistsException if identity with same email id/principal already present in Zendesk.
	 * @throws ConnectorException for api, connection related errors.
	 * 
	 * @return Identity instance with values specified for creation.
	 */
	@Override
	public Identity createIdentity(Identity identity, char[] password)
			throws ConnectorException {
		User user = ZendeskModelConvertor.getInstance().convertZendeskIdentityToZendeskUser((ZendeskIdentity) identity);
		user.setPassword(new String(password));
		
		user = directory.users().save(user);
		
		Identity identitySaved = ZendeskModelConvertor.getInstance().convertZendeskUserToZendeskIdentity(user);
		return identitySaved;
	}
	
	/**
	 * <p>
	 * Updates an identity in Zendesk.
	 * To update extra attributes supported by Zendesk, use attributes map.
	 * <pre>
	 * e.g. identity.setAttribute("organization_id","123..")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html">User REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in Zendesk.
	 * @throws ConnectorException for api, connection related errors or delete privilege not given to service id.
	 */
	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		User user = ZendeskModelConvertor.getInstance().convertZendeskIdentityToZendeskUser((ZendeskIdentity) identity);
		
		directory.users().update(user);
		adjustAdditionRemovalOfRoleOnIdentityUpdate(identity);
	}
	
	/**
	 * <p>
	 * Deletes an identity in Zendesk datastore.
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html">User REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in data store.
	 * @throws ConnectorException for api, connection related errors or delete privilege not given to service id.
	 */
	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
		User userToBeDeleted = directory.users().getByName(principalName);
		directory.users().delete(userToBeDeleted.getId());
	}
	
	
	/**
	 * <p>
	 * Finds all identities present in Zendesk.
	 * <p>
	 * The extra attributes supported by graph e.g. role, organization_id are populated in identites's attributes map.
	 * <pre>
	 * identity.getAttribute("organization_id")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html">User REST operations</a>.
	 * </p>
	 * 
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 * 
	 * @return iterator with all identities.
	 */
	@Override
	public Iterator<Identity> allIdentities() throws ConnectorException {
		Users users = directory.users().all();
		List<Identity> identities = new ArrayList<Identity>();
		
		List<User> userList = users.getUsers();
		
		if(userList != null){
			for (User user : userList) {
				identities.add(ZendeskModelConvertor.getInstance().convertZendeskUserToZendeskIdentity(user));
			}
		}
		
		return identities.iterator();
	}
	
	/**
	 * Disables/Suspends an account in Zendesk datastore.
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in Zendesk datastore.
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	public void disableIdentity(Identity identity) {
		identitySuspensionHelper(identity,true);
		identity.getAccountStatus().setDisabled(true);
	}
	
	/**
	 * Enables an account in Zendesk datastore.
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in Zendesk datastore.
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	public void enableIdentity(Identity identity) {
		identitySuspensionHelper(identity,false);
		identity.getAccountStatus().setDisabled(false);
	}
	
	/**
	 * <p>
	 * Checks credential provided are valid or not.
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="https://support.zendesk.com/entries/24458591-Using-OAuth-authentication-with-your-application">Zendesk oAuth Flow</a>.
	 * </p>
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 * 
	 * @return true if credentials are correct else false
	 */
	@Override
	protected boolean areCredentialsValid(Identity identity, char[] password)
			throws ConnectorException{
		return directory.users().areCredentialsValid(identity.getPrincipalName(), password);
	}
	
	
	/**
	 * Changes the password of the identity specified by email id/principal.
	 * Also provides provision to force change password in next logon attempt.
	 * <br />
	 * <b>Note</b> <i>Force change password in next logon attempt is not supported by Zendesk, value passed will be ignored</i>
	 * 
	 * 
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html#set-a-user%27s-password">User set password</a>.
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType type) throws ConnectorException {
		try {
			directory.users().setPassword(Integer.parseInt(identity.getGuid()), new String(password));
		} catch (IOException e) {
			throw new ConnectorException("Problem in setting password.", e);
		}
	}
	
	/**
	 * Helper utility method to enable/disable suspension for an identity
	 * 
	 * @param identity
	 * @param suspension true to suspend an account, false to enable it
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in active directory.
	 * @throws ConnectorException for api, connection related errors.
	 */
	private void identitySuspensionHelper(Identity identity,boolean suspension) {
		directory.users().suspend(Integer.parseInt(identity.getGuid()),suspension);
	}


	/**
	 * <p>
	 * Creates directory instance for remote method invocations using Zendesk REST API.
	 * The directory instance is created by providing and enabling
	 * <ul>
	 * 	<li>Client id.</li>
	 *  <li>Client secret.</li>
	 *  <li>Zendesk oAuth url with client subdomain</li>
	 * </ul>
	 * </p>
	 */
	@Override
	protected void onOpen(ZendeskConfiguration parameters)
			throws ConnectorException {
		try {
			
			ZendeskAuthorizationHelper.getInstance()
			.setClientId(parameters.getClientId())
			.setClientSecret(parameters.getClientSecret())
			.setoAuthUrl(parameters.getOAuthUrl())
			.setPasswordAccessJSON(parameters.getOAuthPasswordAccessJSON())
			.setScope(parameters.getoAuthScope())
			.setSubDomain(parameters.getSubDomain());
			
			directory = Directory.getInstance();
			
			log.info("Directory instance created.");
		
			directory.init(parameters);
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Helper utility method to adjust addition and removal of roles from an identity.
	 * It compares the roles currently assigned and new set of roles sent and finds which are to be added and which are to 
	 * be removed and accordingly performs removal or addition action.
	 * 
	 * @param identity
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	private void adjustAdditionRemovalOfRoleOnIdentityUpdate(Identity identity){
		try{
			Identity identityFromSource = getIdentityByGuid(identity);
			
			Set<Role> rolesCurrentlyAssigned = new HashSet<Role>(Arrays.asList(identityFromSource.getRoles()));
			Set<Role> rolesToBeAssigned = new HashSet<Role>(Arrays.asList(identity.getRoles()));
			
			Collection<Role> newRolesToAdd = CollectionUtil.objectsNotPresentInProbeCollection(rolesToBeAssigned, rolesCurrentlyAssigned);
			Collection<Role> rolesToRemove = CollectionUtil.objectsNotPresentInProbeCollection(rolesCurrentlyAssigned,rolesToBeAssigned);
			
			for (Role role : newRolesToAdd) {
				addRoleToUser(role.getGuid(), identity.getGuid());
			}
			
			for (Role role : rolesToRemove) {
				removeRoleFromUser(role.getGuid(), identity.getGuid());
			}
		}catch(Exception e){
			log.error("Problem in adjusting roles " + e.getMessage(), e);
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	/**
	 * Removes a Role(Group) from an identity.
	 * <br/>
	 * <b>Refer groups by guid as all operations on groups are performed using only guid.</b>
	 * <br/>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @param roleName
	 * @param principal
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	private void removeRoleFromUser(String guidRole, String guidUser) {
		directory.groups().removeUserFromGroup(Integer.parseInt(guidUser),Integer.parseInt(guidRole));
		
	}

	/**
	 * Adds a Role(Group) to an identity.
	 * <br/>
	 * <b>Refer groups by guid as all operations on groups are performed using only guid.</b>
	 * <br/>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @param roleName
	 * @param principal
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	private void addRoleToUser(String guidRole, String guidUser) {
		directory.groups().addUserToGroup(Integer.parseInt(guidUser),Integer.parseInt(guidRole));
		
	}
	
	/**
	 * Finds Identity by GUID
	 * 
	 * @param identity
	 * @return identity matching GUID
	 * 
	 * @throws PrincipalNotFoundException if identity by GUID not present in datastore
	 */
	private ZendeskIdentity getIdentityByGuid(Identity identity) {
		return ZendeskModelConvertor.getInstance()
				.convertZendeskUserToZendeskIdentity(
						directory.users().getByGuid(
								Integer.parseInt(identity.getGuid())));
	}
	
}
