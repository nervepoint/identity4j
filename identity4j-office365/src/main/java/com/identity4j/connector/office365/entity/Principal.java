package com.identity4j.connector.office365.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Principal {

	private String id;
	private String objectId;
	private String dirSyncEnabled;
	private String displayName;
	private String mail;
	private String mailNickname;
	
	protected Principal() {
	}
	
	protected Principal(Principal from) {
		id = from.id;
		objectId = from.objectId;
		dirSyncEnabled = from.dirSyncEnabled;
		displayName = from.displayName;
		mail = from.mail;
		mailNickname = from.mailNickname;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		setObjectId(id);
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * @return the objectId
	 */
	public String getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
		this.id = this.objectId;
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

	public String getMailNickname() {
		return mailNickname;
	}

	public void setMailNickname(String mailNickname) {
		this.mailNickname = mailNickname;
	}
}
