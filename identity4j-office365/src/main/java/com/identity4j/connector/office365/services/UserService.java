package com.identity4j.connector.office365.services;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.Office365Connector;
import com.identity4j.connector.office365.entity.Group;
import com.identity4j.connector.office365.entity.Role;
import com.identity4j.connector.office365.entity.User;
import com.identity4j.connector.office365.entity.Users;
import com.identity4j.connector.office365.filter.Filter;
import com.identity4j.connector.office365.services.token.handler.ADToken;
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
public class UserService extends AbstractRestAPIService {
	private static final Log log = LogFactory.getLog(UserService.class);

	public UserService(ADToken token, HttpRequestHandler httpRequestHandler,
			Office365Configuration office365Configuration) {
		super(token, httpRequestHandler, office365Configuration);
	}

	/**
	 * This method retrieves an instance of User corresponding to provided
	 * object id or user principal name. If user is not found in data store it
	 * throws PrincipalNotFoundException
	 * 
	 * @param objectId/userPrincipalName
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public User get(final String objectId) {

		User user = null;
		
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				URI uri = constructURI(String.format("/users/%s", objectId), selectList());
				return httpRequestHandler.handleRequestGet(
						uri, getHeaders().toArray(new HttpPair[0]));
			}
		});
		
		try {
			if (response.status().getCode() == 404) {
				throw new PrincipalNotFoundException(objectId + " not found.", null, PrincipalType.user);
			} else
				checkResponse(response, 200);
			String json = response.contentString();
			user = JsonMapperService.getInstance().getObject(User.class, json);

			probeGroupsAndRoles(user);
			log.info(String.format("User %s (%s) has %d roles, membmer of %d", user.getUserPrincipalName(), objectId, user.getRoles().size(), user.getMemberOf().size()));
			return user;
		} finally {
			response.release();
		}
	}

	protected void checkResponse(HttpResponse response, int code) {
		if(response.status().getCode() != code) {
			throw new ConnectorException(String.format("Unexpected response code %d. %s", response.status().getCode(), response.status().getError()));
		}
	}

	/**
	 * This method retrieves all users present in the data store. . If there is
	 * more data to return, {@link Users#getNextLink()} will be non-null.
	 * 
	 * @return users list
	 */
	public Users all() {
		return all(null);
	}

	/**
	 * This method retrieves all users present in the data store, continuing a
	 * previous pages request. <code>null</code> may be used, in which case this
	 * query is started afresh (functionally the same as {@link #all()}. If
	 * there is more data to return, {@link Users#getNextLink()} will be
	 * non-null.
	 * 
	 * @return users list
	 */
	public Users all(String nextLink) {
		return all(nextLink, null);
	}

	/**
	 * This method retrieves all users present in the data store, continuing a
	 * previous pages request. <code>null</code> may be used, in which case this
	 * query is started afresh (functionally the same as {@link #all()}. If
	 * there is more data to return, {@link Users#getNextLink()} will be
	 * non-null. A filter may also be supplied. This will be encoded as the OData 
	 * 'filter' expression. If this is null, it will be ommitted
	 * 
	 * @return users list
	 */
	public Users all(String nextLink, Filter filter) {
		final StringBuilder q = new StringBuilder();
		q.append(selectList());
		q.append("&$top=");
		q.append(office365Configuration.getRequestSizeLimit());
		if(filter != null) {
			q.append("&$filter=");
			q.append(filter.encode());
			try {
				q.append(URLEncoder.encode(filter.encode(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException("Filter cannot be UTF-8 encoded.");
			}
		}
		if (nextLink != null) {
			q.append("&$skiptoken=");
			q.append(nextLink.substring(nextLink.indexOf("$skiptoken=") + 11));
		}
		
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestGet(constructURI("/users", q.toString()),
						getHeaders().toArray(new HttpPair[0]));
			}
		});
		
		try {
			String string = response.contentString();
			checkResponse(response, 200);
			return JsonMapperService.getInstance().getObject(Users.class, string);
		} finally {
			response.release();
		}
	}

