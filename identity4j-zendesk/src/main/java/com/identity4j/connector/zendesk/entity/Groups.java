package com.identity4j.connector.zendesk.entity;

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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents collection of Users as represented in JSON collection records
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Groups {

	@JsonProperty("results")
	private List<Group> groups;
	@JsonProperty("next_page")
	private String nextPage;
	@JsonProperty("previous_page")
	private String previousPage;
	@JsonProperty("count")
	private Integer count;

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
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
