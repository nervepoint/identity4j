package com.identity4j.connector.office365.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.entity.Group;
import com.identity4j.connector.office365.entity.Role;
import com.identity4j.connector.office365.entity.User;
import com.identity4j.connector.office365.entity.Users;
import com.identity4j.connector.office365.services.token.handler.ADToken;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.http.response.HttpResponse;
import com.identity4j.util.json.JsonMapperService;

/**
 * This class is responsible for managing REST calls for User entity.
 * 
 * @author gaurav
 *
 */
public class UserService extends AbstractRestAPIService{
	
	public UserService(ADToken token, HttpRequestHandler httpRequestHandler,Office365Configuration office365Configuration) {
		super(token, httpRequestHandler, office365Configuration);
	}

	/**
	 * This method retrieves an instance of User corresponding to provided object id or user principal name.
	 * If user is not found in data store it throws PrincipalNotFoundException
	 * 
	 * @param objectId/userPrincipalName
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public User get(String objectId) {

		User user = null;
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI(String.format("/users/%s", objectId), null), HEADER_HTTP_HOOK);
		if(response.getHttpStatusCodes().getStatusCode().intValue() == 404){
			throw new PrincipalNotFoundException(objectId + " not found.",null,PrincipalType.user);
		}
		user = JsonMapperService.getInstance().getObject(User.class, response.getData().toString());
		
		probeGroupsAndRoles(user);
		return user;
	}
	
	/**
	 * This method retrieves all users present in the data store. . If there is more data to return, 
	 * {@link Users#getNextLink()} will be non-null.
	 * 
	 * @return users list
	 */
	public Users all() {
		return all(null);
	}
	
