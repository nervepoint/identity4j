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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This class represents a Group Object of the WAAD top level entity Group. Also this class gives a publicly available
 * access mechanism to access each individual member variables such as Object Id, DisplayName etc.
 * @author gaurav
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Group {
	
	private String objectId;	
	private String description;
	private String dirSyncEnabled;
	private String displayName;
	private String lastDirSyncTime;
	private String mail;
	private String mailEnabled;
	private String securityEnabled;
	private String mailNickname;

	/**
	 * @return The objectId of this Group.
	 */
	public String getObjectId() {
		return objectId;
	}
	/**
	 * @param objectId The objectId to set.
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	/**
	 * @return The dirSyncEnabled attribute of this Group.
	 */
	public String getDirSyncEnabled() {
		return dirSyncEnabled;
	}
	/**
	 * @param dirSyncEnabled The dirSyncEnabled to set.
	 */
	public void setDirSyncEnabled(String dirSyncEnabled) {
		this.dirSyncEnabled = dirSyncEnabled;
	}
	/**
	 * @return The description of the Group.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return The displayName of this Group.
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName The displayName to set to this Group.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return The lastDirSyncTime of this Group.
	 */
	public String getLastDirSyncTime() {
		return lastDirSyncTime;
	}
	/**
	 * @param lastDirSyncTime The lastDirSyncTime to set to this Group.
	 */
	public void setLastDirSyncTime(String lastDirSyncTime) {
		this.lastDirSyncTime = lastDirSyncTime;
	}
	/**
	 * @return The mail attribute of this Group.
	 */
	public String getMail() {
		return this.mail;
	}
	/**
	 * @param mail The mail to set to this Group.
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}
	/**
	 * @return The mailEnabled attribute of this Group.
	 */
	public String getMailEnabled() {
		return mailEnabled;
	}
	/**
	 * @param mailEnabled The mailEnabled to set to this Group.
	 */
	public void setMailEnabled(String mailEnabled) {
		this.mailEnabled = mailEnabled;
	}
	/**
	 * @return The securityEnabled attribute of this Group.
	 */
	public String getSecurityEnabled() {
		return securityEnabled;
	}
	/**
	 * @param securityEnabled The securityEnabled to set to this Group.
	 */
	public void setSecurityEnabled(String securityEnabled) {
		this.securityEnabled = securityEnabled;
	}
	public String getMailNickname() {
		return mailNickname;
	}
	public void setMailNickname(String mailNickname) {
		this.mailNickname = mailNickname;
	}
	

}
