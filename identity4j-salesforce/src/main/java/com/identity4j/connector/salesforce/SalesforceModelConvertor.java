package com.identity4j.connector.salesforce;

import java.util.ArrayList;
import java.util.List;

import com.identity4j.connector.Media;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.connector.salesforce.entity.Group;
import com.identity4j.connector.salesforce.entity.GroupMember;
import com.identity4j.connector.salesforce.entity.GroupMembers;
import com.identity4j.connector.salesforce.entity.User;
import com.identity4j.util.StringUtil;

/**
 * Converter helper class, helps in converting from Salesforce User POJO (Data Model)
 * to SalesforceIdentity and vice versa.
 * 
 * @author gaurav
 *
 */
public class SalesforceModelConvertor {

	/**
	 * Group properties
	 */
	public static final String EMAIL = "Email";
	public static final String DEVELOPER_NAME = "DeveloperName";
	public static final String CREATED_BY_ID = "CreatedById";
	public static final String CREATED_DATE = "CreatedDate";
	public static final String DOES_INCLUDE_BOSSES = "DoesIncludeBosses";
	public static final String DOES_SEND_EMAIL_TO_MEMBERS = "DoesSendEmailToMembers";
	public static final String LAST_MODIFIED_BY_ID = "LastModifiedById";
	public static final String LAST_MODIFIED_DATE = "LastModifiedDate";
	public static final String OWNER_ID = "OwnerId";
	public static final String RELATED_ID = "RelatedId";
	public static final String SYSTEM_MODSTAMP = "SystemModstamp";
	public static final String TYPE = "Type";
	public static final String GROUP_MEMBER_ID = "GroupMemberId";
	
	/**
	 * User properties
	 */
	public static final String PROFILE_ID = "ProfileId";
	public static final String USER_PERMISSIONS_SF_CONTENT_USER = "UserPermissionsSFContentUser";
	public static final String TIMEZONE_SID_KEY = "TimeZoneSidKey";
	public static final String LOCALE_SID_KEY = "LocaleSidKey";
	public static final String EMAIL_ENCODING_KEY = "EmailEncodingKey";
	public static final String LANGUAGE_LOCALE_KEY = "LanguageLocaleKey";
	
	private SalesforceModelConvertor salesforceModelConvertor;
	private SalesforceConfiguration configuration;
	
