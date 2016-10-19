package com.identity4j.connector.office365.services;

import java.io.IOException;

import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.entity.Group;
import com.identity4j.connector.office365.entity.Groups;
import com.identity4j.connector.office365.services.token.handler.ADToken;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.json.JsonMapperService;

/**
 * This class is responsible for managing REST calls for Group entity.
 * 
 * @author gaurav
 *
 */
public class GroupService extends AbstractRestAPIService{
	
	GroupService(ADToken token, HttpRequestHandler httpRequestHandler,Office365Configuration office365Configuration) {
		super(token, httpRequestHandler, office365Configuration);
	}

	/**
	 * This method retrieves an instance of Group corresponding to provided object id or group principal name.
	 * If group is not found in data store it throws PrincipalNotFoundException
	 * 
	 * @param objectId/groupPrincipalName
	 * @return
	 */
	public Group get(String objectId) {

		Group group = null;
		HttpResponse response = httpRequestHandler.handleRequestGet(
				constructURI(String.format("/groups/%s", objectId), null), getHeaders().toArray(new HttpPair[0]));
		try {
			if (response.status().getCode() == 404) {
				throw new PrincipalNotFoundException(objectId + " not found.", null, PrincipalType.role);
			}
			group = JsonMapperService.getInstance().getObject(Group.class, response.contentString());
			return group;
		} finally {
			response.release();
		}
	}
	
	/**
	 * This method retrieves all groups present in the data store.
	 * 
	 * @return groups list
	 */
	public Groups all() {
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI("/groups", null), getHeaders().toArray(new HttpPair[0]));
		try {
			String contentString = response.contentString();
			return JsonMapperService.getInstance().getObject(Groups.class, contentString);
		}
		finally {
			response.release();
		}
		
	}
	
	/**
	 * Saves group into active directory.
	 * 
	 * @param group
	 * @throws PrincipalAlreadyExistsException if group by same principal name exists.
	 * @throws ConnectorException for possible json mapping exceptions
	 * @return
	 */
	public Group save(Group group){
		try{
			HttpResponse response = httpRequestHandler.handleRequestPost(
					constructURI("/groups", null), JsonMapperService
							.getInstance().getJson(group), getHeaders().toArray(new HttpPair[0]));
			if(response.status().getCode() == 400){
				AppErrorMessage errorMessage = JsonMapperService.getInstance().getObject(AppErrorMessage.class, response.contentString().replaceAll("odata.error", "error"));
				if("A conflicting object with one or more of the specified property values is present in the directory.".equals(errorMessage.getError().getMessage().getValue())){
					throw new PrincipalAlreadyExistsException("Principal contains conflicting properties which already exists, " + group.getDisplayName());
				}
			}
			group = JsonMapperService.getInstance().getObject(Group.class, response.contentString());
			return group;
		} catch (IOException e) {
			throw new ConnectorException("Problem in saving group",e);
		}
	}
	
	/**
	 * Updates group properties sent for update.
	 * <br />
	 * For finding group to update it makes use of guid.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the group by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void update(Group group){
		try{
			HttpResponse response = httpRequestHandler.handleRequestPost(
					constructURI(
							String.format("/groups/%s", group.getObjectId()),
							null),
					JsonMapperService.getInstance().getJson(group),
					getHeaders().toArray(new HttpPair[0]));
			
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(group.getObjectId() + " not found.",null,PrincipalType.role);
			}
			
			if(response.status().getCode() != 204){
				throw new ConnectorException(
						"Problem in updating group as status code is not 204 is "
								+ response.status().getCode() + " : "
								+ response.contentString());
			}
			
		} catch (IOException e) {
			throw new ConnectorException("Problem in updating group",e);
		}
	}

	/**
	 * Deletes group by specified object id.
	 * 
	 * @param object id of group
	 * @throws PrincipalNotFoundException if the group by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void delete(String objectId) {
		HttpResponse response = httpRequestHandler.handleRequestDelete(
				constructURI(String.format("/groups/%s", objectId), null),
				getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() == 404){
			throw new PrincipalNotFoundException(objectId + " not found.",null,PrincipalType.role);
		}
		
		if(response.status().getCode() != 204){
			throw new ConnectorException(
					"Problem in deleting group as status code is not 204 is "
							+ response.status().getCode() + " : " + response.contentString());
		}
	}
	
	/**
	 * Adds user to group.
	 * 
	 * @param groupObjectId
	 * @param userOjectId
	 * @throws PrincipalNotFoundException if the group by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void addUserToGroup(String userOjectId,String groupObjectId){
		String dataTemplate = "{ \"url\":\"%s\" }";
		String url = constructURI(String.format("/directoryObjects/%s",userOjectId), null).toString();
		String data = String.format(dataTemplate, url);
		
		HttpResponse response = httpRequestHandler.handleRequestPost(
				constructURI(String.format("/groups/%s/$links/members",
						groupObjectId), null), data, getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() == 404){
			throw new PrincipalNotFoundException("Principal '" + userOjectId + "' in  '"+ groupObjectId + "' not found.",null,PrincipalType.role);
		}
		
		if(response.status().getCode() == 400){
			AppErrorMessage errorMessage = JsonMapperService.getInstance().getObject(AppErrorMessage.class, response.contentString().replaceAll("odata.error", "error"));
			if(errorMessage.getError().getMessage().getValue().contains("Invalid object identifier")){
				throw new PrincipalNotFoundException("Principal '" + userOjectId + "' in  '"+ groupObjectId + "' not found.",null,PrincipalType.role);
			}
		}
		if(response.status().getCode() != 204){
			throw new ConnectorException(
					"Problem in adding user to group as status code is not 204 is "
							+ response.status().getCode() + " : " + response.contentString());
		}
	}
	
	/**
	 * Removes user from group.
	 * 
	 * @param groupObjectId
	 * @param userObjectId
	 * @throws PrincipalNotFoundException if the group by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void removeUserFromGroup(String userObjectId,String groupObjectId){
		HttpResponse response = httpRequestHandler.handleRequestDelete(
				constructURI(String.format("/groups/%s/$links/members/%s",
						groupObjectId, userObjectId), null), getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() != 204){
			throw new ConnectorException(
					"Problem in removing user from group as status code is not 204 is "
							+ response.status().getCode() + " : " + response.contentString());
		}
	}
	
	
}

