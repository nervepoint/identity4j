package com.identity4j.connector.salesforce.entity;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents User entity in Salesforce Datastore.
 * 
 * @author gaurav
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class User {

	@JsonProperty("Id")
	private String id;
	@JsonProperty("Username")
	private String username;
	@JsonProperty("FirstName")
	private String firstName;
	@JsonProperty("LastName")
	private String lastName;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("CompanyName")
	private String companyName;
	@JsonProperty("Division")
	private String division;
	@JsonProperty("Department")
	private String department;
	@JsonProperty("Title")
	private String title;
	@JsonProperty("Street")
	private String street;
	@JsonProperty("City")
	private String city;
	@JsonProperty("State")
	private String state;
	@JsonProperty("PostalCode")
	private String postalCode;
	@JsonProperty("Country")
	private String country;
	@JsonProperty("Email")
	private String email;
	@JsonProperty("Phone")
	private String phone;
	@JsonProperty("MobilePhone")
	private String mobilePhone;
	@JsonProperty("Fax")
	private String fax;
	@JsonProperty("Alias")
	private String alias;
	@JsonProperty("TimeZoneSidKey")
	private String timeZoneSidKey;
	@JsonProperty("LocaleSidKey")
	private String localeSidKey;
	@JsonProperty("EmailEncodingKey")
	private String emailEncodingKey;
	@JsonProperty("IsActive")
	private Boolean isActive;
	@JsonProperty("ProfileId")
	private String profileId;
	@JsonProperty("LanguageLocaleKey")
	private String languageLocaleKey;
	@JsonProperty("LastLoginDate")
	private String lastLoginDate;
	@JsonProperty("LastPasswordChangeDate")
	private String lastPasswordChangeDate;
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
	@JsonProperty("UserPermissionsSFContentUser")
	private Boolean userPermissionsSFContentUser = Boolean.FALSE;
	@JsonIgnore
	private String password;
	@JsonIgnore
	private GroupMembers groupMembers;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getTimeZoneSidKey() {
		return timeZoneSidKey;
	}
	public void setTimeZoneSidKey(String timeZoneSidKey) {
		this.timeZoneSidKey = timeZoneSidKey;
	}
	public String getLocaleSidKey() {
		return localeSidKey;
	}
	public void setLocaleSidKey(String localeSidKey) {
		this.localeSidKey = localeSidKey;
	}
	public String getEmailEncodingKey() {
		return emailEncodingKey;
	}
	public void setEmailEncodingKey(String emailEncodingKey) {
		this.emailEncodingKey = emailEncodingKey;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public String getProfileId() {
		return profileId;
	}
	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}
	public String getLanguageLocaleKey() {
		return languageLocaleKey;
	}
	public void setLanguageLocaleKey(String languageLocaleKey) {
		this.languageLocaleKey = languageLocaleKey;
	}
	public String getLastLoginDate() {
		return lastLoginDate;
	}
	public void setLastLoginDate(String lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}
	public String getLastPasswordChangeDate() {
		return lastPasswordChangeDate;
	}
	public void setLastPasswordChangeDate(String lastPasswordChangeDate) {
		this.lastPasswordChangeDate = lastPasswordChangeDate;
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public GroupMembers getGroupMembers() {
		return groupMembers;
	}
	public void setGroupMembers(GroupMembers groupMembers) {
		this.groupMembers = groupMembers;
	}
	public Boolean getUserPermissionsSFContentUser() {
		return userPermissionsSFContentUser;
	}
	public void setUserPermissionsSFContentUser(
			Boolean userPermissionsSFContentUser) {
		this.userPermissionsSFContentUser = userPermissionsSFContentUser;
	}
	
}
