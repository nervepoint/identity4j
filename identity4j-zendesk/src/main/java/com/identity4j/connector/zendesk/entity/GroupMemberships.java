package com.identity4j.connector.zendesk.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GroupMemberships {

	@JsonProperty("group_memberships")
	private List<GroupMembership> groupMemberships;
	@JsonProperty("next_page")
	private String nextPage;
	@JsonProperty("previous_page")
	private String previousPage;
	@JsonProperty("count")
	private Integer count;
	
	public List<GroupMembership> getGroupMemberships() {
		return groupMemberships;
	}
	public void setGroupMemberships(List<GroupMembership> groupMemberships) {
		this.groupMemberships = groupMemberships;
	}
	public String getNextPage() {
		return nextPage;
	}
	public void setNextPage(String nextPage) {
		this.nextPage = nextPage;
	}
	public String getPreviousPage() {
		return previousPage;
	}
	public void setPreviousPage(String previousPage) {
		this.previousPage = previousPage;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	
}
