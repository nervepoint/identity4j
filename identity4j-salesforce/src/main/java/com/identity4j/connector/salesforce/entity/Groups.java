package com.identity4j.connector.salesforce.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents collection of Groups as represented in JSON collection records
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Groups {

	@JsonProperty("records")
	private List<Group> groups;

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

}
