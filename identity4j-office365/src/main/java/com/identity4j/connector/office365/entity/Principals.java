package com.identity4j.connector.office365.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Principals<P extends Principal> {

	@JsonProperty("odata.nextLink")
	private String nextLink;
	
	public String getNextLink() {
		return nextLink;
	}

	@JsonIgnore
	public abstract List<P> getPrincipals();
}
