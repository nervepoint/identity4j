package com.identity4j.connector.office365;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.identity4j.connector.Media;
import com.identity4j.connector.office365.entity.Group;
import com.identity4j.connector.office365.entity.User;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.StringUtil;

/**
 * Converter helper class, helps in converting from Office365 User POJO (Data Model)
 * to Office365Identity and vice versa.
 * 
 * @author gaurav
 *
 */
public class Office365ModelConvertor {

	public static final String ATTR_GIVEN_NAME = "givenName";
	public static final String ATTR_MOBILE = "mobile";
	public static final String ATTR_MAIL = "mail";
	public static final String ATTR_SURNAME = "surname";
	public static final String ATTR_JOB_TITLE = "jobTitle";
	public static final String ATTR_DEPARTMENT = "department";
	public static final String ATTR_PREF_LANG = "prefLang";
	public static final String ATTR_USAGE_LOCATION = "usageLocation";
	public static final String ATTR_DELIVERY_OFFICE = "deliveryOffice";
	public static final String ATTR_STREET_ADDRESS = "streetAddress";
	public static final String ATTR_CITY = "city";
	public static final String ATTR_POSTAL_CODE = "postalCode";
	public static final String ATTR_COUNTRY = "country";
	public static final String ATTR_TELEPHONE = "telephone";
	public static final String ATTR_FAX = "fax";
	
	/**
	 * Helper utility which converts Identity instance to Office365 data store user instance.
	 * Attributes supported by Office 365 but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. Email can be retrieved as an attribute by name "email".
	 * <br/>
	 * <p>
	 * Please refer <a href="http://msdn.microsoft.com/en-us/library/dn130116.aspx">User</a> for more details.
	 * </p>
	 * 
	 * @param office365Identity
	 * @return converted User instance from Office365Identity instance
	 */
	public static User covertOfficeIdentityToOffice365User(Identity office365Identity){
		User user = new User();
		user.setMail(emptyStringToNull(office365Identity.getAttribute(ATTR_MAIL)));
		user.setMailNickname(emptyStringToNull(office365Identity.getPrincipalName().split("@")[0]).replace(" ", ""));
		user.setMobilePhone(emptyStringToNull(office365Identity.getAttribute(ATTR_MOBILE)));
		user.setObjectId(office365Identity.getGuid());
		user.setUserPrincipalName(office365Identity.getPrincipalName());
		user.setDisplayName(emptyStringToNull(office365Identity.getFullName()));
		
		user.setAccountEnabled(!office365Identity.getAccountStatus().isDisabled());
		
		user.setGivenName(emptyStringToNull(office365Identity.getAttribute(ATTR_GIVEN_NAME)));
		user.setSurname(emptyStringToNull(office365Identity.getAttribute(ATTR_SURNAME)));
		
		
		user.setJobTitle(emptyStringToNull(office365Identity.getAttribute(ATTR_JOB_TITLE)));
		user.setDepartment(emptyStringToNull(office365Identity.getAttribute(ATTR_DEPARTMENT)));
		user.setPreferredLanguage(emptyStringToNull(office365Identity.getAttribute(ATTR_PREF_LANG)));
		user.setUsageLocation(emptyStringToNull(office365Identity.getAttribute(ATTR_USAGE_LOCATION)));
		
		user.setPhysicalDeliveryOfficeName(emptyStringToNull(office365Identity.getAttribute(ATTR_DELIVERY_OFFICE)));
		user.setStreetAddress(emptyStringToNull(office365Identity.getAttribute(ATTR_STREET_ADDRESS)));
		user.setCity(emptyStringToNull(office365Identity.getAttribute(ATTR_CITY)));
		user.setPostalCode(emptyStringToNull(office365Identity.getAttribute(ATTR_POSTAL_CODE)));
		user.setCountry(emptyStringToNull(office365Identity.getAttribute(ATTR_COUNTRY)));
		
		user.setBusinessPhones(stringToArray(office365Identity.getAttribute(ATTR_TELEPHONE)));
		user.setFaxNumber(emptyStringToNull(office365Identity.getAttribute(ATTR_FAX)));
		
		
		Role[] roles = office365Identity.getRoles();
		user.setMemberOf(new ArrayList<Group>());
		for (Role role : roles) {
			user.addNewGroup(roleToGroup(role));
		}
		return user;
	}
	
	private static Date parseIso8601(String datetime) {
		if(datetime == null || datetime.equals(""))
			return null;
	    TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(datetime);
	    Instant i = Instant.from(ta);
	    return Date.from(i);
	}
	
	private static String arrayToString(String[] str) {
		return str != null ? String.join(",", str) : "";
	}
	
	private static String[] stringToArray(String str) {
		if(str == null || str.trim().equals("")) {
			return new String[0];
		}
		return str.split(",");
	}
	
	private static String emptyStringToNull(String str) {
		if(str!=null && str.trim().equals("")) {
			return null;
		}
		return str;
	}
	
