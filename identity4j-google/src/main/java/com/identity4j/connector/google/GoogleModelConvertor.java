package com.identity4j.connector.google;

/*
 * #%L
 * Identity4J GOOGLE
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

import java.util.Arrays;
import java.util.Date;

import com.google.api.client.util.DateTime;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.UserEmail;
import com.google.api.services.admin.directory.model.UserName;
import com.google.api.services.admin.directory.model.UserPhone;
import com.identity4j.connector.Media;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.StringUtil;

/**
 * Converter helper class, helps in converting from Google User POJO
 * to GoogleIdentity and vice versa.
 * 
 * @author gaurav
 *
 */
class GoogleModelConvertor {

	/**
	 * Helper utility which converts Google data store user instance to Identity instance.
	 * Attributes supported by Google but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. CustomerId can be retrieved as an attribute by name "customerId".
	 * <br/>
	 * <p>
	 * Please refer <a href="https://developers.google.com/admin-sdk/directory/v1/guides/manage-users">User</a> for more details.
	 * </p>
	 * 
	 * @param user
	 * @return converted GoogleIdentity instance from User instance
	 */
	public static GoogleIdentity googleUserToGoogleIdentity(User user){
		GoogleIdentity googleIdentity = new GoogleIdentity(user.getId(),user.getPrimaryEmail());
		
		googleIdentity.setFullName(user.getName() == null ? null : user.getName().getFullName());
		googleIdentity.setAddress(Media.email, user.getPrimaryEmail());
		
		// TODO how?
//		if(user.getPhones() != null && !user.getPhones().isEmpty())
//			googleIdentity.setAddress(Media.mobile, user.getPhones().get(0).getValue());
		
		if(user.getChangePasswordAtNextLogin() != null){
			googleIdentity.getPasswordStatus().setNeedChange(user.getChangePasswordAtNextLogin());
			if(user.getChangePasswordAtNextLogin())
				googleIdentity.getPasswordStatus().setType(PasswordStatusType.changeRequired);
			else
				googleIdentity.getPasswordStatus().calculateType();
		}
		else
			googleIdentity.getPasswordStatus().calculateType();
		
		if(user.getSuspended() != null)
			googleIdentity.getAccountStatus().setDisabled(user.getSuspended());
		googleIdentity.getAccountStatus().calculateType();
		
		if(user.getLastLoginTime() != null)
			googleIdentity.setLastSignOnDate(new Date(user.getLastLoginTime().getValue()));
		
		googleIdentity.setAttribute("customerId", user.getCustomerId()); 
		googleIdentity.setAttribute("etag", user.getEtag()); 
		googleIdentity.setAttribute("surname", user.getName().getFamilyName());  
		googleIdentity.setAttribute("givenName", user.getName().getGivenName());
		googleIdentity.setAttribute("orgUnitPath", user.getOrgUnitPath());  
		
		
		// This should always be false
		googleIdentity.setSystem(false);
		
		
		return googleIdentity;
	}
	
	/**
	 * Helper utility which converts Identity instance to Google data store user instance.
	 * Attributes supported by Google but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. CustomerId can be retrieved as an attribute by name "customerId".
	 * <br/>
	 * <p>
	 * Please refer <a href="https://developers.google.com/admin-sdk/directory/v1/guides/manage-users">User</a> for more details.
	 * </p>
	 * 
	 * @param googleIdentity
	 * @return converted User instance from GoogleIdentity instance
	 */
	public static User googleIdentityToUser(Identity googleIdentity){
		User user = new User();
		
		user.setId(googleIdentity.getGuid());
		
		String givenName = googleIdentity.getAttribute("givenName");
        String surname = googleIdentity.getAttribute("surname");
        String fullName = googleIdentity.getFullName();
        
        if(StringUtil.isNullOrEmpty(surname) || StringUtil.isNullOrEmpty(givenName)) {
            givenName = StringUtil.getBefore(fullName, " ");
            String sn = StringUtil.getAfter(fullName, " ");
            if(!sn.equals(fullName))
                surname = sn;
        }
        
        String principalName = googleIdentity.getPrincipalName();
        user.setPrimaryEmail(principalName).
					setName(new UserName().
					setGivenName(givenName).
					setFamilyName(surname).
					setFullName(fullName)).
					setEmails(Arrays.asList(new UserEmail().setAddress(principalName).
					setPrimary(true)));
		
		if(googleIdentity.getAddress(Media.mobile) != null)
			user.setPhones(Arrays.asList(new UserPhone().setValue(googleIdentity.getAddress(Media.mobile))));
		
		if(googleIdentity.getLastSignOnDate() != null)
			user.setLastLoginTime(new DateTime(googleIdentity.getLastSignOnDate()));
		
		if(!StringUtil.isNullOrEmpty(googleIdentity.getAttribute("orgUnitPath"))){
			user.setOrgUnitPath(googleIdentity.getAttribute("orgUnitPath"));
		}
		
		return user;
	}
	
	/**
	 * Helper utility which converts Role instance to Google data store Group instance.
	 * Groups in google are represented by email ids and guid is auto generated.
	 * <br/>
	 * Attributes supported by Google but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">Groups</a> 
	 * </p>
	 * 
	 * @param role
	 * @return Group instance converted from Role instance
	 */
	public static Group roleToGroup(Role role){
		Group group = new Group();
		group.setName(role.getPrincipalName());
		group.setId(role.getGuid());
		group.setEmail(role.getAttribute("email"));
		group.setDescription(role.getAttribute("description"));
		return group;
	}
	
	/**
	 * Helper utility which converts Google data store Group instance to Role instance.
	 * Groups in google are represented by email ids and guid is auto generated.
	 * <br/>
	 * Attributes supported by Google but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="https://developers.google.com/admin-sdk/directory/v1/guides/manage-groups">Groups</a> 
	 * </p>
	 * 
	 * @param group
	 * @return Role instance converted from Group instance
	 */
	public static Role groupToRole(Group group){
		Role role = new RoleImpl(group.getId(), group.getName());
		role.setAttribute("email", group.getEmail());
		role.setAttribute("description", group.getDescription());
		return role;
	}
}
