package com.identity4j.connector.salesforce;

/*
 * #%L
 * Identity4J Salesforce
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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.OperationContext;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.salesforce.entity.Group;
import com.identity4j.connector.salesforce.entity.Groups;
import com.identity4j.connector.salesforce.entity.User;
import com.identity4j.connector.salesforce.entity.Users;
import com.identity4j.connector.salesforce.services.Directory;
import com.identity4j.connector.salesforce.services.token.handler.SalesforceAuthorizationHelper;
import com.identity4j.util.CollectionUtil;
import com.identity4j.util.passwords.PasswordCharacteristics;

/**
 * Salesforce connector makes use of Salesforce REST API to perform admin operations.
 * Connector enables CRUD operations on Users and can map them to Groups.
 * Users and Groups are referred as SalesforceIdentity and Role respectively in identity4j domain.
 * 
 * <p>
 * To map properties of User not supported by SalesforceIdentity we can make use attributes map.
 * <pre>
 * E.g. user.setAttribute("ProfileId", ".........");
 * </pre>
 * Here we are using attribute map with key "ProfileId" to store profile id of user.
 * </p>
 * 
 * <p>
 * The API can be referred from <a href="http://www.salesforce.com/us/developer/docs/api_rest/">Salesforce REST API</a>
 * </p>
 * 
 * @author gaurav
 *
 */
public class SalesforceConnector extends AbstractConnector<SalesforceConfiguration> {


	private Directory directory;
	private static final Log log = LogFactory.getLog(SalesforceConnector.class);
	
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
			ConnectorCapability.accountDisable,
			ConnectorCapability.identityAttributes,
			ConnectorCapability.roleAttributes
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
	 * Retrieves all the roles (groups) present in the salesforce.
	 * <br/>
	 * <b>Note:</b> Role in salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	public ResultIterator<Role> allRoles(OperationContext opContext) throws ConnectorException {
		Groups groups = directory.groups().all();
		List<Group> groupList = groups.getGroups();
		List<Role> roles = new ArrayList<Role>();
		
		if(groupList != null){
			for (Group group : groupList) {
				roles.add(SalesforceModelConvertor.getInstance().groupToRole(group));
			}
		}
		return ResultIterator.createDefault(roles.iterator(), opContext.getTag());
	}

	/**
	 * <p>
	 * Creates a role in the salesforce.
	 * <br/>
	 * <b>Note:</b> Role in salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalAlreadyExistsException if role with same email id/principal already present in salesforce.
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	public Role createRole(Role role) throws ConnectorException {
		/**
		 * The salesforce API fails to throw exception when role name is already in use.
		 * The API generates unique name by itself, hence we need to explicitly check for it.
		 */
		if(directory.groups().groupAlreadyExists(role.getPrincipalName())){
			throw new PrincipalAlreadyExistsException("Role already exists " + role.getPrincipalName());
		}
		
