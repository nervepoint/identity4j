package com.identity4j.connector.zendesk.services;

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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.connector.zendesk.entity.Group;
import com.identity4j.connector.zendesk.entity.GroupMembership;
import com.identity4j.connector.zendesk.entity.GroupMemberships;
import com.identity4j.connector.zendesk.entity.User;
import com.identity4j.connector.zendesk.entity.Users;
import com.identity4j.connector.zendesk.services.token.handler.Token;
import com.identity4j.connector.zendesk.services.token.handler.ZendeskAuthorizationHelper;
import com.identity4j.util.StringUtil;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.json.JsonMapperService;

/**
 * This class is responsible for managing REST calls for User entity.
 * 
 * @author gaurav
 *
 */
public class UserService extends AbstractRestAPIService{
	
	private static final Log log = LogFactory.getLog(UserService.class);
	
	private GroupService groupService;
	
	UserService(HttpRequestHandler httpRequestHandler,
			ZendeskConfiguration serviceConfiguration,GroupService groupService) {
		super(httpRequestHandler, serviceConfiguration);
		this.groupService = groupService;
	}

	/**
	 * This method retrieves an instance of User corresponding to provided guid.
	 * If user is not found in data store it throws PrincipalNotFoundException
	 * 
	 * @param guid
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public User getByGuid(Integer guid){
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI(String.format("users/%s", guid)), getHeaders().toArray(new HttpPair[0]));
		try {
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(guid + " not found.",null,PrincipalType.user);
			}
			
			@SuppressWarnings("unchecked")
			Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "user");
			
			if(records.isEmpty()){
				throw new PrincipalNotFoundException(guid + " not found.",null,PrincipalType.user);
			}
			
			User user = JsonMapperService.getInstance().convert(records, User.class);
			
			GroupMemberships groupMemberships = groupService.getGroupMembershipsForUser(user.getId());
			user.setGroupMemberships(groupMemberships);
			return user;
		}
		finally {
			response.release();
		}
		
	}
	
	
	/**
	 * This method retrieves an instance of User corresponding to provided user email.
	 * If user is not found in data store it throws PrincipalNotFoundException
	 * <br/>
	 * This method makes use of <b>Zendesk Search API</b> for fetching User.
	 * 
	 * @param email
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public User getByName(String email){
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI("search","query=type:user email:" + email), getHeaders().toArray(new HttpPair[0]));
		try {
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(email + " not found.",null,PrincipalType.user);
			}
			
			@SuppressWarnings("unchecked")
			List<String> records = (List<String>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "results");
			
			if(records == null || records.isEmpty()){
				throw new PrincipalNotFoundException(email + " not found.",null,PrincipalType.user);
			}
			
			User user = JsonMapperService.getInstance().convert(records.get(0), User.class);
			
			GroupMemberships groupMemberships = groupService.getGroupMembershipsForUser(user.getId());
			user.setGroupMemberships(groupMemberships);
			return user;
		}
		finally {
			response.release();
		}
		
	}
	
	
	/**
	 * This method retrieves all users present in the data store.
	 * <br />
	 * This method makes use of <b>Zendesk Search API</b> for fetching Users.
	 * 
	 * @return users list
	 */
	public Users all(){
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI("search","query=type:user"), getHeaders().toArray(new HttpPair[0]));
		try {
			return JsonMapperService.getInstance().getObject(Users.class, response.contentString());
		}
		finally {
			response.release();
		}
	}

	/**
	 * Saves user into zendesk data store.
	 * <br/>
	 * If you need to create users without sending out a verification email, pass a "verified": true parameter.
	 * 
	 * @param user
	 * @throws PrincipalAlreadyExistsException if user by same principal name exists.
	 * @throws ConnectorException for possible json mapping exceptions
	 * 
	 */
	public User save(User user) {
		//we cannot send password as attribute while creation of user
		//hence we cache it and nullify it in user POJO
		String password = user.getPassword();
		user.setPassword(null);
		try{
			String json = String.format("{\"user\":  %s}", JsonMapperService.getInstance().getJson(user));
			HttpResponse response = httpRequestHandler.handleRequestPost(constructURI("users"),json, getHeaders().toArray(new HttpPair[0]));
	
			if(response.status().getCode() != 201){
				if(response.contentString().contains(String.format("Email %s is already being used by another user", user.getEmail()))){
					throw new PrincipalAlreadyExistsException("Principal already exists by email " + user.getEmail());
				}
				
				throw new ConnectorException("Problem in creating principal reason : " + response.contentString());
			}
			
			@SuppressWarnings("unchecked")
			Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "user");
			User userPersisted = JsonMapperService.getInstance().convert(records, User.class);
			
			setPassword(userPersisted.getId(), password);
			
			userPersisted.setPassword(password);//json returned does not have password as field
			userPersisted.setGroupMemberships(new GroupMemberships());
			
			if(user.getGroupMemberships() != null)
				userPersisted.getGroupMemberships().setGroupMemberships(handleGroupMembers(user.getGroupMemberships().getGroupMemberships(),userPersisted.getId()));
			
			
			return userPersisted;
		}catch(IOException e){
			throw new ConnectorException("Problem in saving user " + user.getName(),e);
		}finally{
			user.setPassword(password);
		}
	}
	
	
	/**
	 * Updates user properties sent for update.
	 * <br />
	 * For finding user to update it makes use of guid.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the user by object id not found in datastore.
	 * @throws ConnectorException for service related exception.
	 */
	public User update(User user){
		//we cannot send password as attribute while creation of user
		//hence we cache it and nullify it in user POJO
		String password = user.getPassword();
		user.setPassword(null);
		try{
			String json = String.format("{\"user\":  %s}", JsonMapperService.getInstance().getJson(user));
			HttpResponse response = httpRequestHandler.handleRequestPut(constructURI(String.format("users/%s",user.getId())),json, getHeaders().toArray(new HttpPair[0]));
			
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(user.getId() + " not found.",null,PrincipalType.user);
			}
			
			if(response.status().getCode() != 200){
				throw new ConnectorException("Problem in updating user as status code is not 200 is " 
							+ response.status().getCode() + " " + response.contentString());
			}
			
			@SuppressWarnings("unchecked")
			Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "user");
			User userUpdated = JsonMapperService.getInstance().convert(records, User.class);
			userUpdated.setPassword(user.getPassword());
			
			return userUpdated;
			
		} catch (IOException e) {
			throw new ConnectorException("Problem in updating user " + user.getName(),e);
		}finally{
			user.setPassword(password);
		}
	}
	
	
	
	/**
	 * Deletes user by specified id.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the user by object id not found in datastore.
	 * @throws ConnectorException for service related exception.
	 */
	public User delete(Integer id) {
		HttpResponse response = httpRequestHandler.handleRequestDelete(constructURI(String.format("users/%d", id)), getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() == 404){
			throw new PrincipalNotFoundException(id + " not found.",null,PrincipalType.user);
		}
		
		if(response.status().getCode() != 200){
			throw new ConnectorException("Problem in deleting user as status code is not 200 is " 
						+ response.status().getCode() + " " + response.contentString());
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "user");
		return JsonMapperService.getInstance().convert(records, User.class);
		
	}
	
	/**
	 * Suspends a user present in datastore.
	 * 
	 * @param id of the user who is to be suspended
	 * @param suspend boolean value, true if to be suspended
	 * 
	 * @throws PrincipalNotFoundException if the user by object id not found in datastore.
	 * @throws ConnectorException for service related exception.
	 */
	public User suspend(Integer id,boolean suspend){
		String json = String.format("{\"user\": {\"suspended\":%s}}", suspend);
		HttpResponse response = httpRequestHandler.handleRequestPut(constructURI(String.format("users/%d",id)),json, getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() == 404){
			throw new PrincipalNotFoundException(id + " not found.",null,PrincipalType.user);
		}
		
		if(response.status().getCode() != 200){
			throw new ConnectorException("Problem in updating user as status code is not 200 is " 
						+ response.status().getCode() + " " + response.contentString());
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "user");
		return JsonMapperService.getInstance().convert(records, User.class);
	}
	
	/**
	 * Checks credentials of user.
	 * 
	 * @param identity
	 * @param password
	 * @return true if credentials are valid
	 */
	public boolean areCredentialsValid(String principalName, char[] password){
		Token token = null;
		try{
			token = ZendeskAuthorizationHelper.getInstance()
			.getOAuthAccessToken(principalName,new String(password));
		}catch(Exception e){
			log.error("Login failed " + e.getMessage(), e);
			return false;
		}
		return token != null && !StringUtil.isNullOrEmpty(token.getAccessToken());
	}
	
	/**
	 * Sets a user's password, allowed for Admin users only.
	 * <br />
	 * <b>An admin can set a user's password only if the setting is enabled under Settings > Security > Global.
	 *  The setting is off by default.
	 *  An admin can set a new password for another user without knowing the existing password
	 * </b>
	 *  
	 * 
	 * @param id
	 * @param newPassword
	 * @throws IOException 
	 */
	public void setPassword(Integer id,String newPassword) throws IOException{
		String json = String.format("{\"password\": \"%s\"}", newPassword);
		passwordChangeHelper(id, json);
	}

	/**
	 * Changes a user's password.
	 * <p>
	 * You can only change your own password. 
	 * Nobody can change the password of another user because it requires knowing the user's existing password.
	 * </p>
	 * 
	 * @param id
	 * @param oldPassword
	 * @param newPassword
	 */
	public void changePassword(Integer id,String oldPassword,String newPassword){
		String json = String.format("{\"previous_password\": \"%s\", \"password\": \"%s\"}", oldPassword,newPassword);
		passwordChangeHelper(id, json);
	}
	
	/**
	 * Helper utility method to act on password change and setting.
	 * 
	 * @param id
	 * @param json
	 * 
	 * @throws PrincipalNotFoundException if the user by object id not found in datastore.
	 * @throws ConnectorException for service related exception.
	 */
	private void passwordChangeHelper(Integer id, String json) {
		HttpResponse response = httpRequestHandler.handleRequestPost(constructURI(String.format("users/%d/password", id)),json,getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() == 404){
			throw new PrincipalNotFoundException(id + " not found.",null,PrincipalType.user);
		}
		
		if(response.status().getCode() != 200){
			throw new ConnectorException("Problem in setting password for user as status code is not 200 is " 
						+ response.status().getCode() + " " + response.contentString());
		}
	}
	
	/**
	 * Maps group members to user i.e. user is added to supplied groups.
	 * 
	 * @param members
	 * @param userId
	 * @return
	 */
	private List<GroupMembership> handleGroupMembers(List<GroupMembership> members,Integer userId) {
		
		List<GroupMembership> createdMemberships = new ArrayList<GroupMembership>();
		
		Group group = null;
		GroupMembership createdGroupMemberships = null;
		for (GroupMembership groupMembership : members) {
			//group member addition does not give any hint of principal not found
			//hence we need to check for it explicitly
			group = groupService.getByName(groupMembership.getGroup().getName());
			createdGroupMemberships = groupService.addUserToGroup(userId, group.getId());
			createdGroupMemberships.setGroup(groupMembership.getGroup());
			createdMemberships.add(createdGroupMemberships);
		}
		
		return createdMemberships;
	}
}
