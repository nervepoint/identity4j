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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.zendesk.ZendeskConfiguration;
import com.identity4j.connector.zendesk.entity.Group;
import com.identity4j.connector.zendesk.entity.GroupMembership;
import com.identity4j.connector.zendesk.entity.GroupMemberships;
import com.identity4j.connector.zendesk.entity.Groups;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.json.JsonMapperService;

/**
 * 
 * This class is responsible for managing REST calls for Group entity.
 * 
 * @author gaurav
 *
 */
public class GroupService extends AbstractRestAPIService{

	GroupService(HttpRequestHandler httpRequestHandler,
			ZendeskConfiguration serviceConfiguration) {
		super(httpRequestHandler, serviceConfiguration);
	}

	
	/**
	 * This method retrieves an instance of Group corresponding to provided guid.
	 * If group is not found in data store it throws PrincipalNotFoundException.
	 * 
	 * @param guid
	 * @return Group entity
	 * @throws PrincipalNotFoundException if principal by guid not present in data source
	 */
	public Group getByGuid(Integer guid) {

		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI(String.format("groups/%d", guid)),
				getHeaders().toArray(new HttpPair[0]));
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance()
					.getJsonProperty(response.contentString(), "group");

			if (records == null || records.isEmpty()) {
				throw new PrincipalNotFoundException(guid + " not found.", null, PrincipalType.role);
			}