		Group group = SalesforceModelConvertor.getInstance().roleToGroup(role);
		group = directory.groups().save(group);
		return SalesforceModelConvertor.getInstance().groupToRole(group);
	}

	
	/**
	 * <p>
	 * Updates a role in the salesforce with specified changes.
	 * <br/>
	 * <b>Note:</b> Role in salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if role specified by email id/principal name not present in salesforce.
	 * @throws ConnectorException for api, connection related errors or delete privilege not given to service id.
	 */
	@Override
	public void updateRole(Role role) throws ConnectorException {
		Group group = SalesforceModelConvertor.getInstance().roleToGroup(role);
		directory.groups().update(group);
	}

	/**
	 * <p>
	 * Deletes a role in the salesforce with specified changes.
	 * <br/>
	 * <b>Note:</b> Role in salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if role specified by email id/principal name not present in salesforce.
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
	 * The extra attributes supported by graph e.g. CompanyName, Department are populated in identites's attributes map.
	 * <pre>
	 * identity.getAttribute("Department")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if identity specified by principal name not present in salesforce.
	 * @throws ConnectorException for api, connection related errors.
	 * 
	 * @return Identity instance found by the specified principal name.
	 */
	@Override
	public Identity getIdentityByName(String name)
			throws PrincipalNotFoundException, ConnectorException {
		User user = directory.users().getByName(name);
		Identity identity = SalesforceModelConvertor.getInstance().convertSalesforceUserToSalesforceIdentity(user);
		return identity;
	}
	
	
	/**
	 * <p>
	 * Creates identity with specified roles and password provided.
	 * Role in Salesforce api is known as Group and is identified by unique guid.
	 * Role guid is auto generated number from Salesforce API.
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * <p>
	 * The extra attributes supported by graph e.g. CompanyName, Department are populated in identites's attributes map.
	 * <pre>
	 * identity.getAttribute("Department")
	 * </pre>
	 * </p>
	 * 
	 * @throws PrincipalAlreadyExistsException if identity with same email id/principal already present in salesforce.
	 * @throws ConnectorException for api, connection related errors.
	 * 
	 * @return Identity instance with values specified for creation.
	 */
	@Override
	public Identity createIdentity(Identity identity, char[] password)
			throws ConnectorException {
		User user = SalesforceModelConvertor.getInstance().convertSalesforceIdentityToSalesforceUser((SalesforceIdentity) identity);
		user.setPassword(new String(password));
		
		user = directory.users().save(user);
		
		Identity identitySaved = SalesforceModelConvertor.getInstance().convertSalesforceUserToSalesforceIdentity(user);
		return identitySaved;
	}
	
	/**
	 * <p>
	 * Updates an identity in salesforce.
	 * To update extra attributes supported by salesforce, use attributes map.
	 * <pre>
	 * e.g. identity.setAttribute("Department","Engineering")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in salesforce.
	 * @throws ConnectorException for api, connection related errors or delete privilege not given to service id.
	 */
	@Override
	public void updateIdentity(Identity identity) throws ConnectorException {
		User user = SalesforceModelConvertor.getInstance().convertSalesforceIdentityToSalesforceUser((SalesforceIdentity) identity);
		
		directory.users().update(user);
		adjustAdditionRemovalOfRoleOnIdentityUpdate(identity);
	}
	
	/**
	 * Saesforce API does not provide a delete operation.
	 * <br/>
	 * Only de activation of principal is supported.
	 * <br/>
	 * <b>Note:</b>Method is overridden to by pass super method call.
	 */
	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
	}
	
	
	/**
	 * <p>
	 * Finds all identities present in salesforce.
	 * <p>
	 * The extra attributes supported by graph e.g. CompanyName, Department are populated in identites's attributes map.
	 * <pre>
	 * identity.getAttribute("Department")
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 * 
	 * @return iterator with all identities.
	 */
	@Override
	public ResultIterator<Identity> allIdentities(OperationContext opContext) throws ConnectorException {
		Users users = directory.users().all();
		List<Identity> identities = new ArrayList<Identity>();
		
		List<User> userList = users.getUsers();
		
		if(userList != null){
			for (User user : userList) {
				identities.add(SalesforceModelConvertor.getInstance().convertSalesforceUserToSalesforceIdentity(user));
			}
		}
		
		return ResultIterator.createDefault(identities.iterator(), opContext.getTag());
	}
	
	/**
	 * Disables/Suspends an account in Salesforce datastore.
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in Salesforce datastore.
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	public void disableIdentity(Identity identity) {
		identitySuspensionHelper(identity,true);
		identity.getAccountStatus().setDisabled(true);
	}
	
	/**
	 * Enables an account in Salesforce datastore.
	 * 
	 * @throws PrincipalNotFoundException if identity specified by email id/principal name not present in Salesforce datastore.
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
	 * <br>
	 * Password verification depends on IP range hint in configuration.
	 * <br/>
	 * If machine IP running code is in range, only password is needed else secret key has to be appended
	 * with password.
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
	 * <b>Note</b> <i>Force change password in next logon attempt is not supported by Salesforce, value passed will be ignored</i>
	 * 
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType type) throws ConnectorException {
		User user = new User();
		user.setPassword(new String(password));
		user.setId(identity.getGuid());
		directory.users().handlePasswordSetting(user);
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
		User user = new User();
		user.setIsActive(!suspension);
		user.setId(identity.getGuid());
		directory.users().update(user);
	}


	/**
	 * <p>
	 * Creates directory instance for remote method invocations using Salesforce REST API.
	 * The directory instance is created by providing and enabling
	 * <ul>
	 * 	<li>Admin user id.</li>
	 *  <li>Admin user password.</li>
	 *  <li>Admin user secret key, if server on which application is running is not in trusted IP list in Salesforce console</li>
	 * </ul>
	 * </p>
	 */
	@Override
	protected void onOpen(SalesforceConfiguration parameters)
			throws ConnectorException {
		
		SalesforceAuthorizationHelper.getInstance()
		.setIpRangeOrAppIpLessRestrictive(parameters.getIpRangeOrAppIpLessRestrictive())
		.setLoginSoapEnvelopTemplate(parameters.getLoginSoapEnvelopTemplate())
		.setLoginSoapUrl(parameters.getLoginSoapUrl())
		.setVersion(parameters.getRestApiVersion());
		
		SalesforceModelConvertor.getInstance().init(parameters);
		
		directory = Directory.getInstance();
		
		log.info("Directory instance created.");
		try {
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
			Identity identityFromSource = getIdentityByName(identity.getPrincipalName());
			
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
	 * Please refer <a href="http://msdn.microsoft.com/en-us/library/dn151610.aspx">Groups</a>.
	 * </p>
	 * 
	 * @param roleName
	 * @param principal
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	private void removeRoleFromUser(String guidRole, String guidUser) {
		directory.groups().removeUserFromGroup(guidUser,guidRole);
		
	}

	/**
	 * Adds a Role(Group) to an identity.
	 * <br/>
	 * <b>Refer groups by guid as all operations on groups are performed using only guid.</b>
	 * <br/>
	 * Please refer <a href="http://msdn.microsoft.com/en-us/library/dn151610.aspx">Groups</a>.
	 * </p>
	 * 
	 * @param roleName
	 * @param principal
	 * 
	 * @throws ConnectorException for api, connection related errors.
	 */
	private void addRoleToUser(String guidRole, String guidUser) {
		directory.groups().addUserToGroup(guidUser,guidRole);
		
	}
	
}
