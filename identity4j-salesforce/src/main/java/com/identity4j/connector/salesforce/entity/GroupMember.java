package com.identity4j.connector.salesforce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents GroupMember entity in Salesforce Datastore.
 * <br />
 * GroupMember represents relationship between Group and User entity.
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class GroupMember {

	@JsonProperty("Id")
	private String id;
	@JsonProperty("GroupId")
	private String groupId;
	@JsonProperty("UserOrGroupId")
	private String userOrGroupId;
	@JsonProperty("Group")
	private Group group;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getUserOrGroupId() {
		return userOrGroupId;
	}
	public void setUserOrGroupId(String userOrGroupId) {
		this.userOrGroupId = userOrGroupId;
	}
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}
	
}