	private SalesforceModelConvertor(){}

	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final SalesforceModelConvertor INSTANCE = new SalesforceModelConvertor();
	}
 
	public static SalesforceModelConvertor getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	public void init(SalesforceConfiguration configuration){
		LazyHolder.INSTANCE.configuration = configuration;
	}
	
	public SalesforceModelConvertor getSalesforceModelConvertor() {
		return salesforceModelConvertor;
	}

	/**
	 * Helper utility which converts Identity instance to Salesforce data store user instance.
	 * Attributes supported by Salesforce but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. Department can be retrieved as an attribute by name "Department".
	 * <br/>
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @param salesforceIdentity
	 * @return converted User instance from SalesforceIdentity instance
	 */
	public User convertSalesforceIdentityToSalesforceUser(SalesforceIdentity salesforceIdentity){
		User user = new User();
		
		user.setEmail(salesforceIdentity.getAddress(Media.email));
		user.setMobilePhone(salesforceIdentity.getAddress(Media.mobile));
		
		user.setId(salesforceIdentity.getGuid());
		user.setUsername(salesforceIdentity.getPrincipalName());
		String[] names = salesforceIdentity.getFullName().split("\\s+");
		if(names.length == 1){
			user.setFirstName(names[0]);
		}else if(names.length == 2){
			user.setFirstName(names[0]);
			user.setLastName(names[1]);
		}
		user.setIsActive(!salesforceIdentity.getAccountStatus().isDisabled());
		
		//alias cannot be more than 8 chars
		user.setAlias(names[0].length() > 8 ? names[0].substring(0, 8).trim().toLowerCase() : names[0]);
		
		
		if(StringUtil.isNullOrEmpty(salesforceIdentity.getAttribute(TIMEZONE_SID_KEY))){
			user.setTimeZoneSidKey(configuration.getTimeZoneSidKey());
		}else{
			user.setTimeZoneSidKey(salesforceIdentity.getAttribute(TIMEZONE_SID_KEY));
		}
		
		
		if(StringUtil.isNullOrEmpty(salesforceIdentity.getAttribute(LOCALE_SID_KEY))){
			user.setLocaleSidKey(configuration.getLocaleSidKey());
		}else{
			user.setLocaleSidKey(salesforceIdentity.getAttribute(LOCALE_SID_KEY));
		}
		
		
		if(StringUtil.isNullOrEmpty(salesforceIdentity.getAttribute(EMAIL_ENCODING_KEY))){
			user.setEmailEncodingKey(configuration.getEmailEncodingKey());
		}else{
			user.setEmailEncodingKey(salesforceIdentity.getAttribute(EMAIL_ENCODING_KEY));
		}
		
		
		if(StringUtil.isNullOrEmpty(salesforceIdentity.getAttribute(LANGUAGE_LOCALE_KEY))){
			user.setLanguageLocaleKey(configuration.getLanguageLocaleKey());
		}else{
			user.setLanguageLocaleKey(salesforceIdentity.getAttribute(LANGUAGE_LOCALE_KEY));
		}
		
		
		if(StringUtil.isNullOrEmpty(salesforceIdentity.getAttribute(PROFILE_ID))){
			user.setProfileId(configuration.getProfileId());
		}else{
			user.setProfileId(salesforceIdentity.getAttribute(PROFILE_ID));
		}
		
		if(StringUtil.isNullOrEmpty(salesforceIdentity.getAttribute(USER_PERMISSIONS_SF_CONTENT_USER))){
			user.setUserPermissionsSFContentUser(false);
		}else{
			user.setUserPermissionsSFContentUser(Boolean.parseBoolean(salesforceIdentity.getAttribute(USER_PERMISSIONS_SF_CONTENT_USER)));
		}
		
		
		Role[] roles = salesforceIdentity.getRoles();
		
		GroupMembers groupMembers = new GroupMembers();
		groupMembers.setGroupMembers(new ArrayList<GroupMember>());
		
		if(roles != null){
			for (Role role : roles) {
				groupMembers.getGroupMembers().add(roleToGroupMember(role));
			}
			user.setGroupMembers(groupMembers);
		}
		
		return user;
	}
	
	/**
	 * Helper utility which converts Salesforce data store User instance to Identity instance.
	 * Attributes supported by Salesforce but not Identity are mapped via attributes property.
	 * <br />
	 * E.g. Department can be retrieved as an attribute by name "Department".
	 * <br/>
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @param user
	 * @return converted Salesforce instance from User instance
	 */
	public SalesforceIdentity convertSalesforceUserToSalesforceIdentity(User user){
		SalesforceIdentity identity = new SalesforceIdentity(user.getId(),user.getUsername());
		
		if(user.getFirstName() != null && user.getLastName() != null)
			identity.setFullName(user.getFirstName() + " " + user.getLastName());
		identity.setOtherName(user.getUsername());
		identity.setAddress(Media.email, user.getEmail());
		if( user.getMobilePhone() != null)
			identity.setAddress(Media.mobile, user.getMobilePhone().toString());
		identity.setAccountStatus(new AccountStatus());
		if(user.getIsActive() != null)
			identity.getAccountStatus().setDisabled(!user.getIsActive());
		
		identity.setAttribute(TIMEZONE_SID_KEY, user.getTimeZoneSidKey());
		identity.setAttribute(LOCALE_SID_KEY, user.getLocaleSidKey());
		identity.setAttribute(EMAIL_ENCODING_KEY, user.getEmailEncodingKey());
		identity.setAttribute(LANGUAGE_LOCALE_KEY, user.getLanguageLocaleKey());
		identity.setAttribute(PROFILE_ID, user.getProfileId());
		identity.setAttribute(USER_PERMISSIONS_SF_CONTENT_USER, user
				.getUserPermissionsSFContentUser() == null ? "" : user
				.getUserPermissionsSFContentUser().toString());
		
		List<Role> roles = new ArrayList<Role>();
		
		GroupMembers groupMembers = user.getGroupMembers();
		
		if(groupMembers != null && groupMembers.getGroupMembers() != null){
			for (GroupMember groupMember : groupMembers.getGroupMembers()) {
				roles.add(groupMemeberToRole(groupMember));
			}
		}
		
		identity.setRoles(roles.toArray(new Role[0]));
		
		return identity;
	}
	
	/**
	 * Helper utility which converts Role instance to Salesforce data store Group instance.
	 * <b>Note:</b> Role in Salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Salesforce but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @param role
	 * @return Group instance converted from Role instance
	 */
	public Group roleToGroup(Role role){
		Group group = new Group();
		group.setName(role.getPrincipalName());
		group.setId(role.getGuid());
		mapRoleAttributesToGroup(group, role);
		return group;
	}
	
	/**
	 * Helper utility which converts Salesforce data store Group instance to Role instance.
	 * <b>Note:</b> Role in salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Salesforce but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @param group
	 * @return Role instance converted from Group instance
	 */
	public Role groupToRole(Group group){
		/**
		 * Some of the system managed groups don't have name in salesforce.
		 * API at some places fails due to null pointer exception caused by no name,
		 * hence we use empty string.
		 */
		String groupName = group.getName() == null ? "" : group.getName();
		Role role = new RoleImpl(group.getId(), groupName);
		mapGroupAttributesToRole(group, role);
		return role;
	}
	
	
	/**
	 * Helper utility which converts Role instance to Salesforce data store GroupMember instance.
	 * <b>Note:</b> Role in Salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Salesforce but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @param role
	 * @return GroupMember instance converted from Role instance
	 */
	public GroupMember roleToGroupMember(Role role){
		GroupMember groupMember = new GroupMember();
		groupMember.setId(role.getAttribute(GROUP_MEMBER_ID));
		Group group = new Group();
		group.setName(role.getPrincipalName());
		group.setId(role.getGuid());
		mapRoleAttributesToGroup(group, role);
		groupMember.setGroup(group);
		groupMember.setUserOrGroupId(role.getGuid());
		return groupMember;
	}
	
	
	/**
	 * Helper utility which converts Salesforce data store GroupMember instance to Role instance.
	 * <b>Note:</b> Role in salesforce is referred as Groups and identified by guid and group name.
	 * <br/>
	 * Attributes supported by Salesforce but not Role are mapped via attributes property.
	 * 
	 * <p>
	 * Please refer <a href="http://www.salesforce.com/us/developer/docs/api_rest/Content/resources_list.htm">For generic REST operations</a>.
	 * </p>
	 * 
	 * @param groupMember
	 * @return Role instance converted from GroupMember instance
	 */
	public Role groupMemeberToRole(GroupMember groupMember){
		Role role = new RoleImpl(groupMember.getGroupId(), groupMember.getGroup().getName());
		mapGroupAttributesToRole(groupMember.getGroup(), role);
		role.setAttribute(GROUP_MEMBER_ID, groupMember.getId());
		return role;
	}
	
	

	/**
	 * Helper utility function which captures attributes not supported by Role and adds them 
	 * to its attributes map from Group instance.
	 * 
	 * @param group
	 * @param role
	 */
	private void mapGroupAttributesToRole(Group group, Role role) {
		role.setAttribute(EMAIL, group.getEmail());
		role.setAttribute(DEVELOPER_NAME, group.getDeveloperName());
		role.setAttribute(CREATED_BY_ID, group.getCreatedById());
		role.setAttribute(CREATED_DATE, group.getCreatedDate());
		if(group.getDoesIncludeBosses() != null)
			role.setAttribute(DOES_INCLUDE_BOSSES, group.getDoesIncludeBosses().toString());
		if(group.getDoesSendEmailToMembers() != null)
			role.setAttribute(DOES_SEND_EMAIL_TO_MEMBERS, group.getDoesSendEmailToMembers().toString());
		role.setAttribute(LAST_MODIFIED_BY_ID, group.getLastModifiedById());
		role.setAttribute(LAST_MODIFIED_DATE, group.getLastModifiedDate());
		role.setAttribute(OWNER_ID, group.getOwnerId());
		role.setAttribute(RELATED_ID, group.getRelatedId());
		role.setAttribute(SYSTEM_MODSTAMP, group.getSystemModstamp());
		role.setAttribute(TYPE, group.getType());
	}
	
	
	/**
	 * Helper utility function which captures attributes not supported by Role and sets them 
	 * to Group instance.
	 * 
	 * @param group
	 * @param role
	 */
	private void mapRoleAttributesToGroup(Group group, Role role) {
		if(!StringUtil.isNullOrEmpty(role.getAttribute(EMAIL)))
			group.setEmail(role.getAttribute(EMAIL));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(DEVELOPER_NAME)))
			group.setDeveloperName(role.getAttribute(DEVELOPER_NAME));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(CREATED_BY_ID)))
			group.setCreatedById(role.getAttribute(CREATED_BY_ID));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(CREATED_DATE)))
			group.setCreatedDate(role.getAttribute(CREATED_DATE));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(DOES_INCLUDE_BOSSES)))
			group.setDoesIncludeBosses(Boolean.parseBoolean(role.getAttribute(DOES_INCLUDE_BOSSES)));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(DOES_SEND_EMAIL_TO_MEMBERS)))
			group.setDoesSendEmailToMembers(Boolean.parseBoolean(role.getAttribute(DOES_SEND_EMAIL_TO_MEMBERS)));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(LAST_MODIFIED_BY_ID)))
			group.setLastModifiedById(role.getAttribute(LAST_MODIFIED_BY_ID));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(LAST_MODIFIED_DATE)))
			group.setLastModifiedDate(role.getAttribute(LAST_MODIFIED_DATE));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(OWNER_ID)))
			group.setOwnerId(role.getAttribute(OWNER_ID));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(RELATED_ID)))
			group.setRelatedId(role.getAttribute(RELATED_ID));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(SYSTEM_MODSTAMP)))
			group.setSystemModstamp(role.getAttribute(SYSTEM_MODSTAMP));
		if(!StringUtil.isNullOrEmpty(role.getAttribute(TYPE)))
			group.setType(role.getAttribute(TYPE));
	}
}
