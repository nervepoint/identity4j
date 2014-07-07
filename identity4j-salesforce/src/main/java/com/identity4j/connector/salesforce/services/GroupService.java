package com.identity4j.connector.salesforce.services;

import java.io.IOException;
import java.util.List;

import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.salesforce.SalesforceConfiguration;
import com.identity4j.connector.salesforce.entity.Group;
import com.identity4j.connector.salesforce.entity.GroupMember;
import com.identity4j.connector.salesforce.entity.GroupMembers;
import com.identity4j.connector.salesforce.entity.Groups;
import com.identity4j.util.StringUtil;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.http.response.HttpResponse;
import com.identity4j.util.json.JsonMapperService;

/**
 * 
 * This class is responsible for managing REST calls for Group entity.
 * 
 * @author gaurav
 *
 */
public class GroupService extends AbstractRestAPIService{

	/**
	 * Group attributes represented by Group entity in Salesforce data source
	 */
	private static final String GROUP_ATTRIBUTES = "Id,Name,DeveloperName,RelatedId,Type,Email,OwnerId,"
			+ "DoesSendEmailToMembers,DoesIncludeBosses,CreatedDate,CreatedById,LastModifiedDate,"
			+ "LastModifiedById,SystemModstamp";
	
	/**
	 * GroupMember and Group attributes together represented by GroupMember entity in Salesforce data source.
	 * <br/>
	 * <b>Note: </b> Group attributes are namespaced by 'Group'.
	 */
	private static final String GROUP_MEMBER_ATTRIBUTES = "Id,GroupId,UserOrGroupId,Group.Name,Group.Email,Group.DeveloperName,"
			+ "Group.RelatedId,Group.Type,Group.OwnerId,Group.DoesSendEmailToMembers,Group.DoesIncludeBosses,Group.CreatedDate,"
			+ "Group.CreatedById,Group.LastModifiedDate,Group.LastModifiedById,Group.SystemModstamp";
	
	
	public GroupService(HttpRequestHandler httpRequestHandler,SalesforceConfiguration serviceConfiguration) {
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
	public Group getByGuid(String guid) {

		Group group = null;
		HttpResponse response = httpRequestHandler.handleRequestGet(
				constructURI(String.format("Group/%s", guid)), HEADER_HTTP_HOOK);
		
		if(response.getHttpStatusCodes().getStatusCode().intValue() == 404 || 
				response.getHttpStatusCodes().getStatusCode().intValue() == 400){
			throw new PrincipalNotFoundException(guid + " not found.",null,PrincipalType.role);
		}
		group = JsonMapperService.getInstance().getObject(Group.class, response.getData().toString());
		
		return group;
	}
	
	/**
	 * This method retrieves an instance of Group corresponding to provided group name.
	 * If group is not found in data store it throws PrincipalNotFoundException
	 * <br /> 
	 * This method makes use of <b>Salesforce Object Query Language</b> for fetching group.
	 * 
	 * @param name
	 * @return Group entity
	 * @throws PrincipalNotFoundException if principal by guid not present in data source
	 */
	public Group getByName(String name){
		HttpResponse response = httpRequestHandler.handleRequestGet(
				constructSOQLURI(String.format(
						serviceConfiguration.getGetByNameGroupQuery(),
						GROUP_ATTRIBUTES, name)), HEADER_HTTP_HOOK);
		
		if(response.getHttpStatusCodes().getStatusCode().intValue() == 404){
			throw new PrincipalNotFoundException(name + " not found.",null,PrincipalType.role);
		}
		
		@SuppressWarnings("unchecked")
		List<String> records = (List<String>) JsonMapperService.getInstance()
				.getJsonProperty(response.getData().toString(), "records");
		if(records.isEmpty()){
			throw new PrincipalNotFoundException(name + " not found.",null,PrincipalType.role);
		}
		return JsonMapperService.getInstance().convert(records.get(0), Group.class);
	}
	
	
	/**
	 * This method retrieves all groups present in the data store.
	 * <br />
	 * This method makes use of <b>Salesforce Object Query Language</b> for fetching all groups.
	 * 
	 * @return groups list
	 */
	public Groups all(){
		HttpResponse response = httpRequestHandler.handleRequestGet(
				constructSOQLURI(String.format(serviceConfiguration.getGetAllGroups(),GROUP_ATTRIBUTES)), 
				HEADER_HTTP_HOOK);
		
		return JsonMapperService.getInstance().getObject(Groups.class, response.getData().toString());
	}
	
	/**
	 * Saves group into Salesforce datastore.
	 * 
	 * @param group
	 * @throws PrincipalAlreadyExistsException if group by same principal name exists.
	 * @throws ConnectorException for possible json mapping exceptions or network exceptions
	 * @return
	 */
	public Group save(Group group){
		try{
			HttpResponse response = httpRequestHandler.handleRequestPost(constructURI("Group"),
					JsonMapperService.getInstance().getJson(group),
					HEADER_HTTP_HOOK);
	
			String id = JsonMapperService.getInstance().getJsonProperty(response.getData().toString(), "id").toString();
			group.setId(id);
			
			return group;
		}catch(IOException e){
			throw new ConnectorException("Problem in saving group",e);
		}
	}
	
	/**
	 * Updates group properties sent for update.
	 * <br />
	 * For finding group to update it makes use of guid.
	 * 
	 * @param group
	 * @throws PrincipalNotFoundException if the group by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void update(Group group){
		String  id = null;
		try{
			
			//we cannot send id as updatable property hence we cache it and clear from pojo
			id = group.getId();
			group.setId(null);
			
			HttpResponse response = httpRequestHandler.handleRequestPatch(constructURI(String.format("Group/%s",id)),
					JsonMapperService.getInstance().getJson(group), HEADER_HTTP_HOOK);
			
			if(response.getHttpStatusCodes().getStatusCode().intValue() == 404){
				throw new PrincipalNotFoundException(group.getId() + " not found.",null,PrincipalType.role);
			}
			
			if(response.getHttpStatusCodes().getStatusCode().intValue() != 204){
				throw new ConnectorException(
						"Problem in updating group as status code is not 204 is "
								+ response.getHttpStatusCodes().getStatusCode()
										.intValue() + " : "
								+ response.getData());
			}
			
		} catch (IOException e) {
			throw new ConnectorException("Problem in updating group",e);
		}finally{
			//reset the cached values
			group.setId(id);
		}
	}
	
	/**
	 * Deletes group by specified guid.
	 * 
	 * @param object guid of group
	 * @throws PrincipalNotFoundException if the group by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void delete(String guid) {
		HttpResponse response = httpRequestHandler.handleRequestDelete(constructURI(String.format("Group/%s",guid)), HEADER_HTTP_HOOK);
		
		if(response.getHttpStatusCodes().getStatusCode().intValue() == 404 || 
				response.getHttpStatusCodes().getStatusCode().intValue() == 400){
			throw new PrincipalNotFoundException(guid + " not found.",null,PrincipalType.role);
		}

		if(response.getHttpStatusCodes().getStatusCode().intValue() != 204){
			throw new ConnectorException(
					"Problem in deleting group as status code is not 204 is "
							+ response.getHttpStatusCodes().getStatusCode()
									.intValue() + " : " + response.getData());
		}
	}
	
	/**
	 * Adds user to group.
	 * 
	 * @param userOjectId
	 * @param groupObjectId
	 * @throws ConnectorException for service related exception.
	 * @return GroupMember entity representing relation between user and group.
	 */
	public GroupMember addUserToGroup(String userOjectId,String groupObjectId){
		try{
			GroupMember groupMember = new GroupMember();
			groupMember.setGroupId(groupObjectId);
			groupMember.setUserOrGroupId(userOjectId);
			
			HttpResponse response = httpRequestHandler.handleRequestPost(
					constructURI("GroupMember"),JsonMapperService.getInstance().getJson(groupMember), HEADER_HTTP_HOOK);
			
			if(response.getHttpStatusCodes().getStatusCode().intValue() == 400){
				throw new ConnectorException(
						"Problem in adding group member "
								+ response.getHttpStatusCodes().getStatusCode()
										.intValue() + " : " + response.getData());
			}
	
			String id = JsonMapperService.getInstance().getJsonProperty(response.getData().toString(), "id").toString();
			if(StringUtil.isNullOrEmpty(id)){
				throw new ConnectorException("Problem in saving group member " + response.getData().toString());
			}
			groupMember.setId(id);
			
			return groupMember;
		}catch(IOException e){
			throw new ConnectorException("Problem in saving group member.",e);
		}
	}
	
	/**
	 * Removes user from group.
	 * 
	 * @param userObjectId
	 * @param groupObjectId
	 * @throws ConnectorException for service related exception.
	 */
	public void removeUserFromGroup(String userObjectId,String groupObjectId){
		//first find group member representing relation between user and group
		GroupMember groupMember = getGroupMemberForUserAndGroup(userObjectId, groupObjectId);
		
		//using group member id for deleting relation
		HttpResponse response = httpRequestHandler.handleRequestDelete(
				constructURI(String.format("GroupMember/%s",groupMember.getId())), HEADER_HTTP_HOOK);
		

		if(response.getHttpStatusCodes().getStatusCode().intValue() != 204){
			throw new ConnectorException(
					"Problem in deleting group member as status code is not 204 is "
							+ response.getHttpStatusCodes().getStatusCode()
									.intValue() + " : " + response.getData());
		}
		
	}
	
	/**
	 * Finds relationship id i.e. between user and group represented by GroupMember entity.
	 * 
	 * @param userObjectId
	 * @param groupObjectId
	 * @return GroupMember 
	 * @throws PrincipalNotFoundException if relationship cannot be determined between user and group
	 */
	public GroupMember getGroupMemberForUserAndGroup(String userObjectId,String groupObjectId){
		GroupMembers groupMembers = getGroupMembersForUser(userObjectId);
		if(groupMembers.getGroupMembers().isEmpty()){
			throw new PrincipalNotFoundException(
					"Group member not found for User ID and Group Id "
							+ userObjectId + " " + groupObjectId
							+ " not found.", null, PrincipalType.role);
		}
		return groupMembers.getGroupMembers().get(0);
	}
	
	/**
	 * Finds relationship id i.e. between user and group represented by GroupMember entity.
	 * 
	 * @param userObjectId
	 * @return GroupMembers id
	 * @throws PrincipalNotFoundException if relationship cannot be determined between user and group
	 */
	public GroupMembers getGroupMembersForUser(String userObjectId){
		HttpResponse response = httpRequestHandler
				.handleRequestGet(
						constructSOQLURI(String
								.format(serviceConfiguration.getGetGroupMembersForUser(),
										GROUP_MEMBER_ATTRIBUTES, userObjectId)), HEADER_HTTP_HOOK);
		
		return JsonMapperService.getInstance().getObject(GroupMembers.class, response.getData().toString());
	}
	
	/**
	 * Helper method to check for already existing group response message
	 * 
	 * @param group
	 * @param response
	 */
	public boolean groupAlreadyExists(String groupName) {
		try{
			Group group = getByName(groupName);
			return group != null && groupName.equals(group.getName());
		}catch(PrincipalNotFoundException e){
			return false;
		}
	}
}
