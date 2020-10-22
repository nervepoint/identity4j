/* HEADER */
package com.identity4j.connector.as400;

/*
 * #%L
 * Identity4J AS400
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.User;
import com.ibm.as400.access.UserGroup;
import com.ibm.as400.access.UserList;
import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.as400.callback.As400Callback;
import com.identity4j.connector.as400.callback.As400CallbackWithoutResult;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.StringUtil;
import com.identity4j.util.passwords.DefaultPasswordCharacteristics;
import com.identity4j.util.passwords.PasswordCharacteristics;

public class As400Connector extends AbstractConnector<As400Configuration> {
	private static final String CREATE_PROFILE_SUCCESS = "CPI2221";
	private static final String DELETE_PROFILE_SUCCESS = "CPC2191";
	private static final String CHANGE_PROFILE_SUCCESS = "CPC2205";

	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(
			Arrays.asList(new ConnectorCapability[] { ConnectorCapability.accountDisable,
					ConnectorCapability.passwordChange, ConnectorCapability.passwordSet, ConnectorCapability.createUser,
					ConnectorCapability.deleteUser, ConnectorCapability.updateUser, ConnectorCapability.hasFullName,
					ConnectorCapability.roles, ConnectorCapability.authentication, ConnectorCapability.requireGUID,
					ConnectorCapability.createIdentityGUID, ConnectorCapability.identities,
					ConnectorCapability.forcePasswordChange, ConnectorCapability.hasPasswordPolicy,
					ConnectorCapability.createRole, ConnectorCapability.updateRole, ConnectorCapability.deleteRole,
					ConnectorCapability.childRoles, ConnectorCapability.roleAttributes,
					ConnectorCapability.identityAttributes, 
					ConnectorCapability.caseInsensitivePrincipalNames }));

	private AS400 as400;
	private PasswordCharacteristics policy;

	@Override
	public boolean isOpen() {
		getIdentityByName(getConfiguration().getServiceAccountUsername());
		return as400 != null && as400.isConnected();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void disableIdentity(final Identity identity) {
		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {
				User user = new User(as400, identity.getPrincipalName());
				user.setStatus("*DISABLED");
			}
		}.execute();
	}

	@Override
	public void enableIdentity(final Identity identity) {
		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {
				User user = new User(as400, identity.getPrincipalName());
				user.setStatus("*ENABLED");
			}
		}.execute();
	}

	@Override
	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		try {
			return as400.validateSignon(identity.getPrincipalName(), String.valueOf(password));
		} catch (AS400SecurityException ase) {
			try {
				User usr = new User(as400, identity.getPrincipalName());
				if (usr.isPasswordSetExpire())
					throw new PasswordChangeRequiredException();
			} catch (PasswordChangeRequiredException pcre) {
				throw pcre;
			} catch (Exception e) {
				//
			}
			return false;
		} catch (IOException ioe) {
			return false;
		}
	}

	@Override
	protected void changePassword(final Identity identity, final char[] oldPassword, final char[] password) {

		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {
				// create new as400 connection with the given identity
				AS400 as400 = getConfiguration().buildConnection(identity.getPrincipalName(), String.valueOf(password));
				// change password and disconnect connection
				as400.changePassword(String.valueOf(oldPassword), String.valueOf(password));
				as400.disconnectAllServices();
			}
		}.execute();
	}

	@Override
	protected void setPassword(final Identity identity, final char[] password, final boolean forcePasswordChangeAtLogon,
			PasswordResetType type) throws ConnectorException {
		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {

				User user = new User(as400, identity.getPrincipalName());
				boolean wasEnabled = "*ENABLED".equals(user.getStatus());

				// run the update profile command
				List<AS400Message> msgs = runCommandCall("CHGUSRPRF USRPRF(" + identity.getPrincipalName()
						+ ") PASSWORD(" + String.valueOf(password) + ")");
				// if failure of command exception is thrown from method
				validateSuccessStatus(msgs, CHANGE_PROFILE_SUCCESS);

				user = new User(as400, identity.getPrincipalName());

				if (forcePasswordChangeAtLogon && !user.isPasswordSetExpire()) {
					user.setPasswordSetExpire(true);
					if (wasEnabled)
						user.setStatus("*ENABLED");
				} else if (!forcePasswordChangeAtLogon && user.isPasswordSetExpire()) {
					user.setPasswordSetExpire(false);
				}
			}
		}.execute();
	}

	/**
	 * Run command on the remote system
	 * 
	 * @param command
	 * @return system messages
	 */
	private List<AS400Message> runCommandCall(final String command) {
		return new As400Callback<List<AS400Message>>() {
			@Override
			protected List<AS400Message> executeInCallback() throws Exception {
				try {
					// run command on remote system
					CommandCall cmd = new CommandCall(as400);
					cmd.run(command);
					return Arrays.asList(cmd.getMessageList());
				} finally {
					// clean up by disconnecting
					as400.disconnectService(AS400.COMMAND);
				}
			}
		}.execute();
	}

	/**
	 * Convenience method to concatenate any error messages returned as a result
	 * of a command call
	 * 
	 * @param msgs
	 * @return error string
	 */
	private String buildCmdErrorMsg(List<AS400Message> msgs) {
		StringBuilder builder = new StringBuilder("Error:");
		for (AS400Message msg : msgs) {
			builder.append(msg.getText());
		}
		return builder.toString();
	}

	/**
	 * Get identity from remote system
	 * 
	 * @param principal
	 *            name
	 * @return identity
	 */
	@Override
	public Identity getIdentityByName(final String principalName)
			throws PrincipalNotFoundException, ConnectorException {
		if (principalName.length() > 10) {
			throw new PrincipalNotFoundException(
					"Identity cannot exist, as it's name is greater than 10 characters in length.");
		}
		return new As400Callback<Identity>() {
			@Override
			protected Identity executeInCallback() throws Exception {
				User user = new User(as400, principalName);
				if (!user.exists()) {
					throw new PrincipalNotFoundException("Identity not found = '" + principalName + "'");
				}
				return mapUserToIdentity(user);
			}
		}.execute();
	}

	/**
	 * Get all identities from system
	 * 
	 * @return list of identities
	 */
	@Override
	public Iterator<Identity> allIdentities() throws ConnectorException {
		final Enumeration<User> users = getUsers(UserList.USER);
		return new Iterator<Identity>() {
			@Override
			public boolean hasNext() {
				return users.hasMoreElements();
			}

			@Override
			public Identity next() {
				User nextElement = users.nextElement();
				return mapUserToIdentity(nextElement);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove is not supported");
			}
		};
	}

	/**
	 * Translate between system user to internal identity
	 * 
	 * @param user
	 * @return identity
	 */
	private Identity mapUserToIdentity(final User user) {
		return new As400Callback<Identity>() {
			@Override
			protected Identity executeInCallback() throws Exception {
				// https://app.assembla.com/spaces/hypersocket/tickets/2240-nam--as400-error-when-service-account-profile-has-a-group-flag/details
				//	if (user.getGroupID() != 0)
				//		throw new IllegalArgumentException("This user is a  group.");

				String guid = String.valueOf(user.getUserID());
				As400Identity identity = new As400Identity(user, guid, user.getName());

				PasswordStatus passwordStatus = identity.getPasswordStatus();
				passwordStatus.setLastChange(user.getPasswordLastChangedDate());
				String warndays = policy.getAttributes().get(AS400PasswordRules.QPWDEXPWRN);
				if (!StringUtil.isNullOrEmpty(warndays)) {
					Calendar warn = Calendar.getInstance();
					warn.setTime(user.getPasswordExpireDate());
					warn.add(Calendar.DATE, Integer.parseInt(warndays));
					passwordStatus.setWarn(warn.getTime());
				}
				if (user.isPasswordSetExpire())
					passwordStatus.setNeedChange(true);
				else
					passwordStatus.setExpire(user.getPasswordExpireDate());
				passwordStatus.calculateType();

				identity.setLastSignOnDate(user.getPreviousSignedOnDate());

				// compile a list of attributes
				identity.setFullName(user.getDescription());
				mapProfile(user, identity);

				// get propertied for user
				AccountStatus accountStatus = identity.getAccountStatus();
				accountStatus.setExpire(user.getUserExpirationDate());
				accountStatus.setDisabled("*DISABLED".equals(user.getStatus()));
				accountStatus.calculateType();

				// group is represented as a profile just like user. To retrieve
				// guid for group you need to interrogate it
				String groupName = user.getGroupProfileName();

				// as long as user has a group then retrieve its guid
				if (!StringUtil.isNullOrEmpty(groupName) && !groupName.equals(User.NONE)) {
					UserGroup nativeGroup = new UserGroup(as400, groupName);
					String groupId = String.valueOf(nativeGroup.getUserID());
					identity.addRole(new As400Role(nativeGroup, groupId, groupName));
				}

				// other groups
				for (String g : user.getSupplementalGroups()) {
					UserGroup nativeGroup = new UserGroup(as400, g);
					String groupId = String.valueOf(nativeGroup.getUserID());
					identity.addRole(new As400Role(nativeGroup, groupId, groupName));
				}

				// add group to memberOf attribute
				return identity;
			}
		}.execute();
	}

	/**
	 * Get role
	 * 
	 * @param rolename
	 * @return role
	 */
	@Override
	public Role getRoleByName(final String rolename) throws PrincipalNotFoundException, ConnectorException {
		if (rolename.length() > 10) {
			throw new PrincipalNotFoundException(
					"Role cannot exist, as it's name is greater than 10 characters in length.");
		}

		return new As400Callback<Role>() {
			@Override
			protected Role executeInCallback() throws Exception {
				UserGroup user = new UserGroup(as400, rolename);
				if (!user.exists()) {
					throw new PrincipalNotFoundException("User Group not found = '" + rolename + "'");
				}
				return mapRoleToIdentity(user);
			}
		}.execute();
	}

	/**
	 * Get all roles
	 * 
	 * @return all roles
	 */
	@Override
	public Iterator<Role> allRoles() throws ConnectorException {
		final Enumeration<User> users = getUsers(UserList.GROUP);
		return new Iterator<Role>() {
			@Override
			public boolean hasNext() {
				return users.hasMoreElements();
			}

			@Override
			public Role next() {
				User nextElement = users.nextElement();
				return mapRoleToIdentity((UserGroup) nextElement);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove is not supported");
			}
		};
	}

	private Role mapRoleToIdentity(UserGroup user) {
		String guid = String.valueOf(user.getUserID());
		As400Role role = new As400Role(user, guid, user.getName());
		role.setAttribute("description", user.getDescription());
		mapProfile(user, role);
		return role;
	}

	private Enumeration<User> getUsers(final String userInfo) {
		return new As400Callback<Enumeration<User>>() {
			@SuppressWarnings("unchecked")
			@Override
			protected Enumeration<User> executeInCallback() throws Exception {
				UserList userList = new UserList(as400, userInfo, UserList.NONE);
				return userList.getUsers();
			}
		}.execute();
	}

	/**
	 * Create new user within the system
	 * 
	 * @param principalName
	 * @param password
	 * @return identity
	 */
	@Override
	public Identity createIdentity(final Identity id, final char[] password) {
		return new As400Callback<Identity>() {
			@Override
			protected Identity executeInCallback() throws Exception {
				// create user
				// CRTUSRPRF USRPRF(JJADAMS) PASSWORD(S1CR2T)
				String command = "CRTUSRPRF USRPRF(" + id.getPrincipalName() + ") PASSWORD(" + String.valueOf(password)
						+ ") PWDEXP(*NO) STATUS(*ENABLED) USRCLS(*USER)";
				List<AS400Message> msgs = runCommandCall(command);
				// if failure of command exception is thrown from method
				validateSuccessStatus(msgs, CREATE_PROFILE_SUCCESS);

				As400Identity identity = (As400Identity) getIdentityByName(id.getPrincipalName());

				// Details
				identity.getNativeUser().setDescription(id.getFullName() == null ? "" : id.getFullName());
				updateUserProfile(identity, id.getAttributes());

				// set password
				setPassword(identity, password, false, PasswordResetType.USER);

				setupGroups(id, identity);

				return identity;
			}

		}.execute();
	}

	/**
	 * Iterate through messages and identify whether success or failure message
	 * 
	 * @param list
	 *            of messages
	 * @param expected
	 *            success code
	 * @throws ConnectorException
	 */
	private void validateSuccessStatus(List<AS400Message> msgs, String successCode) throws ConnectorException {
		boolean error = false;
		// locate success code in messages
		for (AS400Message msg : msgs) {
			error = msg.getText().contains(successCode);
		}
		// success code not found throw exception
		if (error) {
			throw new ConnectorException(buildCmdErrorMsg(msgs));
		}
	}

	/**
	 * Basic delete user
	 * 
	 * @param principalName
	 */
	@Override
	public void deleteIdentity(final String principalName) throws ConnectorException {
		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {
				/* Make sure this actually IS a role */
				if (getIdentityByName(principalName) == null)
					throw new Exception("No such identity.");

				// delete user
				String command = "DLTUSRPRF USRPRF" + "(" + principalName + ")";
				List<AS400Message> msgs = runCommandCall(command);
				validateSuccessStatus(msgs, DELETE_PROFILE_SUCCESS);
			}
		}.execute();
	}

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {

		return new As400Callback<PasswordCharacteristics>() {
			@Override
			protected PasswordCharacteristics executeInCallback() throws Exception {
				DefaultPasswordCharacteristics def = new DefaultPasswordCharacteristics();
				def.setSymbols(new char[0]);
				def.setMinimumSymbols(0);
				for (AS400PasswordRules key : AS400PasswordRules.values()) {
					Object obj = new SystemValue(as400, key.toString()).getValue();
					switch (key) {
					case QPWDMINLEN:
						def.setMinimumSize(Integer.valueOf(obj.toString()));
						break;
					case QPWDMAXLEN:
						def.setMaximumSize(Integer.valueOf(obj.toString()));
						break;
					case QPWDRQDDGT:
						def.setMinimumDigits(Integer.valueOf(obj.toString()));
						break;
					case QPWDRQDDIF:
						if (obj.toString().equals("0"))
							def.setHistorySize(0);
						else if (obj.toString().equals("1"))
							def.setHistorySize(32);
						else if (obj.toString().equals("2"))
							def.setHistorySize(24);
						else if (obj.toString().equals("3"))
							def.setHistorySize(18);
						else if (obj.toString().equals("4"))
							def.setHistorySize(12);
						else if (obj.toString().equals("5"))
							def.setHistorySize(10);
						else if (obj.toString().equals("6"))
							def.setHistorySize(8);
						else if (obj.toString().equals("7"))
							def.setHistorySize(6);
						else if (obj.toString().equals("8"))
							def.setHistorySize(4);
						break;
					default:
						def.getAttributes().put(key.getMeaning(), obj.toString());
						break;
					}
				}
				return def;
			}
		}.execute();
	}

	@Override
	protected void onOpen(As400Configuration parameters) throws ConnectorException {
		try {
			this.as400 = getConfiguration().buildConnection();
			this.policy = getPasswordCharacteristics();
			if (!as400.authenticate(getConfiguration().getServiceAccountUsername(),
					getConfiguration().getServiceAccountPassword())) {
				throw new IOException("Invalid credentials");
			}
		} catch (IOException e) {
			throw new ConnectorException("Failed to connect.", e);
		} catch (AS400SecurityException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	public Role createRole(final Role role) throws ConnectorException {
		return new As400Callback<Role>() {
			@Override
			protected Role executeInCallback() throws Exception {
				// create user
				// CRTUSRPRF USRPRF(JJADAMS) PASSWORD(S1CR2T)
				String command = "CRTUSRPRF USRPRF(" + role.getPrincipalName() + ") "
						+ "GID(*GEN) STATUS(*ENABLED) USRCLS(*USER)";
				List<AS400Message> msgs = runCommandCall(command);
				// if failure of command exception is thrown from method
				validateSuccessStatus(msgs, CREATE_PROFILE_SUCCESS);

				As400Role nativeRole = (As400Role) getRoleByName(role.getPrincipalName());

				// Details
				nativeRole.getNativeUser().setDescription(role.getAttributeOrDefault("description", ""));
				updateUserProfile(nativeRole, role.getAttributes());

				return nativeRole;
			}

		}.execute();
	}

	@Override
	public void deleteRole(final String principalName) throws ConnectorException {

		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {
				/* Make sure this actually IS a role */
				if (getRoleByName(principalName) == null)
					throw new Exception("No such role.");

				// delete user
				String command = "DLTUSRPRF USRPRF" + "(" + principalName + ")";
				List<AS400Message> msgs = runCommandCall(command);
				validateSuccessStatus(msgs, DELETE_PROFILE_SUCCESS);
			}
		}.execute();
	}

	@Override
	public void updateRole(final Role role) throws ConnectorException {
		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {
				As400Role old = (As400Role) getRoleByName(role.getPrincipalName());
				old.getNativeUser().setDescription(role.getAttributeOrDefault("description", ""));
				updateUserProfile(old, role.getAttributes());
			}
		}.execute();
	}

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}

	@Override
	public void updateIdentity(final Identity identity) throws ConnectorException {
		new As400CallbackWithoutResult() {
			@Override
			protected void executeInCallbackWithoutResult() throws Exception {
				As400Identity old = (As400Identity) getIdentityByName(identity.getPrincipalName());
				if (!Objects.equals(identity.getFullName(), old.getFullName())) {
					old.getNativeUser().setDescription(identity.getFullName());
				}

				setupGroups(identity, old);

				updateUserProfile(old, identity.getAttributes());
			}
		}.execute();
	}

	protected void setupGroups(final Identity identity, As400Identity nativeUser)
			throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException {
		String wantPrimaryGroup = User.NONE;
		String[] wantSupGroups = new String[] { User.NONE };

		Role[] idRoles = identity.getRoles();
		if (idRoles.length != 0) {
			wantPrimaryGroup = idRoles[0].getPrincipalName();
			if (idRoles.length > 1) {
				wantSupGroups = new String[idRoles.length - 1];
				for (int i = 1; i < idRoles.length; i++) {
					wantSupGroups[i - 1] = idRoles[i].getPrincipalName();
				}
			}
		}

		if (!nativeUser.getNativeUser().getGroupProfileName().equals(wantPrimaryGroup)) {
			nativeUser.getNativeUser().setGroupProfileName(wantPrimaryGroup);
		}
		if (!Arrays.equals(nativeUser.getNativeUser().getSupplementalGroups(), wantSupGroups)) {
			nativeUser.getNativeUser().setSupplementalGroups(wantSupGroups);
		}
	}

	protected void updateUserProfile(As400Principal identity, Map<String, String[]> map)
			throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException {

		String[] attr = map.get("accountingCode");
		if (!Objects.equals(identity.getAttribute("accountingCode"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setAccountingCode(attr[0]);
		}
		attr = map.get("assistanceLevel");
		if (!Objects.equals(identity.getAttribute("assistanceLevel"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setAssistanceLevel(attr[0]);
		}
		attr = map.get("attentionKeyProgram");
		if (!Objects.equals(identity.getAttribute("attentionKeyProgram"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setAttentionKeyHandlingProgram(attr[0]);
		}
		attr = map.get("ccsid");
		if (!Objects.equals(identity.getAttribute("ccsid"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setCCSID(Integer.parseInt(attr[0]));
		}
		attr = map.get("chridControl");
		if (!Objects.equals(identity.getAttribute("chridControl"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setCHRIDControl(attr[0]);
		}
		attr = map.get("countryID");
		if (!Objects.equals(identity.getAttribute("countryID"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setCountryID(attr[0]);
		}
		attr = map.get("currentLibraryName");
		if (!Objects.equals(identity.getAttribute("currentLibraryName"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setCurrentLibraryName(attr[0].equals("*CRTDFT") ? "" : attr[0]);
		}
		attr = map.get("displaySignOnInformation");
		if (!Objects.equals(identity.getAttribute("displaySignOnInformation"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setDisplaySignOnInformation(attr[0]);
		}
		attr = map.get("groupAuthority");
		if (!Objects.equals(identity.getAttribute("groupAuthority"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setGroupAuthority(attr[0]);
		}
		attr = map.get("groupAuthorityType");
		if (!Objects.equals(identity.getAttribute("groupAuthorityType"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setGroupAuthorityType(attr[0]);
		}
		attr = map.get("groupProfileName");
		if (!Objects.equals(identity.getAttribute("groupProfileName"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setGroupProfileName(attr[0]);
		}
		attr = map.get("highestSchedulingPriority");
		if (!Objects.equals(identity.getAttribute("highestSchedulingPriority"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0 && attr != null && attr.length > 0) {
			identity.getNativeUser().setHighestSchedulingPriority(Integer.parseInt(attr[0]));
		}
		attr = map.get("homeDir");
		if (!Objects.equals(identity.getAttribute("homeDir"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setHomeDirectory(attr[0]);
		}
		attr = map.get("initialProgram");
		if (!Objects.equals(identity.getAttribute("initialProgram"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setInitialProgram(attr[0]);
		}
		attr = map.get("initialMenu");
		if (!Objects.equals(identity.getAttribute("initialMenu"), StringUtil.toDefaultString(attr))) {
			identity.getNativeUser().setInitialMenu(attr == null || attr.length < 1 ? "" : attr[0].trim());
		}
		attr = map.get("keyboardBuffering");
		if (!Objects.equals(identity.getAttribute("keyboardBuffering"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setKeyboardBuffering(attr[0]);
		}
		attr = map.get("languageID");
		if (!Objects.equals(identity.getAttribute("languageID"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setLanguageID(attr[0]);
		}
		attr = map.get("limitCapabilites");
		if (!Objects.equals(identity.getAttribute("limitCapabilites"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setLimitCapabilities(attr[0]);
		}
		attr = map.get("limitDeviceSessions");
		if (!Objects.equals(identity.getAttribute("limitDeviceSessions"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setLimitDeviceSessions(attr[0]);
		}
		attr = map.get("localeJobAttributes");
		if (!Objects.equals(identity.getAttribute("localeJobAttributes"), StringUtil.toDefaultString(attr))
				&& attr != null) {
			identity.getNativeUser().setLocaleJobAttributes(attr);
		}
		attr = map.get("localePathName");
		if (!Objects.equals(identity.getAttribute("localePathName"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setLocalePathName(attr[0]);
		}
		attr = map.get("maximumStorageAllowed");
		if (!Objects.equals(identity.getAttribute("maximumStorageAllowed"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setMaximumStorageAllowed(attr[0]);
		}
		attr = map.get("messageQueue");
		if (!Objects.equals(identity.getAttribute("messageQueue"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setMessageQueue(attr[0]);
		}
		attr = map.get("messageQueueDeliveryMethod");
		if (!Objects.equals(identity.getAttribute("messageQueueDeliveryMethod"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setMessageQueueDeliveryMethod(attr[0]);
		}
		attr = map.get("messageQueueSeverity");
		if (!Objects.equals(identity.getAttribute("messageQueueSeverity"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setMessageQueueSeverity(Integer.parseInt(attr[0]));
		}
		attr = map.get("objectAuditingValue");
		if (!Objects.equals(identity.getAttribute("objectAuditingValue"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setObjectAuditingValue(attr[0]);
		}
		attr = map.get("outputQueue");
		if (!Objects.equals(identity.getAttribute("outputQueue"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setOutputQueue(attr[0]);
		}
		attr = map.get("owner");
		if (!Objects.equals(identity.getAttribute("owner"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setOwner(attr[0]);
		}
		attr = map.get("printDevice");
		if (!Objects.equals(identity.getAttribute("printDevice"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setPrintDevice(attr[0]);
		}
		attr = map.get("sortSequenceTable");
		if (!Objects.equals(identity.getAttribute("sortSequenceTable"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setSortSequenceTable(attr[0]);
		}
		attr = map.get("specialAuthority");
		if (!Objects.equals(identity.getAttribute("specialAuthority"), StringUtil.toDefaultString(attr))
				&& attr != null) {
			identity.getNativeUser().setSpecialAuthority(attr);
		}
		attr = map.get("specialEnvironment");
		if (!Objects.equals(identity.getAttribute("specialEnvironment"), StringUtil.toDefaultString(attr))
				&& attr != null && attr.length > 0) {
			identity.getNativeUser().setSpecialEnvironment(attr[0]);
		}
		attr = map.get("userClassName");
		if (!Objects.equals(identity.getAttribute("userClassName"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setUserClassName(attr[0]);
		}
		attr = map.get("userOptions");
		if (!Objects.equals(identity.getAttribute("userOptions"), StringUtil.toDefaultString(attr)) && attr != null
				&& attr.length > 0) {
			identity.getNativeUser().setUserOptions(attr);
		}
		attr = map.get("userActionAuditLevel");
		if (!Objects.equals(identity.getAttribute("userActionAuditLevel"), StringUtil.toDefaultString(attr))
				&& attr != null) {
			identity.getNativeUser().setUserActionAuditLevel(attr);
		}
	}

	protected void mapProfile(final User user, As400Principal identity) {
		identity.setAttribute("accountingCode", user.getAccountingCode());
		identity.setAttribute("assistanceLevel", user.getAssistanceLevel());
		identity.setAttribute("attentionKeyProgram", user.getAttentionKeyHandlingProgram());
		identity.setAttribute("ccsid", String.valueOf(user.getCCSID()));
		identity.setAttribute("chridControl", user.getCHRIDControl());
		identity.setAttribute("countryID", user.getCountryID());
		identity.setAttribute("currentLibraryName",
				user.getCurrentLibraryName().equals("") ? "*CRTDFT" : user.getCurrentLibraryName());
		identity.setAttribute("displaySignOnInformation", user.getDisplaySignOnInformation());
		identity.setAttribute("groupAuthority", user.getGroupAuthority());
		identity.setAttribute("groupAuthorityType", user.getGroupAuthorityType());
		identity.setAttribute("groupProfileName", user.getGroupProfileName());
		identity.setAttribute("highestSchedulingPriority", String.valueOf(user.getHighestSchedulingPriority()));
		identity.setAttribute("homeDir", user.getHomeDirectory());
		identity.setAttribute("initialProgram", user.getInitialProgram());
		identity.setAttribute("initialMenu", user.getInitialMenu());
		identity.setAttribute("keyboardBuffering", user.getKeyboardBuffering());
		identity.setAttribute("languageID", user.getLanguageID());
		identity.setAttribute("limitCapabilites", user.getLimitCapabilities());
		identity.setAttribute("limitDeviceSessions", user.getLimitDeviceSessions());
		identity.setAttribute("localeJobAttributes", user.getLocaleJobAttributes());
		identity.setAttribute("localePathName", user.getLocalePathName());
		identity.setAttribute("maximumStorageAllowed", String.valueOf(user.getMaximumStorageAllowed()));
		identity.setAttribute("messageQueue", user.getMessageQueue());
		identity.setAttribute("messageQueueDeliveryMethod", user.getMessageQueueDeliveryMethod());
		identity.setAttribute("messageQueueSeverity", String.valueOf(user.getMessageQueueSeverity()));
		identity.setAttribute("objectAuditingValue", user.getObjectAuditingValue());
		identity.setAttribute("outputQueue", user.getOutputQueue());
		identity.setAttribute("owner", user.getOwner());
		identity.setAttribute("printDevice", user.getPrintDevice());
		identity.setAttribute("sortSequenceTable", user.getSortSequenceTable());
		identity.setAttribute("specialAuthority", user.getSpecialAuthority());
		identity.setAttribute("specialEnvironment", user.getSpecialEnvironment());
		identity.setAttribute("storageUsed", String.valueOf(user.getStorageUsedInLong()));
		identity.setAttribute("userActionAuditLevel", user.getUserActionAuditLevel());
		identity.setAttribute("userClassName", user.getUserClassName());
		identity.setAttribute("userExpirationAction", user.getUserExpirationAction());
		identity.setAttribute("userOptions", user.getUserOptions());
	}
}