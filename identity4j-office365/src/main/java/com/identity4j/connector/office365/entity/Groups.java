package com.identity4j.connector.office365.entity;

/*
 * #%L
 * Identity4J OFFICE 365
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
 * Class encapsulates collection of groups. 
 * <br />
 * This class is wrapper for collection to properly map list of group json to group java object.
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Groups extends Principals<Group> {

	@JsonProperty("value")
	private List<Group> groups;

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	@Override
	public List<Group> getPrincipals() {
		return getGroups();
	}
	
}
