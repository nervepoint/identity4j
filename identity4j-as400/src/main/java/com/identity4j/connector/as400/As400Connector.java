/* HEADER */
package com.identity4j.connector.as400;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.SystemValue;
import com.ibm.as400.access.User;
import com.ibm.as400.access.UserGroup;
import com.ibm.as400.access.UserList;
import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.as400.callback.As400Callback;
import com.identity4j.connector.as400.callback.As400CallbackWithoutResult;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Role;
import com.identity4j.connector.principal.RoleImpl;
import com.identity4j.util.StringUtil;
import com.identity4j.util.passwords.DefaultPasswordCharacteristics;
import com.identity4j.util.passwords.PasswordCharacteristics;

public class As400Connector extends AbstractConnector {
	private static final String CREATE_PROFILE_SUCCESS = "CPI2221";
	private static final String DELETE_PROFILE_SUCCESS = "CPC2191";
	private static final String CHANGE_PROFILE_SUCCESS = "CPC2205";
	
	static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(
			Arrays.asList(new ConnectorCapability[] { ConnectorCapability.passwordChange,
					ConnectorCapability.passwordSet, ConnectorCapability.createUser, ConnectorCapability.deleteUser,
					ConnectorCapability.updateUser, ConnectorCapability.hasFullName, ConnectorCapability.roles,
					ConnectorCapability.authentication, ConnectorCapability.requireGUID,
					ConnectorCapability.createIdentityGUID, ConnectorCapability.identities }));

	private As400Configuration as400Configuration;
	private AS400 as400;

	@Override
	public boolean isOpen() {
		getIdentityByName(as400Configuration.getServiceAccountUsername());
		return as400 != null && as400.isConnected();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		try {
			return as400.validateSignon(identity.getPrincipalName(), String.valueOf(password));
		} catch (AS400SecurityException ase) {
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
				AS400 as400 = as400Configuration.buildConnection(identity.getPrincipalName(), String.valueOf(password));
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
				if (forcePasswordChangeAtLogon) {
					User user = new User(as400, identity.getPrincipalName());
					user.setPasswordSetExpire(true);
				} else {
					// run the update profile command
					List<AS400Message> msgs = runCommandCall("CHGUSRPRF USRPRF(" + identity.getPrincipalName()
							+ ") PASSWORD(" + String.valueOf(password) + ")");
					// if failure of command exception is thrown from method
					validateSuccessStatus(msgs, CHANGE_PROFILE_SUCCESS);
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
				String guid = String.valueOf(user.getUserID());
				Identity identity = new IdentityImpl(guid, user.getName());

				// get propertied for user
				PasswordStatus passwordStatus = identity.getPasswordStatus();
				passwordStatus.setLastChange(user.getPasswordLastChangedDate());
				passwordStatus.setExpire(user.getPasswordExpireDate());
				passwordStatus.calculateType();

				// TODO check if this is needed
				if (!passwordStatus.getType().equals(PasswordStatusType.expired) && user.isPasswordSetExpire()) {
					passwordStatus.setType(PasswordStatusType.expired);
				}

				identity.setLastSignOnDate(user.getPreviousSignedOnDate());

				// compile a list of attributes
				identity.setAttribute("description", user.getDescription());
				identity.setAttribute("homeDir", user.getHomeDirectory());

				// group is represented as a profile just like user. To retrieve
				// guid for group you need to interrogate it
				String groupName = user.getGroupProfileName();
				String groupId = new String();

				// as long as user has a group then retrieve its guid
				if (!StringUtil.isNullOrEmpty(groupName) && !groupName.equals(User.NONE)) {
					groupId = String.valueOf(new UserGroup(as400, groupName).getUserID());
				}

				// add group to memberOf attribute
				identity.memberOf(new RoleImpl(groupId, groupName));
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
		return new As400Callback<Role>() {
			@Override
			protected Role executeInCallback() throws Exception {
				User user = new UserGroup(as400, rolename);
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
				return mapRoleToIdentity(nextElement);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove is not supported");
			}
		};
	}

	private Role mapRoleToIdentity(User user) {
		// TODO not sure what information to retain
		String guid = String.valueOf(user.getUserID());
		Role role = new RoleImpl(user.getName(), guid);
		role.setAttribute("description", user.getDescription());
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

				Identity identity = getIdentityByName(id.getPrincipalName());
				// set password
				setPassword(identity, password, false, PasswordResetType.USER);
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
				for (AS400PasswordRules key : AS400PasswordRules.values()) {
					Object obj = new SystemValue(as400, key.toString()).getValue();
					switch (key) {
					case QPWDMINLEN:
						def.setMinimumSize(Integer.valueOf(obj.toString()));
						break;
					case QPWDMAXLEN:
						def.setMaximumSize(Integer.valueOf(obj.toString()));
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
	protected void onOpen(ConnectorConfigurationParameters parameters) throws ConnectorException {
		as400Configuration = (As400Configuration) parameters;
		try {
				this.as400 = as400Configuration.buildConnection();
				if(!as400.authenticate(as400Configuration.getServiceAccountUsername(), 
						as400Configuration.getServiceAccountPassword())) {
					throw new IOException("Invalid credentials");
				}
		} catch (IOException e) {
			throw new ConnectorException("Failed to connect.", e);
		} catch (AS400SecurityException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	public Role createRole(Role role) throws ConnectorException {
		throw new UnsupportedOperationException("Role maintenance is not yet supported");
	}

	@Override
	public void deleteRole(String principleName) throws ConnectorException {
		throw new UnsupportedOperationException("Role maintenance is not yet supported");
	}

	@Override
	public void updateRole(Role role) throws ConnectorException {
		throw new UnsupportedOperationException("Role maintenance is not yet supported");
	}

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}
}