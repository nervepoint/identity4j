package com.identity4j.connector.office365.entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 
 * This class represents a Role Object of the WAAD top level entity Role. Also, this class gives a publicly available
 * access mechanism to access each individual member variables such as Object Id, DisplayName etc of the particular Role
 * an instance of this class is associated with.
 * 
 *  @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Role {
	/**
	 * The private member variables of this Role.
	 */
	private String objectId;	
	private String description;
	private String displayName;
	private String builtin;
	private String roleDisabled;
	/**
	 * @return The objectId of this Role.
	 */
	public String getObjectId() {
		return objectId;
	}
	/**
	 * @param objectId The objectId to set to this Role.
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return The description of this Role.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set to this Role.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return The displayName of this Role.
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName The displayName to set to this Role.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return The builtin attribute of this Role.
	 */
	public String getBuiltin() {
		return this.builtin;
	}
	/**
	 * @param bIn The builtin to set to this Role.
	 */
	public void setBuiltin(String bIn) {
		this.builtin = bIn;
	}
	/**
	 * @return The roleDisabled attribute of this Role.
	 */
	public String getRoleDisabled() {
		return roleDisabled;
	}
	/**
	 * @param roleDisabled The roleDisabled attribute to set to this Role.
	 */
	public void setRoleDisabled(String roleDisabled) {
		this.roleDisabled = roleDisabled;
	}
	
}
