package com.identity4j.connector.office365.services;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.office365.Office365Configuration;
import com.identity4j.connector.office365.entity.Group;
import com.identity4j.connector.office365.entity.Groups;
import com.identity4j.connector.office365.filter.Filter;
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
public class GroupService extends AbstractRestAPIService {

	GroupService(ADToken token, HttpRequestHandler httpRequestHandler, Office365Configuration office365Configuration) {
		super(token, httpRequestHandler, office365Configuration);
	}

	/**
	 * This method retrieves an instance of Group corresponding to provided object
	 * id If group is not found in data store it throws PrincipalNotFoundException
	 * 
	 * @param objectId/groupPrincipalName
	 * @return
	 */
	public Group get(final String objectId) {

		Group group = null;

		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestGet(constructURI(String.format("/groups/%s", objectId), null),
						getHeaders().toArray(new HttpPair[0]));
			}
		});

		try {
			if (response.status().getCode() == 404 || response.status().getCode() == 400) {
				throw new PrincipalNotFoundException(objectId + " not found.", null, PrincipalType.role);
			}
			else
				checkResponse(response, 204);
			group = JsonMapperService.getInstance().getObject(Group.class, response.contentString());
			return group;
		} finally {
			response.release();
		}
	}

	protected void checkResponse(HttpResponse response, int code) {
		if (response.status().getCode() != code) {
			throw new ConnectorException(String.format("Unexpected status %d, expected %d. %s", response.status().getCode(), code,
					response.status().getError()));
		}
	}

	/**
	 * This method retrieves an instance of Group corresponding to provided object
	 * id If group is not found in data store it throws PrincipalNotFoundException
	 * 
	 * @param objectId/groupPrincipalName
	 * @return
	 */
	public Group getByName(final String name) {

		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				URI uri = constructURI("/groups", String.format("$filter=displayName eq '%s'&$top=1&$count=true", name));
				return httpRequestHandler.handleRequestGet(
						uri,
						getHeaders().toArray(new HttpPair[] {
								new HttpPair("ConsistencyLevel", "eventual")
						}));
			}
		});

		try {
			checkResponse(response, 200);
			String contentString = response.contentString();
			Groups g = JsonMapperService.getInstance().getObject(Groups.class, contentString);
			final List<Group> groups = g.getGroups();
			if (groups.isEmpty())
				throw new PrincipalNotFoundException(name + " not found.", null, PrincipalType.role);
			return groups.get(0);
		} finally {
			response.release();
		}
	}

	/**
	 * This method retrieves all groups present in the data store. . If there is
	 * more data to return, {@link Groups#getNextLink()} will be non-null.
	 * 
	 * @return groups list
	 */
	public Groups all() {
		return all(null);
	}

	/**
	 * This method retrieves all groups present in the data store, continuing a
	 * previous pages request. <code>null</code> may be used, in which case this
	 * query is started afresh (functionally the same as {@link #all()}. If there is
	 * more data to return, {@link Groups#getNextLink()} will be non-null.
	 * 
	 * @return users list
	 */
	public Groups all(String nextLink) {
		return all(nextLink, null);
	}

	/**
	 * This method retrieves all groups present in the data store, continuing a
	 * previous pages request. <code>null</code> may be used, in which case this
	 * query is started afresh (functionally the same as {@link #all()}. If there is
	 * more data to return, {@link Groups#getNextLink()} will be non-null. A filter
	 * may also be supplied. This will be encoded as the OData 'filter' expression.
	 * If this is null, it will be ommitted
	 * 
	 * @return users list
	 */
	public Groups all(String nextLink, Filter filter) {
		final StringBuilder q = new StringBuilder();
		q.append("$top=");
		q.append(office365Configuration.getRequestSizeLimit());
		if(filter != null) {
			q.append("&$filter=");
			q.append(filter.encode());
		}
		if (nextLink != null) {
			q.append("&$skiptoken=");
			q.append(nextLink.substring(nextLink.indexOf("$skiptoken=") + 11));
		}

		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestGet(constructURI("/groups", q.toString()),
						getHeaders().toArray(new HttpPair[0]));
			}
		});

		try {
			String string = response.contentString();
			checkResponse(response, 200);
			return JsonMapperService.getInstance().getObject(Groups.class, string);
		} finally {
			response.release();
		}
	}

	public GroupMembers members(String guid) {
		return members(null, guid);
	}

	public GroupMembers members(String nextLink, final String guid) {
		final StringBuilder q = new StringBuilder();
		q.append("$top=");
		q.append(office365Configuration.getRequestSizeLimit());
		if (nextLink != null) {
			q.append("&$skiptoken=");
			q.append(nextLink.substring(nextLink.indexOf("$skiptoken=") + 11));
		}

		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestGet(
						constructURI(String.format("/groups/%s/members", guid), q.toString()),
						getHeaders().toArray(new HttpPair[0]));
			}
		});

		try {
			checkResponse(response, 200);
			return JsonMapperService.getInstance().getObject(GroupMembers.class, response.contentString());
		} finally {
			response.release();
		}
	}
	
	
	public static class GroupMembers {
		private List<GroupMember> value;
		
		@JsonProperty("@odata.nextLink")
		private String nextLink;
		
		public List<GroupMember> getValue() {
			return value;
		}

		public void setValue(List<GroupMember> value) {
			this.value = value;
		}
		
		public String getNextLink() {
			return nextLink;
		}


	}

	public static class GroupMember {
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	/**
	 * Saves group into active directory.
	 * 
	 * @param group
	 * @throws PrincipalAlreadyExistsException if group by same principal name
	 *                                         exists.
	 * @throws ConnectorException              for possible json mapping exceptions
	 * @return
	 */
	public Group save(final Group group) {
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestPost(constructURI("/groups", null),
						JsonMapperService.getInstance().getJson(group), getHeaders().toArray(new HttpPair[0]));
			}
		});

		if (response.status().getCode() == 400) {
			AppErrorMessage errorMessage = JsonMapperService.getInstance().getObject(AppErrorMessage.class,
					response.contentString().replaceAll("odata.error", "error"));
			if ("A conflicting object with one or more of the specified property values is present in the directory."
					.equals(errorMessage.getError().getMessage())) {
				throw new PrincipalAlreadyExistsException(
						"Principal contains conflicting properties which already exists, " + group.getDisplayName());
			}
		}
		else
			checkResponse(response, 201);
		return JsonMapperService.getInstance().getObject(Group.class, response.contentString());
	}

	/**
	 * Updates group properties sent for update. <br />
	 * For finding group to update it makes use of guid.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the group by object id not found in
	 *                                    active directory.
	 * @throws ConnectorException         for service related exception.
	 */
	public void update(final Group group) {

		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestPatch(
						constructURI(String.format("/groups/%s", group.getObjectId()), null),
						JsonMapperService.getInstance().getJson(group), getHeaders().toArray(new HttpPair[0]));
			}
		});

		if (response.status().getCode() == 404) {
			throw new PrincipalNotFoundException(group.getObjectId() + " not found.", null, PrincipalType.role);
		}
		else
			checkResponse(response, 204);
	}

	/**
	 * Deletes group by specified object id.
	 * 
	 * @param object id of group
	 * @throws PrincipalNotFoundException if the group by object id not found in
	 *                                    active directory.
	 * @throws ConnectorException         for service related exception.
	 */
	public void delete(final String objectId) {
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestDelete(constructURI(String.format("/groups/%s", objectId), null),
						getHeaders().toArray(new HttpPair[0]));
			}
		});

		if (response.status().getCode() == 404) {
			throw new PrincipalNotFoundException(objectId + " not found.", null, PrincipalType.role);
		}
		else
			checkResponse(response, 204);
	}

	/**
	 * Adds user to group.
	 * 
	 * @param groupObjectId
	 * @param userOjectId
	 * @throws PrincipalNotFoundException if the group by object id not found in
	 *                                    active directory.
	 * @throws ConnectorException         for service related exception.
	 */
	public void addUserToGroup(String userOjectId, final String groupObjectId) {
		String dataTemplate = "{ \"@odata.id\":\"%s\" }";
		String url = constructURI(String.format("/directoryObjects/%s", userOjectId), null).toString();
		final String data = String.format(dataTemplate, url);

		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestPost(
						constructURI(String.format("/groups/%s/members/$ref", groupObjectId), null), data,
						getHeaders().toArray(new HttpPair[0]));
			}
		});

		if (response.status().getCode() == 404) {
			throw new PrincipalNotFoundException(
					"Principal '" + userOjectId + "' in  '" + groupObjectId + "' not found.", null, PrincipalType.role);
		}

		if (response.status().getCode() == 400) {
			AppErrorMessage errorMessage = JsonMapperService.getInstance().getObject(AppErrorMessage.class,
					response.contentString().replaceAll("odata.error", "error"));
			if (errorMessage.getError().getMessage().contains("Invalid object identifier")) {
				throw new PrincipalNotFoundException(
						"Principal '" + userOjectId + "' in  '" + groupObjectId + "' not found.", null,
						PrincipalType.role);
			}
		}
		checkResponse(response, 204);
	}

	/**
	 * Removes user from group.
	 * 
	 * @param groupObjectId
	 * @param userObjectId
	 * @throws PrincipalNotFoundException if the group by object id not found in
	 *                                    active directory.
	 * @throws ConnectorException         for service related exception.
	 */
	public void removeUserFromGroup(final String userObjectId, final String groupObjectId) {
		HttpResponse response = retryIfTokenFails(new Callable<HttpResponse>() {
			@Override
			public HttpResponse call() throws Exception {
				return httpRequestHandler.handleRequestDelete(
						constructURI(String.format("/groups/%s/members/%s/$ref", groupObjectId, userObjectId), null),
						getHeaders().toArray(new HttpPair[0]));
			}
		});

		if (response.status().getCode() != 204) {
			throw new ConnectorException("Problem in removing user from group as status code is not 204 is "
					+ response.status().getCode() + " : " + response.contentString());
		}
	}

}