			return JsonMapperService.getInstance().convert(records, Group.class);
		} finally {
			response.release();
		}
		
	}
	
	/**
	 * This method retrieves an instance of Group corresponding to provided group name.
	 * If group is not found in data store it throws PrincipalNotFoundException
	 * <br /> 
	 * This method makes use of <b>Zendesk Search API</b> for fetching group.
	 * 
	 * @param name
	 * @return Group entity
	 * @throws PrincipalNotFoundException if principal by guid not present in data source
	 */
	public Group getByName(String name){
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI("search","query=type:group name:" + name), getHeaders().toArray(new HttpPair[0]));
		try {
		
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(name + " not found.",null,PrincipalType.role);
			}
			
			@SuppressWarnings("unchecked")
			List<String> records = (List<String>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "results");
			
			if(records == null || records.isEmpty()){
				throw new PrincipalNotFoundException(name + " not found.",null,PrincipalType.role);
			}
			
			return JsonMapperService.getInstance().convert(records.get(0), Group.class);
		}
		finally {
			response.release();
		}
	}
	
	/**
	 * This method retrieves all groups present in the data store.
	 * <br />
	 * This method makes use of <b>Zendesk Search API</b> for fetching Groups.
	 * 
	 * @return groups list
	 */
	public Groups all(){
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI("search", "query=type:group"),
				getHeaders().toArray(new HttpPair[0]));
		try {
			return JsonMapperService.getInstance().getObject(Groups.class, response.contentString());
		} finally {
			response.release();
		}
	}
	
	
	/**
	 * Saves group into Zendesk datastore.
	 * 
	 * @param group
	 * @throws PrincipalAlreadyExistsException if group by same principal name exists.
	 * @throws ConnectorException for possible json mapping exceptions or network exceptions
	 * @return
	 * @throws IOException 
	 */
	public Group save(Group group) {
		try{
			String json = String.format("{\"group\":  %s}", JsonMapperService.getInstance().getJson(group));
			HttpResponse response = httpRequestHandler.handleRequestPost(constructURI("groups"),json, getHeaders().toArray(new HttpPair[0]));
	
			if(response.status().getCode() != 201){
				throw new ConnectorException("Problem in creating principal reason : " + response.contentString());
			}
			
			@SuppressWarnings("unchecked")
			Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "group");
			return JsonMapperService.getInstance().convert(records, Group.class);
		}catch(IOException e){
			throw new ConnectorException("Problem in saving group " + group.getName(), e);
		}
	}
	
	
	/**
	 * Updates group properties sent for update.
	 * <br />
	 * For finding group to update it makes use of guid.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the user by object id not found in Zendesk.
	 * @throws ConnectorException for service related exception.
	 */
	public void update(Group group){
		try{
			String json = String.format("{\"group\":  %s}", JsonMapperService.getInstance().getJson(group));
			HttpResponse response = httpRequestHandler.handleRequestPut(
					constructURI(String.format("groups/%s", group.getId()),
							null), json, getHeaders().toArray(new HttpPair[0]));
			
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(group.getId() + " not found.",null,PrincipalType.role);
			}
			
			if(response.status().getCode() != 200){
				throw new ConnectorException("Problem in updating group as status code is not 200 is " 
							+ response.status().getCode() + " " + response.contentString());
			}
			
		} catch (IOException e) {
			throw new ConnectorException("Problem in updating user",e);
		}
	}
	
	/**
	 * Deletes group by specified id.
	 * 
	 * @param id
	 * @throws PrincipalNotFoundException if the user by object id not found in Zendesk.
	 * @throws ConnectorException for service related exception.
	 */
	public void delete(Integer id) {
		HttpResponse response = httpRequestHandler.handleRequestDelete(constructURI(String.format("groups/%d", id)), getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() == 404){
			throw new PrincipalNotFoundException(id + " not found.",null,PrincipalType.user);
		}
		
		if(response.status().getCode() != 200){
			throw new ConnectorException("Problem in deleting group as status code is not 200 is " 
						+ response.status().getCode() + " " + response.contentString());
		}
		
	}
	
	/**
	 * Adds user to group.
	 * <br />
	 * <b>Note:</b> Only agents can be added to or removed from a group.
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/group_memberships.html"> Group Membership </a>
	 * REST API for more details.
	 * 
	 * @param userId
	 * @param groupId
	 * @throws ConnectorException for service related exception.
	 * @return GroupMembership entity representing relation between user and group.
	 */
	public GroupMembership addUserToGroup(Integer userId,Integer groupId){
		try{
			GroupMembership groupMembership = new GroupMembership();
			groupMembership.setUserId(userId);
			groupMembership.setGroupId(groupId);
			
			String json = String.format("{\"group_membership\": %s}",JsonMapperService.getInstance().getJson(groupMembership));
			
			
			HttpResponse response = httpRequestHandler.handleRequestPost(constructURI("group_memberships"),json, getHeaders().toArray(new HttpPair[0]));
			
			if(response.status().getCode() != 201){
				throw new ConnectorException(
						"Problem in adding group member "
								+ response.status().getCode() + " : " + response.contentString());
			}
	
			@SuppressWarnings("unchecked")
			Map<String, Object> records = (Map<String, Object>) JsonMapperService.getInstance().getJsonProperty(
					response.contentString(), "group_membership");
			return JsonMapperService.getInstance().convert(records, GroupMembership.class);
		}catch(IOException e){
			throw new ConnectorException("Problem in saving group membership.",e);
		}
	}
	
	/**
	 * Removes user from group.
	 * <br />
	 * <b>Note:</b> Only agents can be added to or removed from a group.
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/group_memberships.html"> Group Membership </a>
	 * REST API for more details.
	 * 
	 * @param userObjectId
	 * @param groupObjectId
	 * @throws ConnectorException for service related exception.
	 */
	public void removeUserFromGroup(Integer userId,Integer groupId){
		//first find group member representing relation between user and group
		GroupMembership groupMembership = getGroupMembershipForUserAndGroup(userId, groupId);
		
		//using group member id for deleting relation
		HttpResponse response = httpRequestHandler.handleRequestDelete(
				constructURI(String.format("group_memberships/%d",groupMembership.getId())), getHeaders().toArray(new HttpPair[0]));
		

		if(response.status().getCode() != 200){
			throw new ConnectorException(
					"Problem in deleting group member as status code is not 200 is "
							+ response.status().getCode() + " : " + response.contentString());
		}
		
	}
	
	/**
	 * Finds relationship id i.e. between user and group represented by GroupMembership entity.
	 * 
	 * @param userId
	 * @param groupId
	 * @return GroupMembership 
	 * @throws PrincipalNotFoundException if relationship cannot be determined between user and group
	 */
	public GroupMembership getGroupMembershipForUserAndGroup(Integer userId,Integer groupId){
		GroupMemberships groupMemberships = getGroupMembershipsForUser(userId);
		
		for (GroupMembership groupMembership : groupMemberships.getGroupMemberships()) {
			if(groupMembership.getGroupId().equals(groupId) && groupMembership.getUserId().equals(userId)){
				return groupMembership;
			}
		}
		throw new PrincipalNotFoundException(
				"Group member not found for User ID and Group Id "
						+ userId + " " + groupId
						+ " not found.", null, PrincipalType.role);
	}
	
	/**
	 * Finds relationship id i.e. between user and group represented by GroupMembership entity.
	 * 
	 * @param userId
	 * @return GroupMemberships 
	 * @throws PrincipalNotFoundException if relationship cannot be determined between user and group
	 */
	public GroupMemberships getGroupMembershipsForUser(Integer userId){
		try {
			HttpResponse response = httpRequestHandler
					.handleRequestGet(constructURI(String.format("users/%d/group_memberships",userId)), getHeaders().toArray(new HttpPair[0]));
			
			GroupMemberships groupMemberships;
			try {
				groupMemberships = JsonMapperService.getInstance().getObject(GroupMemberships.class,response.contentString());
			}
			finally {
				response.release();
			}
			
			/**
			 * Groupmemberships does not have any detailed information about the group it is linking to 
			 * user, in some scenarios this information is important, hence we fetch it(group info) in a different call
			 * and set it to appropriate group memebership
			 */
			try {
				response = httpRequestHandler
						.handleRequestGet(constructURI(String.format("users/%d/groups",userId)), getHeaders().toArray(new HttpPair[0]));
			
			/**
			 * We have annotated Groups class's groups list to "results" but in this class the property is returned
			 * by key "groups" rather than "results". Problem is we have single property which is to be mapped to two different
			 * json keys.
			 * 
			 * In this call json returned is having key as "groups" but as mentioned Groups class expects it as "results"
			 * therefore we need to fetch json represented by key "groups" as a Java Map, serialize it back to json, 
			 * and again deserialize it to List of Group.
			 * 
			 */
				List<Group> groups = JsonMapperService.getInstance().getObject(new TypeReference<List<Group>>() {},
									 JsonMapperService.getInstance().getJson(
											 	JsonMapperService.getInstance().getJsonProperty(response.contentString(), "groups")
											)
									 );
					
				if (groups != null) {
					/**
					 * Mapping group membership to group	
					 */
					for (Group group : groups) {
						for (GroupMembership groupMembership : groupMemberships
								.getGroupMemberships()) {
							if (groupMembership.getGroupId().equals(group.getId())) {
								groupMembership.setGroup(group);
							}
						}
					}
				}
				return groupMemberships;
			}
			finally {
				response.release();
			}
		
		} catch (IOException e) {
			throw new ConnectorException("Problem in fetching group membership.",e);
		}
	}
	
	/**
	 * Helper method to check for already existing group response message
	 * 
	 * @param group
	 * @param response
	 */
	public boolean checkGroupExists(String groupName){
		try{
			Group group = getByName(groupName);
			return group != null && groupName.equals(group.getName()) && !group.getDeleted();
		}catch(PrincipalNotFoundException exception){
			return false;
		}
	}
	
}