	/**
	 * This method retrieves all users present in the data store, continuing a previous pages
	 * request. <code>null</code> may be used, in which case this query is started afresh
	 * (functionally the same as {@link #all()}. If there is more data to return, 
	 * {@link Users#getNextLink()} will be non-null.
	 * 
	 * @return users list
	 */
	public Users all(String nextLink) {
		StringBuilder q = new StringBuilder();
		q.append("$top=");
		q.append(office365Configuration.getRequestSizeLimit());
		if(nextLink != null) {
			q.append("&$skiptoken=");
			q.append(nextLink.substring(nextLink.indexOf("$skiptoken=") + 11));
		}
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI("/users", q.toString()),HEADER_HTTP_HOOK);
		String string = response.getData().toString();
		return JsonMapperService.getInstance().getObject(Users.class, string);
	}
	
	/**
	 * Saves user into active directory.
	 * 
	 * @param user
	 * @throws PrincipalAlreadyExistsException if user by same principal name exists.
	 * @throws ConnectorException for possible json mapping exceptions
	 * @return
	 */
	public User save(User user){
		try{
			HttpResponse response = httpRequestHandler.handleRequestPost(
					constructURI("/users", null), JsonMapperService
							.getInstance().getJson(user), HEADER_HTTP_HOOK);
			if(response.getHttpStatusCodes().getStatusCode().intValue() == 400){
				AppErrorMessage errorMessage = JsonMapperService.getInstance().getObject(AppErrorMessage.class, response.getData().toString().replaceAll("odata.error", "error"));
				String err = errorMessage.getError().getMessage().getValue();
				
				// TODO is there not a better way to test for error messages? This seems crazy
				
				if("A conflicting object with one or more of the specified property values is present in the directory.".equals(err) ||
				   "Another object with the same value for property userPrincipalName already exists.".equals(err)){
					throw new PrincipalAlreadyExistsException("Principal contains conflicting properties which already exists, " + user.getUserPrincipalName());
				} else {
					throw new ConnectorException(err);
				}
			}
			user = JsonMapperService.getInstance().getObject(User.class, response.getData().toString());
			return user;
		} catch (IOException e) {
			throw new ConnectorException("Problem in saving user",e);
		}
	}
	
	/**
	 * Updates user properties sent for update.
	 * <br />
	 * For finding user to update it makes use of guid.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the user by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void update(User user){
		try{
			HttpResponse response = httpRequestHandler.handleRequestPatch(
					constructURI(
							String.format("/users/%s", user.getObjectId()),
							null), JsonMapperService.getInstance()
							.getJson(user), HEADER_HTTP_HOOK);
			
			if(response.getHttpStatusCodes().getStatusCode().intValue() == 404){
				throw new PrincipalNotFoundException(user.getObjectId() + " not found.",null,PrincipalType.user);
			}
			
			if(response.getHttpStatusCodes().getStatusCode().intValue() != 204){
				throw new ConnectorException("Problem in updating user as status code is not 204 is " + response.getHttpStatusCodes().getStatusCode().intValue() + ". " + response.getHttpStatusCodes().getResonPhrase());
			}
			
		} catch (IOException e) {
			throw new ConnectorException("Problem in updating user",e);
		}
	}

	/**
	 * Deletes user by specified object id.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the user by object id not found in active directory.
	 * @throws ConnectorException for service related exception.
	 */
	public void delete(String objectId) {
		HttpResponse response = httpRequestHandler.handleRequestDelete(
				constructURI(String.format("/users/%s", objectId), null),
				HEADER_HTTP_HOOK);
		
		if(response.getHttpStatusCodes().getStatusCode().intValue() == 404){
			throw new PrincipalNotFoundException(objectId + " not found.",null,PrincipalType.user);
		}
		
		if(response.getHttpStatusCodes().getStatusCode().intValue() != 204){
			throw new ConnectorException("Problem in deleting user as status code is not 204 is " + response.getHttpStatusCodes().getStatusCode().intValue() + ". " + response.getHttpStatusCodes().getResonPhrase());
		}
		
	}
	
	/**
	 * This method retrieves an instance of GroupsAndRoles corresponding to provided object id.
	 * GroupsAndRoles encapsulates Roles and Groups assigned to object principal.
	 * If service principal is not found in data store it throws PrincipalNotFoundException
	 * 
	 * @param objectId/userPrincipalName
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public GroupsAndRoles getServicePrincipalGroupsAndRoles(String objectId) {
		HttpResponse response = httpRequestHandler.handleRequestGet(
				constructURI(String.format("/servicePrincipals/%s/memberOf",
						objectId), null), HEADER_HTTP_HOOK);
		if(response.getHttpStatusCodes().getStatusCode().intValue() == 404){
			throw new PrincipalNotFoundException(objectId + " not found.",null,PrincipalType.user);
		}
		
		return mapGroupsAndRoles(response);
	}
	
	
	/**
	 * It checks for delete privilege is given to service id or not.
	 * 
	 * @return true if service principal is having delete privilege
	 * @throws PrincipalNotFoundException 
	 */
	public boolean isDeletePrivilege(String principalObjectId,String appDeletePrincipalRole){
		GroupsAndRoles groupsAndRoles = getServicePrincipalGroupsAndRoles(principalObjectId);
		List<Role> roles = groupsAndRoles.roles;
		for (Role role : roles) {
			if(role.getDisplayName().equalsIgnoreCase(appDeletePrincipalRole)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Finds roles and groups a user by specified object id has or belongs.
	 * 
	 * @param objectId
	 * @param user
	 */
	public void probeGroupsAndRoles(User user) {
		HttpResponse response = httpRequestHandler
				.handleRequestGet(constructURI(String.format("/users/%s/memberOf",user.getObjectId()), null),
						HEADER_HTTP_HOOK);
		GroupsAndRoles groupsAndRoles = mapGroupsAndRoles(response);
		user.setMemberOf(groupsAndRoles.groups);
		user.setRoles(groupsAndRoles.roles);
	}

	/**
	 * Helper utility function which segregates groups and roles an object id belongs to into different lists and
	 * returns them encapsulated in GroupsAndRoles.
	 * 
	 * @param response
	 * @return GroupsAndRoles instance
	 */
	private GroupsAndRoles mapGroupsAndRoles(HttpResponse response) {
		GroupsAndRoles groupsAndRoles = new GroupsAndRoles();
		Map<?,?> groupsAndRolesMap = JsonMapperService.getInstance().getObject(Map.class,response.getData().toString());
		List<?> groupsAndRolesList = (List<?>) groupsAndRolesMap.get("value");
		for (Object object : groupsAndRolesList) {
			Object type = ((Map<?,?>)object).get("objectType");
			if("Group".equals(type.toString())){
				groupsAndRoles.groups.add(JsonMapperService.getInstance().convert(object, Group.class));
			}else{
				groupsAndRoles.roles.add(JsonMapperService.getInstance().convert(object, Role.class));
			}
		}
		
		return groupsAndRoles;
	}
	
	/**
	 * Helper class encapsulates list of groups and roles for a principal.
	 * 
	 * @author gaurav
	 *
	 */
	private static class GroupsAndRoles {
		private List<Role> roles = new ArrayList<Role>();
		private List<Group> groups = new ArrayList<Group>();
 	}
	
}

