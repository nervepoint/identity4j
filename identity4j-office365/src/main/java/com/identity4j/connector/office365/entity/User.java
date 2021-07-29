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

@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends Principal {
	/**
	 * The following are the individual private members of a User object that holds
	 * a particular simple attribute of an User object.
	 */
	private String objectReference;
	private String objectType;
	private Boolean accountEnabled;
	private String city;
	private String country;
	private String department;
	private String givenName;
	private String jobTitle;
	private String passwordPolicies;
	private String physicalDeliveryOfficeName;
	private String postalCode;
	private String preferredLanguage;
	private String state;
	private String streetAddress;
	private String surname;
	private String usageLocation;
	private String userPrincipalName;
	private PasswordProfile passwordProfile;
	
	/**
	 * MS Graph  API
	 */
	private String onPremisesSyncEnabled;
	private String faxNumber;
	private String onPremisesImmutableId;
	private String onPremisesLastSyncDateTime;
	private String mobilePhone;
	private String signinSessionsValidFromDateTime;
	private String[] businessPhones;
	private String lastPasswordChangeDateTime;

	/**
	 * The list groups holds a list of group entity this user belongs to.
	 */
	private List<Group> memberOf;

	/**
	 * The list roles holds a list of role entity this user belongs to.
	 */
	private List<Role> roles;

	/**
	 * The constructor for the User class. Initializes the dynamic lists and manager
	 * variables.
	 */
	public User() {
		passwordProfile = new PasswordProfile();
	}

	/**
	 * @return the objectReference
	 */
	public String getObjectReference() {
		return objectReference;
	}

	/**
	 * @param objectReference the objectReference to set
	 */
	public void setObjectReference(String objectReference) {
		this.objectReference = objectReference;
	}

	/**
	 * @return the objectType
	 */
	public String getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType the objectType to set
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the accountEnabled
	 */
	public Boolean getAccountEnabled() {
		return accountEnabled;
	}

	/**
	 * @param accountEnabled the accountEnabled to set
	 */
	public void setAccountEnabled(Boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * @param department the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @param givenName the givenName to set
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return the jobTitle
	 */
	public String getJobTitle() {
		return jobTitle;
	}

	/**
	 * @param jobTitle the jobTitle to set
	 */
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	/**
	 * @return the passwordPolicies
	 */
	public String getPasswordPolicies() {
		return passwordPolicies;
	}

	/**
	 * @param passwordPolicies the passwordPolicies to set
	 */
	public void setPasswordPolicies(String passwordPolicies) {
		this.passwordPolicies = passwordPolicies;
	}

	/**
	 * @return the physicalDeliveryOfficeName
	 */
	public String getPhysicalDeliveryOfficeName() {
		return physicalDeliveryOfficeName;
	}

	/**
	 * @param physicalDeliveryOfficeName the physicalDeliveryOfficeName to set
	 */
	public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName) {
		this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
	}

	/**
	 * @return the postalCode
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * @param postalCode the postalCode to set
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * @return the preferredLanguage
	 */
	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	/**
	 * @param preferredLanguage the preferredLanguage to set
	 */
	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the streetAddress
	 */
	public String getStreetAddress() {
		return streetAddress;
	}

	/**
	 * @param streetAddress the streetAddress to set
	 */
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * @return the usageLocation
	 */
	public String getUsageLocation() {
		return usageLocation;
	}

	/**
	 * @param usageLocation the usageLocation to set
	 */
	public void setUsageLocation(String usageLocation) {
		this.usageLocation = usageLocation;
	}

	/**
	 * @return the userPrincipalName
	 */
	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	/**
	 * @param userPrincipalName the userPrincipalName to set
	 */
	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}

	/**
	 * @return the groups
	 */
	public List<Group> getMemberOf() {
		return memberOf;
	}

	/**
	 * @param memberOf the groups to set
	 */
	public void setMemberOf(List<Group> memberOf) {
		this.memberOf = memberOf;
	}

	/**
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public void addNewGroup(Group group) {
		this.memberOf.add(group);
	}

	/**
	 * @param index The index of the Group Entry.
	 * @return The ObjectId of the Group entry at index.
	 */

	public String getGroupObjectId(int index) {
		return this.memberOf.get(index).getObjectId();
	}

	/**
	 * @param index The index of the Group Entry.
	 * @return The DisplayName of the Group entry at index.
	 */

	public String getGroupDisplayName(int index) {
		return this.memberOf.get(index).getDisplayName();
	}

	/**
	 * @param index The index of the Roles Entry.
	 * @return The ObjectId of the Role entry at index.
	 */

	public String getRoleObjectId(int index) {
		return this.roles.get(index).getObjectId();
	}

	/**
	 * @param index The index of the Roles Entry.
	 * @return The DisplayName of the Roles entry at index.
	 */

	public String getRoleDisplayName(int index) {
		return this.roles.get(index).getDisplayName();
	}

	public void addNewRole(Role role) {
		this.roles.add(role);
	}

	public PasswordProfile getPasswordProfile() {
		return passwordProfile;
	}

	public void setPasswordProfile(PasswordProfile passwordProfile) {
		this.passwordProfile = passwordProfile;
	}
	
	public String getOnPremisesSyncEnabled() {
		return onPremisesSyncEnabled;
	}

	public void setOnPremisesSyncEnabled(String onPremisesSyncEnabled) {
		this.onPremisesSyncEnabled = onPremisesSyncEnabled;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public String getOnPremisesImmutableId() {
		return onPremisesImmutableId;
	}

	public void setOnPremisesImmutableId(String onPremisesImmutableId) {
		this.onPremisesImmutableId = onPremisesImmutableId;
	}

	public String getOnPremisesLastSyncDateTime() {
		return onPremisesLastSyncDateTime;
	}

	public void setOnPremisesLastSyncDateTime(String onPremisesLastSyncDateTime) {
		this.onPremisesLastSyncDateTime = onPremisesLastSyncDateTime;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getSigninSessionsValidFromDateTime() {
		return signinSessionsValidFromDateTime;
	}

	public void setSigninSessionsValidFromDateTime(String signinSessionsValidFromDateTime) {
		this.signinSessionsValidFromDateTime = signinSessionsValidFromDateTime;
	}

	public String[] getBusinessPhones() {
		return businessPhones;
	}

	public void setBusinessPhones(String[] businessPhones) {
		this.businessPhones = businessPhones;
	}

	public String getLastPasswordChangeDateTime() {
		return lastPasswordChangeDateTime;
	}

	public void setLastPasswordChangeDateTime(String lastPasswordChangeDateTime) {
		this.lastPasswordChangeDateTime = lastPasswordChangeDateTime;
	}



	public static class PasswordProfile {
		private String password;
		private Boolean forceChangePasswordNextLogin;

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Boolean getForceChangePasswordNextLogin() {
			return forceChangePasswordNextLogin;
		}

		public void setForceChangePasswordNextLogin(Boolean forceChangePasswordNextLogin) {
			this.forceChangePasswordNextLogin = forceChangePasswordNextLogin;
		}

	}

}
