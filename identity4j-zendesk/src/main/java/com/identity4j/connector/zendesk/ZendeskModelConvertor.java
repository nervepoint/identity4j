package com.identity4j.connector.zendesk;

import java.util.ArrayList;
import java.util.List;

import com.identity4j.connector.Media;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.connector.zendesk.entity.Group;
import com.identity4j.connector.zendesk.entity.GroupMembership;
import com.identity4j.connector.zendesk.entity.GroupMemberships;
import com.identity4j.connector.zendesk.entity.User;
import com.identity4j.util.StringUtil;

/**
 * Converter helper class, helps in converting from Zendesk User POJO (Data Model)
 * to ZendeskIdentity and vice versa.
 * 
 * @author gaurav
 *
 */
public class ZendeskModelConvertor {

	/**
	 * Group properties
	 */
	public static final String GROUP_MEMBERSHIP_ID = "GroupMembershipId";
	public static final String GROUP_CREATED_AT = "GroupCreatedAt";
	public static final String GROUP_UPDATED_AT = "GroupUpdatedAt";
	public static final String GROUP_URL = "GroupUrl";
	public static final String GROUP_DELETED = "GroupDeleted";
	
	/**
	 * User properties
	 */
	public static final String USER_ORGANIZATION_ID = "UserOrganizationId";
	public static final String USER_LOCALE = "UserLocale";
	public static final String USER_LOCALE_ID = "UserLocaleId";
	public static final String USER_ROLE = "UserRole";
	public static final String USER_MODERATOR = "UserModerator";
	public static final String USER_VERIFIED = "UserVerified";
	public static final String USER_TIMEZONE = "UserTimezone";
	public static final String USER_SUSPENDED = "UserSuspended";
	public static final String USER_ACTIVE = "UserActive";
	public static final String USER_CREATED_AT = "UserCreatedAt";
	public static final String USER_UPDATED_AT = "UserUpdatedAt";
	
	
	private ZendeskModelConvertor zendeskModelConvertor;
	
