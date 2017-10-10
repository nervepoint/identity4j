package com.identity4j.connector.salesforce.entity;

/*
 * #%L
 * Identity4J Salesforce
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents Group entity in Salesforce Datastore.
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Group {

	@JsonProperty("Id")
	private String id;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("DeveloperName")
	private String developerName;
	@JsonProperty("RelatedId")
	private String relatedId;
	@JsonProperty("Type")
	private String type;
	@JsonProperty("Email")
	private String email;
	@JsonProperty("OwnerId")
	private String ownerId;
	@JsonProperty("DoesSendEmailToMembers")
	private Boolean doesSendEmailToMembers;
	@JsonProperty("DoesIncludeBosses")
	private Boolean doesIncludeBosses;
	@JsonProperty("CreatedDate")
	private String createdDate;
	@JsonProperty("CreatedById")
	private String createdById;
	@JsonProperty("LastModifiedDate")
	private String lastModifiedDate;
	@JsonProperty("LastModifiedById")
	private String lastModifiedById;
	@JsonProperty("SystemModstamp")
	private String systemModstamp;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDeveloperName() {
		return developerName;
	}
	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}
	public String getRelatedId() {
		return relatedId;
	}
	public void setRelatedId(String relatedId) {
		this.relatedId = relatedId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public Boolean getDoesSendEmailToMembers() {
		return doesSendEmailToMembers;
	}
	public void setDoesSendEmailToMembers(Boolean doesSendEmailToMembers) {
		this.doesSendEmailToMembers = doesSendEmailToMembers;
	}
	public Boolean getDoesIncludeBosses() {
		return doesIncludeBosses;
	}
	public void setDoesIncludeBosses(Boolean doesIncludeBosses) {
		this.doesIncludeBosses = doesIncludeBosses;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getCreatedById() {
		return createdById;
	}
	public void setCreatedById(String createdById) {
		this.createdById = createdById;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public String getLastModifiedById() {
		return lastModifiedById;
	}
	public void setLastModifiedById(String lastModifiedById) {
		this.lastModifiedById = lastModifiedById;
	}
	public String getSystemModstamp() {
		return systemModstamp;
	}
	public void setSystemModstamp(String systemModstamp) {
		this.systemModstamp = systemModstamp;
	}
	
}