	private static String nullToEmptyString(String str) {
		if(str==null) {
			return "";
		}
		return str;
	}
	/**
	 * Helper utility which converts Office 365 data store User instance to Identity instance.
	 * Attributes supported by Office 365 but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. Email can be retrieved as an attribute by name "email".
	 * <br/>
	 * <p>
	 * Please refer <a href="http://msdn.microsoft.com/en-us/library/dn130116.aspx">User</a> for more details.
	 * </p>
	 * 
	 * @param user
	 * @return converted Office365Identity instance from User instance
	 */
	public static Office365Identity convertOffice365UserToOfficeIdentity(User user){
		Office365Identity identity = new Office365Identity(user.getObjectId(),user.getUserPrincipalName());
		identity.setFullName(user.getDisplayName());

		identity.setAddress(Media.email, user.getMail());
		identity.setAddress(Media.mobile, user.getMobilePhone());
		
		identity.setAccountStatus(new AccountStatus());
		if(user.getAccountEnabled() != null)
			identity.getAccountStatus().setDisabled(!user.getAccountEnabled());
		identity.getAccountStatus().calculateType();
		
		identity.setPasswordStatus(new PasswordStatus());
		identity.getPasswordStatus().setLastChange(parseIso8601(user.getLastPasswordChangeDateTime()));
		identity.getPasswordStatus().calculateType();
		
		identity.setAttribute(ATTR_MOBILE, nullToEmptyString(user.getMobilePhone()));
		identity.setAttribute(ATTR_MAIL, nullToEmptyString(user.getMail()));
		identity.setAttribute(ATTR_GIVEN_NAME, nullToEmptyString(user.getGivenName()));
		identity.setAttribute(ATTR_SURNAME,nullToEmptyString(user.getSurname()));
		
		identity.setAttribute(ATTR_JOB_TITLE,nullToEmptyString(user.getJobTitle()));
		identity.setAttribute(ATTR_DEPARTMENT,nullToEmptyString(user.getDepartment()));
		identity.setAttribute(ATTR_PREF_LANG,nullToEmptyString(user.getPreferredLanguage()));
		identity.setAttribute(ATTR_USAGE_LOCATION,nullToEmptyString(user.getUsageLocation()));
		
		identity.setAttribute(ATTR_DELIVERY_OFFICE,nullToEmptyString(user.getPhysicalDeliveryOfficeName()));
		identity.setAttribute(ATTR_STREET_ADDRESS,nullToEmptyString(user.getStreetAddress()));
		identity.setAttribute(ATTR_CITY, nullToEmptyString(user.getCity()));
		identity.setAttribute(ATTR_POSTAL_CODE,nullToEmptyString(user.getPostalCode()));
		identity.setAttribute(ATTR_COUNTRY,nullToEmptyString(user.getCountry()));
		
		identity.setAttribute(ATTR_TELEPHONE,nullToEmptyString(arrayToString(user.getBusinessPhones())));
		identity.setAttribute(ATTR_FAX,nullToEmptyString(user.getFaxNumber()));
		
		List<Group> groups = user.getMemberOf();
		if(groups != null){
			for (Group group : groups) {
				identity.addRole(groupToRole(group));
			}
		}
		
		return identity;
	}
	
	
	/**
	 * Helper utility which converts Role instance to Office 365 data store Group instance.
	 * <b>Note:</b> Role in active directory is referred as Groups and identified by only guid, not group name.
	 * <br/>
	 * Attributes supported by Office 365 but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://msdn.microsoft.com/en-us/library/dn151610.aspx">Groups</a>.
	 * </p>
	 * 
	 * @param role
	 * @return Group instance converted from Role instance
	 */
	public static Group roleToGroup(Role role){
		Group group = new Group();
		group.setDisplayName(role.getPrincipalName());
		group.setObjectId(role.getGuid());
		group.setMailNickname(role.getAttribute("mailNickname"));
		group.setDescription(role.getAttribute("description"));

		if(!StringUtil.isNullOrEmpty(role.getAttribute("mailEnabled")))
			group.setMailEnabled(role.getAttribute("mailEnabled"));
		
		if(!StringUtil.isNullOrEmpty(role.getAttribute("securityEnabled")))
			group.setSecurityEnabled(role.getAttribute("securityEnabled"));
		
		return group;
	}
	
	/**
	 * Helper utility which converts Office data store Group instance to Role instance.
	 * <b>Note:</b> Role in active directory is referred as Groups and identified by only guid, not group name.
	 * <br/>
	 * Attributes supported by Office 365 but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://msdn.microsoft.com/en-us/library/dn151610.aspx">Groups</a>. 
	 * </p>
	 * 
	 * @param group
	 * @return Role instance converted from Group instance
	 */
	public static Role groupToRole(Group group){
		Role role = new RoleImpl(group.getObjectId(), group.getDisplayName());
		role.setAttribute("mail", group.getMail());
		role.setAttribute("mailNickname", group.getMailNickname());
		role.setAttribute("mailEnabled", group.getMailEnabled());
		role.setAttribute("securityEnabled", group.getSecurityEnabled());
		role.setAttribute("description", group.getDescription());
		return role;
	}
}
