package com.identity4j.connector.salesforce.services;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.identity4j.connector.PrincipalType;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.salesforce.SalesforceConfiguration;
import com.identity4j.connector.salesforce.entity.Group;
import com.identity4j.connector.salesforce.entity.GroupMember;
import com.identity4j.connector.salesforce.entity.GroupMembers;
import com.identity4j.connector.salesforce.entity.User;
import com.identity4j.connector.salesforce.entity.Users;
import com.identity4j.connector.salesforce.services.token.handler.SalesforceAuthorizationHelper;
import com.identity4j.connector.salesforce.services.token.handler.Token;
import com.identity4j.util.StringUtil;
import com.identity4j.util.http.HttpPair;
import com.identity4j.util.http.HttpResponse;
import com.identity4j.util.http.request.HttpRequestHandler;
import com.identity4j.util.json.JsonMapperService;

/**
 * This class is responsible for managing REST calls for User entity.
 * 
 * @author gaurav
 *
 */
public class UserService extends AbstractRestAPIService{
	
	private static final Log log = LogFactory.getLog(UserService.class);
	
	/**
	 * User attributes represented by User entity in Salesforce data source
	 */
	private static final String USER_ATTRIBUTES = "Id,Username,LastName,FirstName,Name,CompanyName,Division,Department,Title,Street,City,State,"
			+ "PostalCode,Country,Latitude,Longitude,Email,EmailPreferencesAutoBcc,EmailPreferencesAutoBccStayInTouch,"
			+ "EmailPreferencesStayInTouchReminder,SenderEmail,SenderName,Signature,StayInTouchSubject,StayInTouchSignature,"
			+ "StayInTouchNote,Phone,Fax,MobilePhone,Alias,CommunityNickname,IsActive,TimeZoneSidKey,UserRoleId,LocaleSidKey,"
			+ "ReceivesInfoEmails,ReceivesAdminInfoEmails,EmailEncodingKey,ProfileId,UserType,LanguageLocaleKey,EmployeeNumber,"
			+ "DelegatedApproverId,ManagerId,LastLoginDate,LastPasswordChangeDate,CreatedDate,CreatedById,LastModifiedDate,"
			+ "LastModifiedById,SystemModstamp,OfflineTrialExpirationDate,OfflinePdaTrialExpirationDate,UserPermissionsMarketingUser,"
			+ "UserPermissionsOfflineUser,UserPermissionsCallCenterAutoLogin,UserPermissionsMobileUser,UserPermissionsSFContentUser,"
			+ "UserPermissionsKnowledgeUser,UserPermissionsInteractionUser,UserPermissionsSupportUser,UserPermissionsSiteforceContributorUser,"
			+ "UserPermissionsSiteforcePublisherUser,UserPermissionsChatterAnswersUser,UserPermissionsWorkDotComUserFeature,ForecastEnabled,"
			+ "UserPreferencesActivityRemindersPopup,UserPreferencesEventRemindersCheckboxDefault,UserPreferencesTaskRemindersCheckboxDefault,"
			+ "UserPreferencesReminderSoundOff,UserPreferencesDisableAllFeedsEmail,UserPreferencesDisableFollowersEmail,"
			+ "UserPreferencesDisableProfilePostEmail,UserPreferencesDisableChangeCommentEmail,UserPreferencesDisableLaterCommentEmail,"
			+ "UserPreferencesDisProfPostCommentEmail,UserPreferencesContentNoEmail,UserPreferencesContentEmailAsAndWhen,"
			+ "UserPreferencesApexPagesDeveloperMode,UserPreferencesHideCSNGetChatterMobileTask,UserPreferencesDisableMentionsPostEmail,"
			+ "UserPreferencesDisMentionsCommentEmail,UserPreferencesHideCSNDesktopTask,UserPreferencesHideChatterOnboardingSplash,"
			+ "UserPreferencesHideSecondChatterOnboardingSplash,UserPreferencesDisCommentAfterLikeEmail,UserPreferencesDisableLikeEmail,"
			+ "UserPreferencesDisableMessageEmail,UserPreferencesOptOutOfTouch,UserPreferencesDisableBookmarkEmail,"
			+ "UserPreferencesDisableSharePostEmail,UserPreferencesEnableAutoSubForFeeds,UserPreferencesDisableFileShareNotificationsForApi,"
			+ "UserPreferencesShowTitleToExternalUsers,UserPreferencesShowManagerToExternalUsers,UserPreferencesShowEmailToExternalUsers,"
			+ "UserPreferencesShowWorkPhoneToExternalUsers,UserPreferencesShowMobilePhoneToExternalUsers,UserPreferencesShowFaxToExternalUsers,"
			+ "UserPreferencesShowStreetAddressToExternalUsers,UserPreferencesShowCityToExternalUsers,UserPreferencesShowStateToExternalUsers,"
			+ "UserPreferencesShowPostalCodeToExternalUsers,UserPreferencesShowCountryToExternalUsers,UserPreferencesShowProfilePicToGuestUsers,"
			+ "UserPreferencesShowTitleToGuestUsers,UserPreferencesShowCityToGuestUsers,UserPreferencesShowStateToGuestUsers,"
			+ "UserPreferencesShowPostalCodeToGuestUsers,UserPreferencesShowCountryToGuestUsers,UserPreferencesDisableFeedbackEmail,"
			+ "UserPreferencesDisableCoachingEmail,UserPreferencesDisableGoalEmail,UserPreferencesDisableWorkEmail,UserPreferencesHideS1BrowserUI,"
			+ "ContactId,AccountId,CallCenterId,Extension,FederationIdentifier,AboutMe,FullPhotoUrl,SmallPhotoUrl,DigestFrequency,"
			+ "DefaultGroupNotificationFrequency,LastViewedDate,LastReferencedDate";