	private ZendeskModelConvertor(){}

	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final ZendeskModelConvertor INSTANCE = new ZendeskModelConvertor();
	}
 
	public static ZendeskModelConvertor getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	public ZendeskModelConvertor getZendeskModelConvertor() {
		return zendeskModelConvertor;
	}

	/**
	 * Helper utility which converts Identity instance to Zendesk data store user instance.
	 * Attributes supported by Zendesk but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. role can be retrieved as an attribute by name "role".
	 * <br/>
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html">User REST operations</a>.
	 * </p>
	 * 
	 * @param zendeskIdentity
	 * @return converted User instance from ZendeskIdentity instance
	 */
	public User convertZendeskIdentityToZendeskUser(ZendeskIdentity zendeskIdentity){
		User user = new User();
		
		user.setEmail(zendeskIdentity.getPrincipalName());
		user.setPhone(zendeskIdentity.getAddress(Media.mobile));
		
		if(zendeskIdentity.getGuid() != null)
			user.setId(Integer.parseInt(zendeskIdentity.getGuid()));
		user.setName(zendeskIdentity.getFullName());
		
		if(zendeskIdentity.getAccountStatus() != null)
			user.setActive(!zendeskIdentity.getAccountStatus().isDisabled());
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_CREATED_AT)))
			user.setCreatedAt(zendeskIdentity.getAttribute(USER_CREATED_AT));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_UPDATED_AT)))
			user.setUpdatedAt(zendeskIdentity.getAttribute(USER_UPDATED_AT));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_LOCALE)))
			user.setLocale(zendeskIdentity.getAttribute(USER_LOCALE));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_LOCALE_ID)))
			user.setLocaleId(Integer.parseInt(zendeskIdentity.getAttribute(USER_LOCALE_ID)));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_ACTIVE)))
			user.setActive(Boolean.valueOf(zendeskIdentity.getAttribute(USER_ACTIVE)));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_SUSPENDED)))
			user.setSuspended(Boolean.valueOf(zendeskIdentity.getAttribute(USER_SUSPENDED)));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_MODERATOR)))
			user.setModerator(Boolean.valueOf(zendeskIdentity.getAttribute(USER_MODERATOR)));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_ROLE)))
			user.setRole(zendeskIdentity.getAttribute(USER_ROLE));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_TIMEZONE)))
			user.setTimezone(zendeskIdentity.getAttribute(USER_TIMEZONE));
		if(!StringUtil.isNullOrEmpty(zendeskIdentity.getAttribute(USER_VERIFIED)))
			user.setVerified(Boolean.valueOf(zendeskIdentity.getAttribute(USER_VERIFIED)));
		
		Role[] roles = zendeskIdentity.getRoles();
		
		GroupMemberships groupMemberships = new GroupMemberships();
		groupMemberships.setGroupMemberships(new ArrayList<GroupMembership>());
		
		if(roles != null){
			for (Role role : roles) {
				groupMemberships.getGroupMemberships().add(roleToGroupMembership(role));
			}
			user.setGroupMemberships(groupMemberships);
		}
		
		return user;
	}
	
	/**
	 * Helper utility which converts Zendesk data store User instance to Identity instance.
	 * Attributes supported by Zendesk but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. role can be retrieved as an attribute by name "role".
	 * <br/>
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/users.html">User REST operations</a>.
	 * </p>
	 * 
	 * @param user
	 * @return converted ZendeskIdentity instance from User instance
	 */
	public ZendeskIdentity convertZendeskUserToZendeskIdentity(User user){
		ZendeskIdentity identity = new ZendeskIdentity(user.getId().toString(),user.getEmail());
		
		identity.setFullName(user.getName());
		identity.setOtherName(user.getName());
		identity.setAddress(Media.email, user.getEmail());
		
		identity.setAttribute(USER_CREATED_AT, user.getCreatedAt());
		identity.setAttribute(USER_UPDATED_AT, user.getUpdatedAt());
		identity.setAttribute(USER_LOCALE, user.getLocale());
		identity.setAttribute(USER_LOCALE_ID, user.getLocaleId().toString());
		identity.setAttribute(USER_ACTIVE, user.getActive().toString());
		identity.setAttribute(USER_SUSPENDED, user.getSuspended().toString());
		identity.setAttribute(USER_MODERATOR, user.getModerator().toString());
		identity.setAttribute(USER_ROLE, user.getRole());
		identity.setAttribute(USER_TIMEZONE, user.getTimezone());
		identity.setAttribute(USER_VERIFIED, user.getVerified().toString());
		
		if(user.getPhone() != null)
			identity.setAddress(Media.mobile, user.getPhone());
		
		identity.setAccountStatus(new AccountStatus());
		
		if(user.getActive() != null)
			identity.getAccountStatus().setDisabled(user.getSuspended());
		
		List<Role> roles = new ArrayList<Role>();
		
		GroupMemberships groupMemberships = user.getGroupMemberships();
		
		if(groupMemberships != null && groupMemberships.getGroupMemberships() != null){
			for (GroupMembership groupMembership : groupMemberships.getGroupMemberships()) {
				roles.add(groupMembershipToRole(groupMembership));
			}
		}
		
		identity.setRoles(roles.toArray(new Role[0]));
		
		return identity;
	}
	
	/**
	 * Helper utility which converts Role instance to Zendesk data store Group instance.
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Zendesk but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @param role
	 * @return Group instance converted from Role instance
	 */
	public Group roleToGroup(Role role){
		Group group = new Group();
		group.setName(role.getPrincipalName());
		if(role.getGuid() != null)
			group.setId(Integer.parseInt(role.getGuid()));
		group.setCreatedAt(role.getAttribute(GROUP_CREATED_AT));
		group.setUpdatedAt(role.getAttribute(GROUP_UPDATED_AT));
		group.setUrl(role.getAttribute(GROUP_URL));
		if(role.getAttribute(GROUP_DELETED) != null)
			group.setDeleted(Boolean.getBoolean(role.getAttribute(GROUP_DELETED)));
		return group;
	}
	
	/**
	 * Helper utility which converts Zendesk data store Group instance to Role instance.
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Zendesk but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/groups.html">Group REST operations</a>.
	 * </p>
	 * 
	 * @param group
	 * @return Role instance converted from Group instance
	 */
	public Role groupToRole(Group group){
		Role role = new RoleImpl(group.getId().toString(), group.getName());
		role.setAttribute(GROUP_CREATED_AT, group.getCreatedAt());
		role.setAttribute(GROUP_UPDATED_AT, group.getUpdatedAt());
		role.setAttribute(GROUP_URL, group.getUrl());
		role.setAttribute(GROUP_DELETED, group.getDeleted().toString());
		return role;
	}
	
	
	/**
	 * Helper utility which converts Role instance to Zendesk data store GroupMembership instance.
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Zendesk but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/group_memberships.html">GroupMembership REST operations</a>.
	 * </p>
	 * 
	 * @param role
	 * @return GroupMembership instance converted from Role instance
	 */
	public GroupMembership roleToGroupMembership(Role role){
		GroupMembership groupMembership = new GroupMembership();
		if(!StringUtil.isNullOrEmpty(role.getAttribute(GROUP_MEMBERSHIP_ID)))
			groupMembership.setId(Integer.parseInt(role.getAttribute(GROUP_MEMBERSHIP_ID)));
		
		Group group = new Group();
		
		String guid = role.getGuid();
		if(!StringUtil.isNullOrEmpty(guid)){
			groupMembership.setGroupId(Integer.parseInt(guid));
			group.setId(Integer.parseInt(guid));
		}
		
		
		group.setName(role.getPrincipalName());
		group.setCreatedAt(role.getAttribute(GROUP_CREATED_AT));
		group.setUpdatedAt(role.getAttribute(GROUP_UPDATED_AT));
		group.setUrl(GROUP_URL);
		groupMembership.setGroup(group);
		return groupMembership;
	}
	
	
	/**
	 * Helper utility which converts Zendesk data store GroupMembership instance to Role instance.
	 * <b>Note:</b> Role in Zendesk is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Zendesk but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://developer.zendesk.com/documentation/rest_api/group_memberships.html">GroupMembership REST operations</a>.
	 * </p>
	 * 
	 * @param groupMembership
	 * @return Role instance converted from GroupMembership instance
	 */
	public Role groupMembershipToRole(GroupMembership groupMembership){
		Role role = new RoleImpl(groupMembership.getGroup().getId().toString(), groupMembership.getGroup().getName());
		role.setAttribute(GROUP_MEMBERSHIP_ID, groupMembership.getId().toString());
		role.setAttribute(GROUP_CREATED_AT, groupMembership.getGroup().getCreatedAt());
		role.setAttribute(GROUP_UPDATED_AT, groupMembership.getGroup().getUpdatedAt());
		role.setAttribute(GROUP_URL, groupMembership.getGroup().getUrl());
		return role;
	}

}