	/**
	 * Saves user into active directory.
	 * 
	 * @param user
	 * @throws PrincipalAlreadyExistsException
	 *             if user by same principal name exists.
	 * @throws ConnectorException
	 *             for possible json mapping exceptions
	 * @return
	 */
	public User save(final User user) {
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				String json = JsonMapperService.getInstance().getJson(user);
				return httpRequestHandler.handleRequestPost(constructURI("/users", null),
						json, getHeaders().toArray(new HttpPair[0]));
			}
		});
		
		if (response.status().getCode() == 400) {
			AppErrorMessage errorMessage = JsonMapperService.getInstance().getObject(AppErrorMessage.class,
					response.contentString().replaceAll("odata.error", "error"));
			String err = errorMessage.getError().getMessage();

			// TODO is there not a better way to test for error messages?
			// This seems crazy

			if ("A conflicting object with one or more of the specified property values is present in the directory."
					.equals(err)
					|| "Another object with the same value for property userPrincipalName already exists."
							.equals(err)) {
				throw new PrincipalAlreadyExistsException(
						"Principal contains conflicting properties which already exists, "
								+ user.getUserPrincipalName());
			} else {
				throw new ConnectorException(err);
			}
		}
		return JsonMapperService.getInstance().getObject(User.class, response.contentString());
	}

	/**
	 * Updates user properties sent for update. <br />
	 * For finding user to update it makes use of guid.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException
	 *             if the user by object id not found in active directory.
	 * @throws ConnectorException
	 *             for service related exception.
	 */
	public void update(final User user) {
		/* Do not try to save object ID */
		User nuser = new User(user);
		nuser.setId(null);
		nuser.setObjectId(null);
		
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				String json = JsonMapperService.getInstance().getJson(nuser);
				return httpRequestHandler.handleRequestPatch(
						constructURI(String.format("/users/%s", user.getObjectId()), null),
						json, getHeaders().toArray(new HttpPair[0]));
			}
		});
		
		if (response.status().getCode() == 404) {
			throw new PrincipalNotFoundException(user.getObjectId() + " not found.", null, PrincipalType.user);
		}
		else
			checkResponse(response, 204);
	}

	/**
	 * Deletes user by specified object id.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException
	 *             if the user by object id not found in active directory.
	 * @throws ConnectorException
	 *             for service related exception.
	 */
	public void delete(final String objectId) {
		
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestDelete(
						constructURI(String.format("/users/%s", objectId), null), getHeaders().toArray(new HttpPair[0]));
			}
		});

		if (response.status().getCode() == 404) {
			throw new PrincipalNotFoundException(objectId + " not found.", null, PrincipalType.user);
		}
		else
			checkResponse(response, 204);

	}

	/**
	 * This method retrieves an instance of GroupsAndRoles corresponding to
	 * provided object id. GroupsAndRoles encapsulates Roles and Groups assigned
	 * to object principal. If service principal is not found in data store it
	 * throws PrincipalNotFoundException
	 * 
	 * @param objectId/userPrincipalName
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public GroupsAndRoles getServicePrincipalGroupsAndRoles(final String objectId) {
		
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestGet(
						constructURI(String.format("/servicePrincipals/%s/memberOf", objectId), null),
						getHeaders().toArray(new HttpPair[0]));
			}
		});
		
		try {
			if (response.status().getCode() == 404) {
				throw new PrincipalNotFoundException(objectId + " not found.", null, PrincipalType.user);
			}
			else
				checkResponse(response, 204);

			return mapGroupsAndRoles(response);
		} finally {
			response.release();
		}
	}

	/**
	 * It checks for delete privilege is given to service id or not.
	 * 
	 * @return true if service principal is having delete privilege
	 * @throws PrincipalNotFoundException
	 */
	public boolean isDeletePrivilege(String principalObjectId, String appDeletePrincipalRole) {
		GroupsAndRoles groupsAndRoles = getServicePrincipalGroupsAndRoles(principalObjectId);
		List<Role> roles = groupsAndRoles.roles;
		for (Role role : roles) {
			if (role.getDisplayName().equalsIgnoreCase(appDeletePrincipalRole)) {
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
	public void probeGroupsAndRoles(final User user) {
		
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestGet(
						constructURI(String.format("/users/%s/memberOf", user.getObjectId()), null),
						getHeaders().toArray(new HttpPair[0]));
			}
		});
		try {
			GroupsAndRoles groupsAndRoles = mapGroupsAndRoles(response);
			user.setMemberOf(groupsAndRoles.groups);
			user.setRoles(groupsAndRoles.roles);
		} finally {
			response.release();
		}
	}

	/**
	 * Helper utility function which segregates groups and roles an object id
	 * belongs to into different lists and returns them encapsulated in
	 * GroupsAndRoles.
	 * 
	 * @param response
	 * @return GroupsAndRoles instance
	 */
	private GroupsAndRoles mapGroupsAndRoles(HttpResponse response) {
		GroupsAndRoles groupsAndRoles = new GroupsAndRoles();
		Map<?, ?> groupsAndRolesMap = JsonMapperService.getInstance().getObject(Map.class, response.contentString());
		List<?> groupsAndRolesList = (List<?>) groupsAndRolesMap.get("value");
		if (groupsAndRolesList != null) {
			for (Object object : groupsAndRolesList) {
				Object type = ((Map<?, ?>) object).get("isAssignableRole");
				if (Boolean.TRUE.equals(type)) {
					groupsAndRoles.roles.add(JsonMapperService.getInstance().convert(object, Role.class));
				} else {
					groupsAndRoles.groups.add(JsonMapperService.getInstance().convert(object, Group.class));
				}
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

	private String selectList() {
		return "$select=id,objectId,dirSyncEnabled,displayName,mail,mailNickname,objectReference,objectType,accountEnabled,city,"
				+ "country,department,givenName,jobTitle,passwordPolicies,physicalDeliveryOfficeName,postalCode,preferredLanguage,state,streetAddress,"
				+ "surname,usageLocation,userPrincipalName,passwordProfile,onPremisesSyncEnabled,faxNumber,onPremisesImmutableId,"
				+ "onPremisesLastSyncDateTime,mobilePhone,signinSessionsValidFromDateTime,businessPhones,lastPasswordChangeDateTime";
	}
}