	private GroupService groupService;
	
	UserService(HttpRequestHandler httpRequestHandler,SalesforceConfiguration serviceConfiguration,GroupService groupService) {
		super(httpRequestHandler, serviceConfiguration);
		this.groupService = groupService;
	}

	/**
	 * This method retrieves an instance of User corresponding to provided guid.
	 * If user is not found in data store it throws PrincipalNotFoundException
	 * 
	 * @param guid
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public User getByGuid(String guid){
		HttpResponse response = httpRequestHandler.handleRequestGet(constructURI(String.format("User/%s", guid)), getHeaders().toArray(new HttpPair[0]));
		try {
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(guid + " not found.",null,PrincipalType.user);
			}
			
			User user =  JsonMapperService.getInstance().getObject(User.class, response.contentString());
			probeGroupMembers(user);
			
			return user;
		}
		finally {
			response.release();
		}
	}
	
	
	/**
	 * This method retrieves an instance of User corresponding to provided user name.
	 * If user is not found in data store it throws PrincipalNotFoundException
	 * <br />
	 * This method makes use of <b>Salesforce Object Query Language</b> for fetching group.
	 * 
	 * @param name
	 * @throws PrincipalNotFoundException
	 * @return
	 */
	public User getByName(String name){
		HttpResponse response = httpRequestHandler.handleRequestGet(
				constructSOQLURI(String.format(
						serviceConfiguration.getGetByNameUserQuery(),
						USER_ATTRIBUTES, name)), getHeaders().toArray(new HttpPair[0]));
		
		try {		
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(name + " not found.",null,PrincipalType.user);
			}
			
			@SuppressWarnings("unchecked")
			List<String> records = (List<String>) JsonMapperService.getInstance().getJsonProperty(response.contentString(), "records");
			
			if(records.isEmpty()){
				throw new PrincipalNotFoundException(name + " not found.",null,PrincipalType.user);
			}
			
			User user = JsonMapperService.getInstance().convert(records.get(0), User.class);
			
			probeGroupMembers(user);
			
			return user;
		}
		finally {
			response.release();
		}
	}
	
	
	/**
	 * This method retrieves all users present in the data store.
	 * <br />
	 * This method makes use of <b>Salesforce Object Query Language</b> for fetching group.
	 * 
	 * @return users list
	 */
	public Users all(){
		HttpResponse response = httpRequestHandler.handleRequestGet(
				constructSOQLURI(String.format(serviceConfiguration.getGetAllUsers(),
						USER_ATTRIBUTES)),getHeaders().toArray(new HttpPair[0]));
		try {
			return JsonMapperService.getInstance().getObject(Users.class, response.contentString());
		}
		finally {
			response.release();
		}
	}
	
	/**
	 * <p>
	 * Saves user into Salesforce datastore.
	 * </p>
	 * <p>
	 * While saving user following conditions should be met.
	 * <br />
	 * <ul>
	 * 	<li>Name property of User should not be set, it is computed by Salesforce</li>
	 * 	<li>Alias property length cannot be greater than 8</li>
	 * 	<li>Following fields/properties are mandatory.
	 * 		<ul>
	 * 			<li>Email</li>
	 * 			<li>Alias</li>
	 * 			<li>TimeZoneSidKey</li>
	 * 			<li>LocaleSidKey</li>
	 * 			<li>EmailEncodingKey</li>
	 * 			<li>ProfileId</li>
	 * 			<li>LanguageLocaleKey</li>
	 * 		</ul>
	 *  </li>
	 * </ul>
	 * </p>
	 * <p>
	 * While creating user, it is two step process.
	 * <ul>
	 * 	<li>First we create user without password as API for user creation does not takes password into consideration.</li>
	 * 	<li>After user creation we set password.</li>
	 * </ul>
	 * Two calls are made to create the user.
	 * </p>
	 * 
	 * @param user
	 * @throws PrincipalAlreadyExistsException if user by same principal name exists.
	 * @throws ConnectorException for possible json mapping exceptions
	 * @return
	 */
	public User save(User user){
		try{
			createUserValidation(user);
			
			handleUserCreation(user);
			
			handlePasswordSetting(user);
			
			handleGroupMembers(user);
			
			return user;
		} catch (IOException e) {
			throw new ConnectorException("Problem in saving user",e);
		}
	}

	/**
	 * Updates user properties sent for update.
	 * <br />
	 * For finding user to update it makes use of guid.
	 * <br />
	 * Update validation only checks Alias field if provided is less than 8 characters or not.
	 * 
	 * @param user
	 * @throws PrincipalNotFoundException if the user by object id not found in datastore.
	 * @throws ConnectorException for service related exception.
	 */
	public void update(User user){
		String id = null;
		String profileId = null;
		try{
			updateUserValidation(user);
			
			//we cannot send id as updatable property hence we cache it and clear from pojo
			id = user.getId();
			user.setId(null);
			//profile id is not updatable hence we remove it
			profileId = user.getProfileId();
			user.setProfileId(null);
			
			HttpResponse response = httpRequestHandler.handleRequestPatch(
					constructURI(String.format("User/%s", id)),
					JsonMapperService.getInstance().getJson(user),
					getHeaders().toArray(new HttpPair[0]));
			
			if(response.status().getCode() == 404){
				throw new PrincipalNotFoundException(user.getId() + " not found.",null,PrincipalType.user);
			}
			
			if(response.status().getCode() != 204){
				throw new ConnectorException("Problem in updating user as status code is not 204 is "
						+ response.status().getCode() + " : " + response.contentString());
			}
			
		} catch (IOException e) {
			throw new ConnectorException("Problem in updating user",e);
		}finally{
			//reset the cached values
			user.setId(id);
			user.setProfileId(profileId);
		}
	}
	
	/**
	 * This sets new password for the user.
	 * <br/>
	 * User is identified by guid.
	 * 
	 * @param user
	 */
	public void handlePasswordSetting(User user) {
		
		String passwordJson = String.format("{\"NewPassword\" : \"%s\"}",user.getPassword());
		HttpResponse response = httpRequestHandler.handleRequestPost(
				constructURI(String.format("User/%s/password", user.getId())),
				passwordJson, getHeaders().toArray(new HttpPair[0]));
		
		if(response.status().getCode() != 204){
			throw new ConnectorException("Problem in creating principal reason : " + response.contentString());
		}
	}
	
	/**
	 * Maps group members to user i.e. user is added to supplied groups.
	 * 
	 * @param user
	 */
	private void handleGroupMembers(User user) {
		GroupMembers groupMembers = user.getGroupMembers();
		if(groupMembers == null) return;
		List<GroupMember> members = groupMembers.getGroupMembers();
		
		Group group = null;
		for (GroupMember groupMember : members) {
			//group member addition does not give any hint of principal not found
			//hence we need to check for it explicitly
			group = groupService.getByName(groupMember.getGroup().getName());
			groupService.addUserToGroup(user.getId(), group.getId());
		}
	}
	
	/**
	 * Checks credentials of user.
	 * 
	 * @param identity
	 * @param password
	 * @return
	 */
	public boolean areCredentialsValid(String principalName, char[] password){
		Token token = null;
		try{
			token = SalesforceAuthorizationHelper.getInstance()
			.login(principalName,new String(password));
		}catch(Exception e){
			log.error("Login failed " + e.getMessage(), e);
			return false;
		}
		return token != null && token.getSessionId() != null
				&& principalName.equals(token.getUserName());
	}

	/**
	 * Helper utility method, makes a POST request while creating user.
	 * 
	 * @param user
	 * @throws IOException
	 */
	private void handleUserCreation(User user) throws IOException {
		HttpResponse response = httpRequestHandler
				.handleRequestPost(constructURI("User"), JsonMapperService
						.getInstance().getJson(user), getHeaders().toArray(new HttpPair[0]));

		probeUserCreationException(user, response);
		String id = JsonMapperService.getInstance().getJsonProperty(response.contentString(), "id").toString();
		user.setId(id);
	}

	/**
	 * Helper utility method checks response message for error messages for already existing user in 
	 * datastore.
	 * 
	 * @param user
	 * @param response
	 */
	private void probeUserCreationException(User user, HttpResponse response) {
		if(response.status().getCode() != 201){
			List<AppErrorMessage> appErrorMessages = JsonMapperService.getInstance().
					getObject(new TypeReference<List<AppErrorMessage>>() {}, response.contentString());
			for (AppErrorMessage appErrorMessage : appErrorMessages) {
				if("DUPLICATE_USERNAME".equals(appErrorMessage.errorCode)){
					throw new PrincipalAlreadyExistsException("Principal already exists by username " + user.getUsername());
				}
			}
			
			throw new ConnectorException("Problem in creating principal reason : " + response.contentString());
		}
	}
	
	/**
	 * Helper utility method performs validation checks on user creation.
	 * <br/>
	 * Following field validations are checked.
	 * <ul>
	 * 	<li>Name set or not</li>
	 * 	<li>Required fields present or not</li>
	 * 	<li>Alias length greater than 8</li>
	 * </ul>
	 * 
	 * @param user
	 */
	private void createUserValidation(User user) {
		nameFieldValidation(user);
		
		requiredFieldValidation(user);
		
		aliasFieldLengthValidation(user);
	}
	
	/**
	 * Helper utility method performs validation checks on user updation.
	 * <br/>
	 * Following field validations are checked.
	 * <ul>
	 * 	<li>Alias length greater than 8</li>
	 * </ul>
	 * 
	 * @param user
	 */
	private void updateUserValidation(User user) {
		if(!StringUtil.isNullOrEmpty(user.getAlias()))
			aliasFieldLengthValidation(user);
	}

	/**
	 * Helper utility method for checking alias field length is less than or equal to 8 or not.
	 * 
	 * @param user
	 * @throws ConnectorException if field length is greater than 8 
	 */
	private void aliasFieldLengthValidation(User user) {
		if(user.getAlias().length() > 8){
			throw new ConnectorException("Alias cannot be greater than 8 characters");
		}
	}

	/**
	 * Helper utility method for checking all mandatory fields are present or not.
	 * 
	 * @param user
	 * @throws ConnectorException if any of the required filed is missing
	 */
	private void requiredFieldValidation(User user) {
		if(StringUtil.isNullOrEmpty(user.getEmail()) || StringUtil.isNullOrEmpty(user.getAlias())
				|| StringUtil.isNullOrEmpty(user.getTimeZoneSidKey()) || StringUtil.isNullOrEmpty(user.getLocaleSidKey())
				|| StringUtil.isNullOrEmpty(user.getEmailEncodingKey()) || StringUtil.isNullOrEmpty(user.getProfileId())
				|| StringUtil.isNullOrEmpty(user.getLanguageLocaleKey())){
			throw new ConnectorException("One of the required fields or all missing "
					+ "[Email, Alias, TimeZoneSidKey, LocaleSidKey, EmailEncodingKey, ProfileId, LanguageLocaleKey]");
		}
	}

	/**
	 * Helper utility method for checking name field is set or not.
	 * 
	 * @param user
	 * @throws ConnectorException if name field is set.
	 */
	private void nameFieldValidation(User user) {
		if(!StringUtil.isNullOrEmpty(user.getName())){
			throw new ConnectorException("Name cannot be set it is handled by salesforce.");
		}
	}
	
	/**
	 * Fetches GroupMembers for a user.
	 * 
	 * @param user
	 */
	private void probeGroupMembers(User user){
		GroupMembers groupMembers = groupService.getGroupMembersForUser(user.getId());
		user.setGroupMembers(groupMembers);
	}
}
