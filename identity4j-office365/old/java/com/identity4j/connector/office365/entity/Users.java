package com.identity4j.connector.office365.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class encapsulates collection of users. 
 * <br />
 * This class is wrapper for collection to properly map list of user json to user java object.
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Users {

	@JsonProperty("value")
	private List<User> users;
	
	@JsonProperty("odata.nextLink")
	private String nextLink;
	
	public String getNextLink() {
		return nextLink;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
}
