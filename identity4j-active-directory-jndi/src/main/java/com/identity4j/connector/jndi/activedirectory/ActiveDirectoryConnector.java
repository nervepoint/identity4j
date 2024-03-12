/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

/*
 * #%L
 * Identity4J Active Directory JNDI
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.Count;
import com.identity4j.connector.Media;
import com.identity4j.connector.OperationContext;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PasswordPolicyViolationException;
import com.identity4j.connector.jndi.directory.AbstractDirectoryConfiguration.RoleMode;
import com.identity4j.connector.jndi.directory.AbstractDirectoryConnector;
import com.identity4j.connector.jndi.directory.DirectoryExceptionParser;
import com.identity4j.connector.jndi.directory.DirectoryIdentity;
import com.identity4j.connector.jndi.directory.DirectoryOU;
import com.identity4j.connector.jndi.directory.LdapService.ResultMapper;
import com.identity4j.connector.jndi.directory.filter.And;
import com.identity4j.connector.jndi.directory.filter.Eq;
import com.identity4j.connector.jndi.directory.filter.Filter;
import com.identity4j.connector.jndi.directory.filter.Ge;
import com.identity4j.connector.jndi.directory.filter.Not;
import com.identity4j.connector.jndi.directory.filter.Or;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.AccountStatusType;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Principal;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.CollectionUtil;
import com.identity4j.util.StringUtil;
import com.identity4j.util.Util;
import com.identity4j.util.passwords.PasswordCharacteristics;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;

public class ActiveDirectoryConnector extends AbstractDirectoryConnector<ActiveDirectoryConfiguration> {

	public static final Iterator<String> STRING_ITERATOR = CollectionUtil.emptyIterator(String.class);

	final static Log LOG = LogFactory.getLog(ActiveDirectoryConnector.class);

	public static final String SAM_ACCOUNT_NAME_ATTRIBUTE = "sAMAccountName";
	public static final String DISPLAY_NAME_ATTRIBUTE = "displayName";
	public static final String GIVEN_NAME_ATTRIBUTE = "givenName";
	public static final String SURNAME_ATTRIBUTE = "sn";
	public static final String INITIALS_ATTRIBUTE = "initials";
	public static final String DESCRIPTION_ATTRIBUTE = "description";
	public static final String OFFICE_ATTRIBUTE = "physicalDeliveryOfficeName";
	public static final String HOME_PAGE_ATTRIBUTE = "wWWHomePage";
	public static final String PROFILE_PATH_ATTRIBUTE = "profilePath";
	public static final String SCRIPT_PATH_ATTRIBUTE = "scriptPath";
	public static final String HOME_DIR_ATTRIBUTE = "homeDirectory";
	public static final String HOME_DRIVE_ATTRIBUTE = "homeDrive";
	public static final String HOME_PHONE_ATTRIBUTE = "homePhone";
	public static final String PAGER_ATTRIBUTE = "pager";
	public static final String FAX_ATTRIBUTE = "facsimileTelephoneNumber";
	public static final String IPPHONE_ATTRIBUTE = "ipPhone";
	public static final String INFO_ATTRIBUTE = "info";
	public static final String TITLE_ATTRIBUTE = "title";
	public static final String DEPARTMENT_ATTRIBUTE = "department";
	public static final String COMPANY_ATTRIBUTE = "company";
	public static final String MANAGER_ATTRIBUTE = "manager";
	public static final String USER_PRINCIPAL_NAME_ATTRIBUTE = "userPrincipalName";
	public static final String OBJECT_GUID_ATTRIBUTE = "objectGUID";
	public static final String USER_ACCOUNT_CONTROL_ATTRIBUTE = "userAccountControl";
	public static final String DISTINGUISHED_NAME_ATTRIBUTE = "distinguishedName";
	public static final String ACCOUNT_EXPIRES_ATTRIBUTE = "accountExpires";
	public static final String LOCKOUT_TIME_ATTRIBUTE = "lockoutTime";
	public static final String LOCKOUT_DURATION_ATTRIBUTE = "lockoutDuration";
	public static final String LAST_LOGON_ATTRIBUTE = "lastLogon";
	public static final String LAST_LOGON_TIMESTAMP_ATTRIBUTE = "lastLogontimeStamp";
	public static final String PRIMARY_GROUP_ID_ATTRIBUTE = "primaryGroupId";
	public static final String PWD_LAST_SET_ATTRIBUTE = "pwdLastSet";
	public static final String PWD_HISTORY_LENGTH = "pwdHistoryLength";
	public static final String MINIMUM_PASSWORD_AGE_ATTRIBUTE = "minPwdAge";
	public static final String MAXIMUM_PASSWORD_AGE_ATTRIBUTE = "maxPwdAge";
	public static final String COMMON_NAME_ATTRIBUTE = "cn";
	public static final String MEMBER_OF_ATTRIBUTE = "memberOf";
	public static final String MEMBER_ATTRIBUTE = "member";
	public static final String MAIL_ATTRIBUTE = "mail";
	public static final String MOBILE_PHONE_NUMBER_ATTRIBUTE = "mobile";
	public static final String PHONE_NUMBER_ATTRIBUTE = "telephoneNumber";
	public static final String OTHER_PHONE_NUMBER_ATTRIBUTE = "otherTelephone";
	public static final String OBJECT_SID_ATTRIBUTE = "objectSID";
	public static final String PWD_PROPERTIES_ATTRIBUTE = "pwdProperties";
	public static final String OU_ATTRIBUTE = "ou";
	public static final String PASSWORD_POLICY_APPLIES = "msDS-ResultantPSO";
	public static final String PASSWORD_EXPIRY_COMPUTED = "msDS-UserPasswordExpiryTimeComputed";
	public static final String PRINCIPAL_NAME_ATTR = "msDS-PrincipalName";
	public static final String PROXY_ADDRESSES = "proxyAddresses";
	public static final String COUNTRY_CODE = "c";
	public static final String COUNTRY = "co";
	public static final String CITY = "l";
	public static final String STATE = "st";
	public static final String STREET_ADRESS = "streetAddress";
	public static final String POST_OFFICE_BOX = "postOfficeBox";
	public static final String POSTAL_CODE = "postalCode";
	public static final String GROUP_TYPE_ATTRIBUTE = "groupType";
	public static final String USN_CHANGED = "uSNChanged";
	public static final String HIGHEST_COMMITTED_USN = "highestCommittedUSN";
	public static final String THUMBNAIL_PHOTO = "thumbnailPhoto";

	/**
	 * This is a special attribute we add to mimic the Office365 ImmutableID
	 */
	public static final String IMMUTABLE_ID_ATTR = "ImmutableID";

	public static final String GLOBAL = "-2147483646";
	public static final String DOMAIN_LOCAL = "-2147483644";
	public static final String UNIVERSAL = "-2147483640";

	private ActiveDirectorySchemaVersion cachedVersion;

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		Set<ConnectorCapability> thisCaps = new HashSet<>(capabilities);
		thisCaps.add(ConnectorCapability.hasPasswordPolicy);
		thisCaps.add(ConnectorCapability.caseInsensitivePrincipalNames);
		thisCaps.add(ConnectorCapability.accountLocking);
		thisCaps.add(ConnectorCapability.accountDisable);
		thisCaps.add(ConnectorCapability.createRole);
		thisCaps.add(ConnectorCapability.updateRole);
		thisCaps.add(ConnectorCapability.deleteRole);
		thisCaps.add(ConnectorCapability.forcePasswordChange);
		thisCaps.add(ConnectorCapability.tag);
		thisCaps.add(ConnectorCapability.thumbnailPhoto);
		return thisCaps;
	}

	/**
	 * These are the attributes we need for operation, but are not stored as actual
	 * attributes in our local database
	 */
	private static Collection<String> DEFAULT_USER_ATTRIBUTES = Arrays
			.asList(new String[] { MEMBER_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE, OU_ATTRIBUTE, DISTINGUISHED_NAME_ATTRIBUTE,
					PRIMARY_GROUP_ID_ATTRIBUTE, USN_CHANGED, HIGHEST_COMMITTED_USN });
	/**
	 * These are attributes we need for operation and want to store as attributes as
	 * well.
	 */
	private static Collection<String> ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE = Arrays
			.asList(new String[] { USER_ACCOUNT_CONTROL_ATTRIBUTE, LAST_LOGON_ATTRIBUTE, LAST_LOGON_TIMESTAMP_ATTRIBUTE,
					PWD_LAST_SET_ATTRIBUTE, OU_ATTRIBUTE, COMMON_NAME_ATTRIBUTE, IMMUTABLE_ID_ATTR, PRINCIPAL_NAME_ATTR });

	/**
	 * These are attributes we need for operation and want to store as attributes as
	 * well.
	 */
	private static Collection<String> ALL_USER_ATTRIBUTES = Arrays.asList(new String[] { SAM_ACCOUNT_NAME_ATTRIBUTE,
			USER_PRINCIPAL_NAME_ATTRIBUTE, USER_ACCOUNT_CONTROL_ATTRIBUTE, ACCOUNT_EXPIRES_ATTRIBUTE,
			LOCKOUT_TIME_ATTRIBUTE, LOCKOUT_DURATION_ATTRIBUTE, LAST_LOGON_ATTRIBUTE, LAST_LOGON_TIMESTAMP_ATTRIBUTE,
			PWD_LAST_SET_ATTRIBUTE, MINIMUM_PASSWORD_AGE_ATTRIBUTE, MAXIMUM_PASSWORD_AGE_ATTRIBUTE,
			COMMON_NAME_ATTRIBUTE, MEMBER_OF_ATTRIBUTE, OBJECT_SID_ATTRIBUTE, PWD_PROPERTIES_ATTRIBUTE, MAIL_ATTRIBUTE,
			PHONE_NUMBER_ATTRIBUTE, MOBILE_PHONE_NUMBER_ATTRIBUTE, OTHER_PHONE_NUMBER_ATTRIBUTE, OU_ATTRIBUTE,
			DISTINGUISHED_NAME_ATTRIBUTE, PASSWORD_EXPIRY_COMPUTED, GIVEN_NAME_ATTRIBUTE, SURNAME_ATTRIBUTE,
			INITIALS_ATTRIBUTE, DESCRIPTION_ATTRIBUTE, OFFICE_ATTRIBUTE, HOME_PAGE_ATTRIBUTE, PROFILE_PATH_ATTRIBUTE,
			SCRIPT_PATH_ATTRIBUTE, HOME_DIR_ATTRIBUTE, HOME_DRIVE_ATTRIBUTE, HOME_PHONE_ATTRIBUTE, PAGER_ATTRIBUTE,
			FAX_ATTRIBUTE, IPPHONE_ATTRIBUTE, INFO_ATTRIBUTE, TITLE_ATTRIBUTE, DEPARTMENT_ATTRIBUTE, COMPANY_ATTRIBUTE,
			MANAGER_ATTRIBUTE, DISPLAY_NAME_ATTRIBUTE, PASSWORD_POLICY_APPLIES, PROXY_ADDRESSES, COUNTRY, COUNTRY_CODE,
			CITY, STATE, STREET_ADRESS, POST_OFFICE_BOX, POSTAL_CODE, PRINCIPAL_NAME_ATTR, THUMBNAIL_PHOTO

	});

	private static Collection<String> CORE_IDENTITY_ATTRIBUTES = Arrays.asList(new String[] { COMMON_NAME_ATTRIBUTE,
			SAM_ACCOUNT_NAME_ATTRIBUTE, USER_PRINCIPAL_NAME_ATTRIBUTE, OBJECT_CLASS_ATTRIBUTE, HIGHEST_COMMITTED_USN });

	private static Collection<String> ALL_ROLE_ATTRIBUTES = Arrays.asList(
			new String[] { OBJECT_SID_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE, GROUP_TYPE_ATTRIBUTE, COMMON_NAME_ATTRIBUTE,
					DISTINGUISHED_NAME_ATTRIBUTE, SAM_ACCOUNT_NAME_ATTRIBUTE, PASSWORD_POLICY_APPLIES, MEMBER_OF_ATTRIBUTE, MEMBER_ATTRIBUTE, USN_CHANGED, HIGHEST_COMMITTED_USN });

	public static final int CHANGE_PASSWORD_AT_NEXT_LOGON_FLAG = 0;
	public static final int CHANGE_PASSWORD_AT_NEXT_LOGON_CANCEL_FLAG = -1;

	// Bit mask values for pwdProperties
	public static final int DOMAIN_PASSWORD_COMPLEX = 0x01;

	private List<String> identityAttributesToRetrieve = new ArrayList<String>(ALL_USER_ATTRIBUTES);

	// Controls for Win2008 R2 password history on admin reset
	final byte[] controlData = { 48, (byte) 132, 0, 0, 0, 3, 2, 1, 1 };
	final String LDAP_SERVER_POLICY_HINTS_OID = "1.2.840.113556.1.4.2066";

	private PasswordCharacteristics passwordCharacteristics = null;
	
	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
		
		if(passwordCharacteristics==null) {
			boolean complex = false;
			String value = getAttributeValue(getRootDn(), PWD_PROPERTIES_ATTRIBUTE);
			if (!StringUtil.isNullOrEmpty(value)) {
				int val = Integer.parseInt(value);
				complex = (val & DOMAIN_PASSWORD_COMPLEX) != 0;
			}
			String minPwdLengthField = getAttributeValue(getRootDn(), "minPwdLength");
			passwordCharacteristics = new ADPasswordCharacteristics(complex,
					minPwdLengthField == null ? 6 : Integer.parseInt(minPwdLengthField), getPasswordHistoryLength(),
					getMaximumPasswordAge(), getMinimumPasswordAge(), 0, "Default Group Policy", null);
		}
		return passwordCharacteristics;
	}

	@Override
	protected void onOpen(ActiveDirectoryConfiguration parameters) {
		super.onOpen(parameters);
		Collection<String> connectorIdentityAttributesToRetrieve = parameters.getIdentityAttributesToRetrieve();
		if (connectorIdentityAttributesToRetrieve != null) {
			for (String s : connectorIdentityAttributesToRetrieve) {
				if (!identityAttributesToRetrieve.contains(s)) {
					identityAttributesToRetrieve.add(s);
				}
			}
		}

		/**
		 * LDP - I believe these add lag to every connection
		 * 
		 * As getSchemeVersion is caching it should not matter where its called. So avoid
		 * calling it now because not every connection will require it.
		 * 
		 * getPasswordCharacteristics is not caching and used a lot in reconciles. Perhaps 
		 * this should cache?
		 */
		//getSchemaVersion();
		//getPasswordCharacteristics();
	}

	protected boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {

		String authType = getConfiguration().getConfigurationParameters()
				.getString(ActiveDirectoryConfiguration.ACTIVE_DIRECTORY_AUTHENTICATION);
		if (StringUtils.isBlank(authType) || "ldap".equals(authType)) {
			return super.areCredentialsValid(identity, password);
		} else {
			doNTLMAuthentication(identity.getPrincipalName(), password);
			return true;
		}
	}

	private void doNTLMAuthentication(String username, char[] password) throws ConnectorException {
		try {
			UniAddress uniaddress = UniAddress.getByName(getConfiguration().getConfigurationParameters()
					.getString(ActiveDirectoryConfiguration.DIRECTORY_HOSTNAME));
			NtlmPasswordAuthentication ntlmpasswordauthentication = new NtlmPasswordAuthentication(
					getDomain(getRootDn()), username, new String(password));
			SmbSession.logon(uniaddress, ntlmpasswordauthentication);
		} catch (UnknownHostException e) {
			throw new ConnectorException(e.getMessage(), e);
		} catch (SmbException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	public boolean checkCredentialsOptimised(String username, String remoteIdentifier, char[] password)
			throws ConnectorException {

		String authType = getConfiguration().getConfigurationParameters()
				.getString(ActiveDirectoryConfiguration.ACTIVE_DIRECTORY_AUTHENTICATION);
		if (StringUtils.isBlank(authType) || "ldap".equals(authType)) {
			return super.checkCredentialsOptimised(username, remoteIdentifier, password);
		} else {
			doNTLMAuthentication(username, password);
			return true;
		}
	}

	@Override
	public Count<Long> countIdentities(OperationContext opContext) throws ConnectorException {
		try {
			USNWrapperIterator<Identity> it = new USNWrapperIterator<Identity>(getIdentities(createTagFilter(buildIdentityFilter(WILDCARD_SEARCH), opContext.getTag()), opContext));
			return new Count<Long>(count(it), it.tag());
		} catch (NamingException e) {
			throw new ConnectorException(processNamingException(e), e);
		} catch (IOException e) {
			throw new ConnectorException(e);
		}
		
	}

	@Override
	public Count<Long> countRoles(OperationContext opContext) throws ConnectorException {
		try {
			USNWrapperIterator<Role> it = new USNWrapperIterator<Role>(getRoles(createTagFilter(buildRoleFilter(WILDCARD_SEARCH, true), opContext.getTag()), true, opContext));
			return new Count<Long>(count(it), it.tag());
		} catch (NamingException e) {
			throw new ConnectorException(processNamingException(e), e);
		} catch (IOException e) {
			throw new ConnectorException(e);
		}
		
	}

	protected Filter createTagFilter(Filter filter, String tag) {
		if (!StringUtil.isNullOrEmpty(tag)) {
			And and = new And();
			and.add(filter);
			/*
			 * There is no our greater than condition for LDAP, so increase the USN value by
			 * 1
			 */
			and.add(new Ge(USN_CHANGED, String.valueOf(Long.parseLong(tag) + 1)));
			filter = and;
		}
		return filter;
	}

	@Override
	public ResultIterator<Identity> allIdentities(OperationContext opContext) throws ConnectorException {
		try {
			return new USNWrapperIterator<Identity>(getIdentities(createTagFilter(buildIdentityFilter(WILDCARD_SEARCH), opContext.getTag()), opContext));
		} catch (NamingException e) {
			throw new ConnectorException(processNamingException(e), e);
		} catch (IOException e) {
			throw new ConnectorException(e);
		}
	}

	@Override
	public final ResultIterator<Role> allRoles(OperationContext opContext) throws ConnectorException {
		try {
			return new USNWrapperIterator<Role>(getRoles(createTagFilter(buildRoleFilter(WILDCARD_SEARCH, true), opContext.getTag()), true, opContext));
		} catch (NamingException e) {
			throw new ConnectorException(processNamingException(e), e);
		} catch (IOException e) {
			throw new ConnectorException(e);
		}
	}

	class USNWrapperIterator<E extends Principal> implements ResultIterator<E> {

		private Iterator<E> delegate;
		private long usn;

		USNWrapperIterator(Iterator<E> delegate) throws NamingException, IOException {
			this.delegate = delegate;
			// https://docs.microsoft.com/en-us/windows/win32/ad/polling-for-changes-using-usnchanged
			Attribute usnAttr = ldapService.getRootDSEAttribute(HIGHEST_COMMITTED_USN);
			if (usnAttr != null) {
				usn = Long.valueOf(String.valueOf(usnAttr.get()));
			}
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public E next() {
			return delegate.next();
		}

		@Override
		public String tag() {
			return String.valueOf(usn);
		}

	}

	@Override
	protected void assignRole(LdapName userDn, Role role) throws NamingException, IOException {
		if (role.getPrincipalName().equals("Domain Users")) {
			LOG.warn(String.format("%s is automatically assigned to Domain Users", userDn));
			return;
		}

		List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
		Attribute attribute = new BasicAttribute(MEMBER_ATTRIBUTE, userDn.toString());
		modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute));

		try {
			ldapService.update(((ActiveDirectoryGroup) role).getDn(),
					modificationItems.toArray(new ModificationItem[0]));
		} catch (Exception nme) {
			LOG.warn(String.format("%s is already assigned to %s", userDn, role.getPrincipalName()), nme);
		}
	}

	@Override
	protected void revokeRole(LdapName userDn, Role role) throws NamingException, IOException {

		List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
		Attribute attribute = new BasicAttribute(MEMBER_ATTRIBUTE, userDn.toString());
		modificationItems.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute));

		ldapService.update(((ActiveDirectoryGroup) role).getDn(), modificationItems.toArray(new ModificationItem[0]));
	}

	protected void processGroupAttributes(List<ModificationItem> modificationItems, Principal previousState,
			Principal newState) {

		for (Map.Entry<String, String[]> entry : newState.getAttributes().entrySet()) {
			if (!isExcludeForGroupUpdate(entry.getKey())) {
				if (!previousState.getAttributes().containsKey(entry.getKey())) {
					// New
					if (entry.getValue().length > 0) {

						String[] value = entry.getValue();
						if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
							Attribute attr = new BasicAttribute(entry.getKey());
							for (String val : value) {
								attr.add(val);
							}
							modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attr));
						}
					}
				} else {
					String[] oldValue = previousState.getAttributes().get(entry.getKey());
					String[] newValue = newState.getAttributes().get(entry.getKey());
					if (!Objects.deepEquals(oldValue, newValue)) {

						String[] value = entry.getValue();
						if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
							Attribute attr = new BasicAttribute(entry.getKey());
							for (String val : value) {
								attr.add(val);
							}
							modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr));
						} else {
							Attribute attr = new BasicAttribute(entry.getKey());
							modificationItems.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr));
						}
					}
				}
			}
		}
	}

	@Override
	public void updateRole(final Role role) throws ConnectorException {

		try {
			List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
			Role oldRole = getRoleByName(role.getPrincipalName());

			// OU may have been provided
			String roleOu = role.getAttribute(getActiveDirectoryConfiguration().getDistinguishedNameAttribute());
			LdapName roleDn = new LdapName(roleOu);
			String principalName = role.getPrincipalName();

			processGroupAttributes(modificationItems, oldRole, role);

			if (Util.differs(oldRole.getAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE), principalName)) {
				Attribute attribute = new BasicAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE, principalName);
				modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute));
			}

			ldapService.update(roleDn, modificationItems.toArray(new ModificationItem[0]));

			// Update roles
			// List<Role> toRemove = new ArrayList<Identity>(Arrays.asList(oldRole.));
			// List<Role> toAdd = new ArrayList<Role>(Arrays.asList(identity.getRoles()));
			// toRemove.removeAll(Arrays.asList(identity.getRoles()));
			// toAdd.removeAll(Arrays.asList(oldIdentity.getRoles()));
			//
			// for(Role r : toRemove) {
			// revokeRole(usersDn, r);
			// }
			//
			// for(Role r : toAdd) {
			// assignRole(usersDn, r);
			// }

			if (Util.differs(oldRole.getAttribute(COMMON_NAME_ATTRIBUTE), role.getAttribute(COMMON_NAME_ATTRIBUTE))) {
				LdapName newDN = new LdapName(roleDn.toString());
				newDN.remove(newDN.size() - 1);
				newDN.add(newDN.size(), "CN=" + role.getAttribute(COMMON_NAME_ATTRIBUTE));
				ldapService.rename(roleDn, newDN);
			} else if (Util.differs(oldRole.getAttribute(OU_ATTRIBUTE), role.getAttribute(OU_ATTRIBUTE))) {
				LdapName newDN = new LdapName(
						"CN=" + role.getAttribute(COMMON_NAME_ATTRIBUTE) + "," + role.getAttribute(OU_ATTRIBUTE));
				ldapService.rename(roleDn, newDN);
			}

		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	protected void processUserAttributes(List<ModificationItem> modificationItems, Identity previousState,
			Map<String, String[]> newAttributes, Identity newState) {

		super.processUserAttributes(modificationItems, previousState, newAttributes, newState);

		String principalNameWithDomain = newState.getPrincipalName() + "@"
				+ getActiveDirectoryConfiguration().getDomain();
		if (!newState.getPrincipalName().equalsIgnoreCase(previousState.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE))
				&& !principalNameWithDomain
						.equalsIgnoreCase(previousState.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE))) {
			Attribute attribute = new BasicAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE, principalNameWithDomain);
			modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute));
		}
	}

	@Override
	protected boolean isExcludeForUserUpdate(String attributeName) {
		return !ALL_USER_ATTRIBUTES.contains(attributeName)
				|| ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE.contains(attributeName);
	}

	private boolean isExcludeForGroupUpdate(String attributeName) {
		return !ALL_ROLE_ATTRIBUTES.contains(attributeName)
				|| ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE.contains(attributeName);
	}

	public Role createRole(Role role) throws ConnectorException {

		try {
			final ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

			// OU may have been provided
			String roleOU = role.getAttribute(OU_ATTRIBUTE);
			LdapName roleDN;
			if (StringUtil.isNullOrEmpty(roleOU)) {
				roleDN = new LdapName(getRootDn().toString());
				if (StringUtil.isNullOrEmpty(config.getOU())) {
					roleDN.add(new Rdn("CN=Users"));
				} else {
					roleDN.add(new Rdn(config.getOU()));
				}
			} else {
				roleDN = new LdapName(roleOU);
			}

			role.setAttribute(OU_ATTRIBUTE, "");

			LdapName roleDn = new LdapName(roleDN.toString());
			String principalName = role.getPrincipalName();

			roleDn.add("CN=" + role.getPrincipalName());

			Name baseDn = getConfiguration().getBaseDn();
			if (!roleDn.toString().toLowerCase().endsWith(baseDn.toString().toLowerCase())) {
				throw new ConnectorException("The Role DN (" + roleDn + ") must be a child of the Base DN (" + baseDn
						+ " configured for the Active Directory connector.");
			}

			/*
			 * Set up the attributes for the primary details. Some of these may already have
			 * been in the generic attributes
			 */
			List<Attribute> attributes = new ArrayList<Attribute>();

			// First copy in the generic attributes
			for (Map.Entry<String, String[]> entry : role.getAttributes().entrySet()) {
				String[] value = entry.getValue();
				if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
					Attribute attr = new BasicAttribute(entry.getKey());
					for (String val : value) {
						attr.add(val);
					}
					attributes.add(attr);
				}
			}

			String[] objectClasses = new String[] { "top", "group" };
			BasicAttribute objectClassAttributeValues = new BasicAttribute(OBJECT_CLASS_ATTRIBUTE);
			for (String objectClass : objectClasses) {
				objectClassAttributeValues.add(objectClass);
			}
			attributes.add(objectClassAttributeValues);

			String sAMAccountName = role.getPrincipalName();

			if (StringUtils.isEmpty(sAMAccountName)) {
				sAMAccountName = principalName;
				if (sAMAccountName.contains("@")) {
					sAMAccountName = sAMAccountName.substring(0, sAMAccountName.indexOf("@"));
				}
			}

			attributes.add(new BasicAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE, sAMAccountName));

			ldapService.bind(roleDn, attributes.toArray(new Attribute[0]));

			return getRoleByName(role.getPrincipalName());

		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}

	}

	protected Collection<String> getCoreIdentityAttributes() {
		return CORE_IDENTITY_ATTRIBUTES;
	}

	@Override
	protected void finaliseCreate(DirectoryIdentity directoryIdentity, boolean forceChange) {
		setForcePasswordChangeAtNextLogon(directoryIdentity, forceChange);
	}

	@Override
	protected String finaliseCreate(Identity identity, String principalName, List<Attribute> attributes) {
		String upn = identity.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE);

		if (StringUtils.isEmpty(upn)) {
			if (!principalName.contains("@")) {
				upn = principalName + "@" + getActiveDirectoryConfiguration().getDomain();
				attributes.add(new BasicAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE, upn));
			} else {
				attributes.add(new BasicAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE, principalName));
			}
		} else {
			attributes.add(new BasicAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE, upn));
		}

		String sAMAccountName = identity.getAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE);

		if (StringUtils.isEmpty(sAMAccountName)) {
			sAMAccountName = principalName;
			if (sAMAccountName.contains("@")) {
				sAMAccountName = sAMAccountName.substring(0, sAMAccountName.indexOf("@"));
			}
		}

		attributes.add(new BasicAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE, sAMAccountName));

		if (!StringUtil.isNullOrEmpty(identity.getAddress(com.identity4j.connector.Media.mobile))) {
			attributes.add(new BasicAttribute(MOBILE_PHONE_NUMBER_ATTRIBUTE,
					identity.getAddress(com.identity4j.connector.Media.mobile)));
		}

		return upn;
	}

	@Override
	public void unlockIdentity(Identity identity) throws ConnectorException {
		if (!(identity instanceof DirectoryIdentity)) {
			throw new IllegalArgumentException("May only unlock LDAP identities.");
		}
		if (identity.getAccountStatus().getType().equals(AccountStatusType.expired)) {
			throw new IllegalStateException("May not unlock expired accounts.");
		}
		if (identity.getAccountStatus().getType().equals(AccountStatusType.unlocked)) {
			throw new IllegalStateException("Account already unlocked.");
		}
		try {
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute(LOCKOUT_TIME_ATTRIBUTE, "0")));
			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
			identity.getAccountStatus().unlock();
		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	public void lockIdentity(Identity identity) throws ConnectorException {
		if (!(identity instanceof DirectoryIdentity)) {
			throw new IllegalArgumentException("May only lock LDAP identities.");
		}
		if (identity.getAccountStatus().getType().equals(AccountStatusType.expired)) {
			throw new IllegalStateException("May not lock expired accounts.");
		}
		if (identity.getAccountStatus().getType().equals(AccountStatusType.locked)) {
			throw new IllegalStateException("Account already locked.");
		}
		try {
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(LOCKOUT_TIME_ATTRIBUTE,
					String.valueOf(ActiveDirectoryDateUtil.javaDataToADTime(new Date())))));
			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
			identity.getAccountStatus().lock();
		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	public void disableIdentity(Identity identity) throws ConnectorException {
		try {
			if (!(identity instanceof DirectoryIdentity)) {
				throw new IllegalArgumentException("May only disable LDAP identities.");
			}
			if (identity.getAccountStatus().getType().equals(AccountStatusType.disabled)) {
				throw new IllegalStateException("Account already disabled.");
			}

			DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
					USER_ACCOUNT_CONTROL_ATTRIBUTE,
					String.valueOf(UserAccountControl.getUserAccountControlFlag(
							Integer.valueOf(
									directoryIdentity.getAttributeOrDefault(USER_ACCOUNT_CONTROL_ATTRIBUTE, "0")),
							false, Collections.<String, Boolean>emptyMap())))));

			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}

	}

	@Override
	public void enableIdentity(Identity identity) throws ConnectorException {
		try {
			if (!(identity instanceof DirectoryIdentity)) {
				throw new IllegalArgumentException("May only disable LDAP identities.");
			}
			if (!identity.getAccountStatus().getType().equals(AccountStatusType.disabled)) {
				throw new IllegalStateException("Account already enabled.");
			}

			DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
					USER_ACCOUNT_CONTROL_ATTRIBUTE,
					String.valueOf(UserAccountControl.getUserAccountControlFlag(
							Integer.valueOf(
									directoryIdentity.getAttributeOrDefault(USER_ACCOUNT_CONTROL_ATTRIBUTE, "0")),
							true, Collections.<String, Boolean>emptyMap())))));

			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));

			directoryIdentity.getAccountStatus().setDisabled(false);
			directoryIdentity.getAccountStatus().calculateType();

		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}

	}

	protected void setForcePasswordChangeAtNextLogon(DirectoryIdentity identity, boolean forcePasswordChangeAtLogon) {

		Collection<ModificationItem> items = new ArrayList<ModificationItem>();
		if (forcePasswordChangeAtLogon) {
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute(PWD_LAST_SET_ATTRIBUTE, String.valueOf(CHANGE_PASSWORD_AT_NEXT_LOGON_FLAG))));
		} else {
			items.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(PWD_LAST_SET_ATTRIBUTE)));
			items.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(PWD_LAST_SET_ATTRIBUTE,
					String.valueOf(CHANGE_PASSWORD_AT_NEXT_LOGON_CANCEL_FLAG))));
		}

		try {
			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
		} catch (NamingException e) {
			throw new ConnectorException(processNamingException(e), e);
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}

	}

	private int getMinimumPasswordAge() {
		return getPasswordAge(MINIMUM_PASSWORD_AGE_ATTRIBUTE);
	}

	private int getMaximumPasswordAge() {
		int maxPasswordAgeDays = ((ActiveDirectoryConfiguration) getConfiguration()).getMaxPasswordAgeDays();
		return maxPasswordAgeDays < 1 ? getPasswordAge(MAXIMUM_PASSWORD_AGE_ATTRIBUTE) : maxPasswordAgeDays;
	}

	private int getPasswordHistoryLength() {
		String value = getAttributeValue(getRootDn(), PWD_HISTORY_LENGTH);
		if (StringUtil.isNullOrEmpty(value)) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	private int getPasswordAge(String attributeName) {
		String value = getAttributeValue(getRootDn(), attributeName);
		if (StringUtil.isNullOrEmpty(value)) {
			return 0;
		}
		return ActiveDirectoryDateUtil.adTimeToJavaDays(Long.parseLong(value));
	}

	@Override
	protected ResultIterator<Role> getRoles(Filter filter, boolean applyFilters, OperationContext opContext) {
		try {
			return ldapService.search(filter, new ResultMapper<Role>() {

				@Override
				public Role apply(SearchResult result) throws NamingException {
					return mapRole(result, opContext);
				}

				public boolean isApplyFilters() {
					return applyFilters;
				}
			}, configureRoleSearchControls(ldapService.getSearchControls()), opContext);
		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}

	}

	public Iterator<ADPasswordCharacteristics> getPasswordPolicies() {

		try {
			return ldapService.search(
					ldapService.buildObjectClassFilter("msDS-PasswordSettings", "cn", WILDCARD_SEARCH),
					new ResultMapper<ADPasswordCharacteristics>() {

						@Override
						public ADPasswordCharacteristics apply(SearchResult result)
								throws NamingException, IOException {
							return loadCharacteristics(result);
						}

						public boolean isApplyFilters() {
							return false;
						}
					}, ldapService.getSearchControls(), OperationContext.createDefault());
		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@Override
	protected SearchControls configureSearchControls(SearchControls searchControls) {
		searchControls = super.configureSearchControls(searchControls);
		List<String> attr = new ArrayList<String>(identityAttributesToRetrieve);
		attr.addAll(DEFAULT_USER_ATTRIBUTES);
		searchControls.setReturningAttributes(attr.toArray(new String[0]));
		return searchControls;
	}

	protected SearchControls configurePSOSearchControls(SearchControls searchControls) {
		searchControls = super.configureSearchControls(searchControls);
		List<String> attr = new ArrayList<String>(Arrays.asList("cn", "msDS-PSOAppliesTo"));
		searchControls.setReturningAttributes(attr.toArray(new String[0]));
		return searchControls;
	}

	@Override
	protected SearchControls configureRoleSearchControls(SearchControls searchControls) {
		searchControls = super.configureSearchControls(searchControls);
		List<String> attr = new ArrayList<String>(ALL_ROLE_ATTRIBUTES);
		searchControls.setReturningAttributes(attr.toArray(new String[0]));
		return searchControls;
	}

	@Override
	protected final void changePassword(Identity identity, char[] oldPassword, char[] password) {
		try {
			ActiveDirectoryConfiguration directoryConfiguration = (ActiveDirectoryConfiguration) getConfiguration();
			String newQuotedPassword = "\"" + new String(password) + "\"";
			String oldQuotedPassword = "\"" + new String(oldPassword) + "\"";

			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
			byte[] oldUnicodePassword = oldQuotedPassword.getBytes("UTF-16LE");

			ModificationItem item1 = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute(directoryConfiguration.getIdentityPasswordAttribute(), oldUnicodePassword));
			ModificationItem item2 = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute(directoryConfiguration.getIdentityPasswordAttribute(), newUnicodePassword));
			ldapService.update(((DirectoryIdentity) identity).getDn(), new ModificationItem[] { item1, item2 });

		} catch (NamingException e) {
			try {
				processNamingException(e);
				throw new IllegalStateException("Unreachable code");
			} catch (PasswordChangeRequiredException pcre) {
				LOG.warn(
						"Could not use change password because 'Change Password At Next Login' was set. Falling back to setPassword. Depending on the version of Active Directory in use, this may bypass password history checks.");
				setPassword(identity, password, false, PasswordResetType.USER);
			}
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unused")
	private boolean isUsePSO() {
		int version = getSchemaVersion().getVersion();
		return version >= ActiveDirectorySchemaVersion.WINDOWS_2008.getVersion();
	}

	protected ActiveDirectorySchemaVersion getSchemaVersion() {
		
		ActiveDirectorySchemaVersion cfgSchema = getConfiguration().getSchema();
		if(cfgSchema != null && cfgSchema != ActiveDirectorySchemaVersion.AUTOMATIC) {
			return cfgSchema;
		}

		if (cachedVersion != null) {
			return cachedVersion;
		}
		
		Throwable firstException = null;
		Name rootDn = getRootDn();

		while(true) {
			try {
	
				/**
				 * TODO we need to fix this for child domains
				 */
				LdapName schemaDn = new LdapName("CN=Schema,CN=Configuration," + rootDn.toString());
				LOG.info("Attempting to get schema version from " + schemaDn);
				String value = getAttributeValue(schemaDn, "objectVersion");
	
				int version = Integer.parseInt(value);
				switch (version) {
				case 13:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2000;
					break;
				case 30:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2003;
					break;
				case 31:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2003_R2;
					break;
				case 44:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2008;
					break;
				case 47:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2008_R2;
					break;
				case 56:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2012;
					break;
				case 69:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2012_R2;
					break;
				case 87:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2016;
					break;
				case 88:
					cachedVersion = ActiveDirectorySchemaVersion.WINDOWS_2019;
					break;
				default:
					LOG.info("Could not determine Active Directory schema version from value " + value);
					if (version < ActiveDirectorySchemaVersion.WINDOWS_2000.getVersion()) {
						cachedVersion = ActiveDirectorySchemaVersion.PRE_WINDOWS_2000;
					} else if (version > ActiveDirectorySchemaVersion.WINDOWS_2019.getVersion()) {
						cachedVersion = ActiveDirectorySchemaVersion.POST_WINDOWS_2019;
					} else {
						cachedVersion = ActiveDirectorySchemaVersion.UNKNOWN;
					}
					break;
				}
	
			} catch (Throwable e) {
				if(firstException == null)
					firstException = e;
			}
			
			if(cachedVersion == null) {
				try {
					rootDn = rootDn.getPrefix(rootDn.size() - 1);
				}
				catch(Exception e) {
					break;
				}
			}
			else {
				break;
			}
		}
		
		if(cachedVersion == null) {
			if(firstException != null) {
				LOG.info("Could not determine Active Directory schema version. Dumping first exception.", firstException);
			}
			cachedVersion = ActiveDirectorySchemaVersion.UNKNOWN;
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Active Directory schema version is " + cachedVersion.name().replaceAll("_", " "));
		}

		return cachedVersion;

	}

	protected String processNamingException(NamingException nme) {
		DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
		String reason = dep.getReason();
		// Now for specific errors. These will hopefully build up over
		// time
		if (reason.equals("0000052D")) {
			// Turns out this is just a very generic error indicating
			// password rules have been violated
			String message = "The new password does not comply with current rules.";
			int minPasswordAge = getMinimumPasswordAge();
			if (minPasswordAge > 0) {
				message += " Your password policy also has a minimum password age of " + minPasswordAge
						+ " days, you will not be able to change password until your current password is older than this.";
			}
			int passwordHistoryLength = getPasswordHistoryLength();
			if (passwordHistoryLength > 0) {
				message += " Your password policy also has password history enabled, you will not be able to use any of your "
						+ passwordHistoryLength + " previous passwords.";
			}
			throw new PasswordPolicyViolationException(message);
		} else if (reason.equals("00000056") || reason.equals("00000057")) {
			throw new PasswordPolicyViolationException(
					"The new password does not comply with the rules enforced by Active Directory. It is also likely you very recently made another password change.",
					nme);
		} else if (reason.equals("00000524")) {
			throw new ConnectorException("Attempt to create account with username that already exists.", nme);
		} else if (reason.equals("0000001F")) {
			throw new ConnectorException(
					"Could not perform the requested operation. Please configure the server to connect to your Active Directory securely over SSL.",
					nme);
		} else if (reason.equals("80090308") && "773".equals(dep.getData())) {
			throw new PasswordChangeRequiredException(
					"Cannot change password when changePasswordPasswordAtNextLogin is set, must use setPassword");
		} else if (reason.equals("80090308") && "525".equals(dep.getData())) {
			throw new ConnectorException("The user cannot be found.");
		} else if (reason.equals("80090308") && "52e".equals(dep.getData())) {
			throw new InvalidLoginCredentialsException("Invalid credentials");
		} else if (reason.equals("80090308") && "530".equals(dep.getData())) {
			throw new ConnectorException("Not permitted to logon at this time");
		} else if (reason.equals("80090308") && "531".equals(dep.getData())) {
			throw new ConnectorException("Not permitted to logon at this workstation");
		} else if (reason.equals("80090308") && "532".equals(dep.getData())) {
			throw new PasswordChangeRequiredException("Your password has expired");
		} else if (reason.equals("80090308") && "533".equals(dep.getData())) {
			throw new ConnectorException("Your account is disabled");
		} else if (reason.equals("80090308") && "534".equals(dep.getData())) {
			throw new ConnectorException("The user has not been granted the requested logon type at this machine");
		} else if (reason.equals("80090308") && "701".equals(dep.getData())) {
			throw new ConnectorException("Your account has expired");
		} else if (reason.equals("80090308") && "775".equals(dep.getData())) {
			throw new ConnectorException("Your account is locked");
		} else if (reason.equals("00000005") && "0".equals(dep.getData())) {
			throw new ConnectorException("Password change not allowed for this account. Check 'User Cannot Change Password' in your Active Directory.");
		}

		/**
		 * Errors are typically thrown up and logged so this causes additional spam in
		 * logs.
		 */
		if (LOG.isDebugEnabled()) {
			LOG.debug(nme.getMessage() + ". Reason code give was " + reason, nme);
		}
		super.processNamingException(nme);

		return reason;

	}

	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon,
			PasswordResetType type) throws ConnectorException {
		try {

			if (forcePasswordChangeAtLogon) {
				switch (identity.getPasswordStatus().getType()) {
				case neverExpires:
					throw new ConnectorException(
							"You cannot force password change at next login as the user's password is set to never expire.");
				case noChangeAllowed:
					throw new ConnectorException(
							"You cannot force password change at next login as the user is not allowed to change their password.");
				default: {

				}
				}
			}
			String newQuotedPassword = "\"" + new String(password) + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
			boolean enforce = getConfiguration().getConfigurationParameters()
					.getBoolean(ActiveDirectoryConfiguration.ACTIVE_DIRECTORY_ENFORCE_PASSWORD_RULES);

			if (type == PasswordResetType.USER) {
				if (!enforce) {
					ldapService.setPassword(((DirectoryIdentity) identity).getDn().toString(), newUnicodePassword);
				} else {
					if (getSchemaVersion().ordinal() >= ActiveDirectorySchemaVersion.WINDOWS_2008_R2.ordinal()) {
						ldapService.setPassword(((DirectoryIdentity) identity).getDn().toString(), newUnicodePassword,
								new BasicControl(LDAP_SERVER_POLICY_HINTS_OID, true, controlData));
					} else {
						ldapService.setPassword(((DirectoryIdentity) identity).getDn().toString(), newUnicodePassword);
					}
				}
			} else {
				ldapService.setPassword(((DirectoryIdentity) identity).getDn().toString(), newUnicodePassword);
			}

			setForcePasswordChangeAtNextLogon((DirectoryIdentity) identity, forcePasswordChangeAtLogon);

		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e);
		}

	}

	private String[] getElements(Attribute attribute) throws NamingException {
		Collection<String> values = new ArrayList<String>();
		for (int index = 0; index < attribute.size(); index++) {
			// TODO how to decide how non-string attributes are
			// converted
			final Object object = attribute.get(index);
			if (object instanceof byte[]) {
				values.add(StringUtil.convertByteToString((byte[]) object));
			} else if (object instanceof String || object instanceof Number || object instanceof Boolean) {
				values.add(object.toString());
			} else {
				LOG.warn("Unknown attribute class, assuming String.");
				values.add(object.toString());
			}
		}
		return values.toArray(new String[values.size()]);
	}

	protected boolean isChildDomain() {
		return getRootDomainDN() != null;
	}

	protected String getRootDomainDN() {
		String objectCategory = getAttributeValue(getRootDn(), "objectCategory");
		if (!objectCategory.toLowerCase().endsWith(getRootDn().toString().toLowerCase())) {
			return objectCategory.substring("CN=Domain-DNS,CN=Schema,CN=Configuration,".length());
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<DirectoryOU> getOrganizationalUnits() throws ConnectorException, IOException {
		Iterator<DirectoryOU> it = super.getOrganizationalUnits();
		List<DirectoryOU> extras = new ArrayList<>();
		if(getConfiguration().isIncludeDefaultUsers()) {
			extras.add(new DirectoryOU(ActiveDirectoryConfiguration.CN_USERS + "," + getRootDn(), "Default Users"));
		}
		if(getConfiguration().isIncludeBuiltInGroups()) {
			extras.add(new DirectoryOU(ActiveDirectoryConfiguration.CN_BUILTIN + "," + getRootDn(), "Default Groups"));
		}
		return new CompoundIterator<DirectoryOU>(extras.iterator(), it);
	}

	@Override
	public ResultIterator<Identity> getIdentities(Filter filter, OperationContext opContext) {

		final ActiveDirectoryConfiguration config = (ActiveDirectoryConfiguration) getConfiguration();
		
		// 3 maps used for fast group lookup
		final Map<String, ActiveDirectoryGroup> groups = new HashMap<String, ActiveDirectoryGroup>();
		final Map<Long, ActiveDirectoryGroup> groupsByRID = new HashMap<Long, ActiveDirectoryGroup>();
		final Map<String, List<String>> userGroups = new HashMap<String, List<String>>();

		if (config.isEnableRoles()) {
			Iterator<Role> it = getRoles(buildRoleFilter(WILDCARD_SEARCH, true), config.isCacheFilteredGroups(), opContext);
			LOG.info(String.format("Pre-caching roles (%s)", config.isCacheFilteredGroups() ? "using filtered groups for cache" : "using unfiltered groups for cache"));
			while (it.hasNext()) {
				ActiveDirectoryGroup group = (ActiveDirectoryGroup) it.next();
				mapGroup(groups, groupsByRID, userGroups, group, group.getDn().toString());
				if(groups.size() % 1000 == 0)
					LOG.info(String.format("Cached %d roles so far for %s", groups.size(), getConfiguration().getDomain()));
			}
			int r = 0;
			LOG.info(String.format("Pre-cached %d roles", groups.size()));
			for(String k : groups.keySet()) {
				r++;
				LOG.info(String.format("    %s", k));
				if(r > 10) {
					LOG.info(String.format("  ... and %d others", groups.size() - 10));
					break;
				}
			}
		}

		final int minimumPasswordAge = getMinimumPasswordAge();
		final int maximumPasswordAge = getMaximumPasswordAge();
		final long lockoutDuration = getBaseLongAttribute(LOCKOUT_DURATION_ATTRIBUTE);

		// If using server side DN member filter, encode a new filter
		if (getActiveDirectoryConfiguration().isFilteredByRole()
				&& getActiveDirectoryConfiguration().getRoleMode().equals(RoleMode.serverDistinguishedNames)) {

			Or incFilters = new Or();
			for (String inc : getActiveDirectoryConfiguration().getIncludedRolesDN()) {
				incFilters.add(new Eq("memberof", inc));
			}

			Or excFilters = new Or();
			for (String exc : getActiveDirectoryConfiguration().getExcludedRolesDN()) {
				excFilters.add(new Eq("memberof", exc));
			}

			Filter groupMemberFilter;
			if (getActiveDirectoryConfiguration().getIncludedRolesDN().isEmpty()) {
				groupMemberFilter = new Not(excFilters);
			} else {
				if (getActiveDirectoryConfiguration().getExcludedRolesDN().isEmpty()) {
					groupMemberFilter = incFilters;
				} else {
					groupMemberFilter = new And().add(incFilters).add(excFilters);
				}
			}

			And newFilter = new And();
			newFilter.add(filter);
			newFilter.add(groupMemberFilter);
			filter = newFilter;
			LOG.info("Final user search filter is '" + filter.encode() + "'");
		}

		try {
			return ldapService.search(filter, new ResultMapper<Identity>() {

				private boolean isAttributeMapped(Attribute attribute) {
					return true;
				}

				@Override
				public Identity apply(SearchResult result) throws NamingException {

					Attributes attributes = result.getAttributes();

					byte[] guidBytes = (byte[]) getAttribute(attributes.get(OBJECT_GUID_ATTRIBUTE));
					String guid = UUID.nameUUIDFromBytes(guidBytes).toString();
					Name udn = new LdapName(result.getNameInNamespace());
					String domain = getDomain(udn);
					String username = selectUsername(result);
					Util.memDbg("User " + username);
					DirectoryIdentity directoryIdentity = new DirectoryIdentity(guid, username, udn);

					// Generate Immutable ID for MSOL services
					String guidBase64 = org.apache.commons.codec.binary.StringUtils
							.newStringUtf8(Base64.encodeBase64(guidBytes, false));
					directoryIdentity.setAttribute(IMMUTABLE_ID_ATTR, guidBase64.trim());

					for (NamingEnumeration<? extends Attribute> attributeEmun = result.getAttributes()
							.getAll(); attributeEmun.hasMoreElements();) {
						Attribute attribute = attributeEmun.nextElement();
						if (isAttributeMapped(attribute)) {
							directoryIdentity.setAttribute(attribute.getID(), getElements(attribute));
						}
					}

					String userPrincipalName = StringUtil
							.nonNull((String) getAttribute(attributes.get(USER_PRINCIPAL_NAME_ATTRIBUTE)));

					// If service account, mark as 'system'
					String serviceAccountDn = config.getServiceAccountDn();
					if (userPrincipalName.equals(serviceAccountDn)) {
						directoryIdentity.setSystem(true);
					}

					String otherName = getOtherName(username, userPrincipalName, domain, config);
					directoryIdentity.setAddress(Media.email, (String) getAttribute(attributes.get(MAIL_ATTRIBUTE)));
					directoryIdentity.setFullName((String) getAttribute(attributes.get(COMMON_NAME_ATTRIBUTE)));
					directoryIdentity.setOtherName(otherName);

					String phoneNumber = (String) getAttribute(attributes.get(MOBILE_PHONE_NUMBER_ATTRIBUTE));
					if (!StringUtil.isNullOrEmpty(phoneNumber)) {
						directoryIdentity.setAddress(Media.mobile, phoneNumber);
					}

					LdapName ou = new LdapName(
							(String) getAttribute(attributes.get(config.getDistinguishedNameAttribute())));
					ou.remove(ou.size() - 1);
					directoryIdentity.setAttribute(OU_ATTRIBUTE, ou.toString());

					// Last sign on
					String lastLogonTimestamp = (String) getAttribute(attributes.get(LAST_LOGON_TIMESTAMP_ATTRIBUTE));
					if (!StringUtil.isNullOrEmpty(lastLogonTimestamp)) {
						long lastLogonTime = Long.parseLong(lastLogonTimestamp);
						if (lastLogonTime > 0) {
							directoryIdentity
									.setLastSignOnDate(ActiveDirectoryDateUtil.adTimeToJavaDate(lastLogonTime));
						}
					}
					String lastLogon = (String) getAttribute(attributes.get(LAST_LOGON_ATTRIBUTE));

					if (directoryIdentity.getLastSignOnDate() == null && !StringUtil.isNullOrEmpty(lastLogon)) {
						long lastLogonTime = Long.parseLong(lastLogon);
						if (lastLogonTime > 0) {
							directoryIdentity
									.setLastSignOnDate(ActiveDirectoryDateUtil.adTimeToJavaDate(lastLogonTime));
						}
					}

					// Calculate the password status
					PasswordStatus passwordStatus = directoryIdentity.getPasswordStatus();
					Date passwordLastSet = trimDate(getDateAttribute(result, PWD_LAST_SET_ATTRIBUTE));
					passwordStatus.setLastChange(passwordLastSet);
					boolean passwordChangeAllowed = isPasswordChangeAllowed(result);
					if (passwordChangeAllowed) {
						passwordStatus.setUnlocked(getAgedDate(minimumPasswordAge, passwordLastSet));
					}

					if (!isPasswordNeverExpire(result)) {
						String passwordexpiryComputed = directoryIdentity.getAttribute(PASSWORD_EXPIRY_COMPUTED);
						if (LOG.isInfoEnabled()) {
							LOG.info(String.format("%s's computed password expiry is %s", username,
									passwordexpiryComputed));
						}

						if (passwordexpiryComputed != null && StringUtils.isNotBlank(passwordexpiryComputed)) {
							long expireTime = Long.parseLong(passwordexpiryComputed);
							if (expireTime > 0 && expireTime < Long.MAX_VALUE) {
								passwordStatus.setExpire(ActiveDirectoryDateUtil.adTimeToJavaDate(expireTime));
							}
						} else if (passwordLastSet != null) {
							passwordStatus.setExpire(getAgedDate(maximumPasswordAge, passwordLastSet));
						}
					}

					String userDn = ActiveDirectoryConfiguration.buildUsername(config.getBaseDn().toString(),
							config.getDomain(), directoryIdentity.getPrincipalName());
					if (userDn.equalsIgnoreCase(getConfiguration().getServiceAccountDn())) {
						// Do not allow the service account password to be reset
						passwordStatus.setType(PasswordStatusType.noChangeAllowed);
					} else if (isPasswordChangeRequired(result)) {
						passwordStatus.setType(PasswordStatusType.changeRequired);
					} else {
						passwordStatus.calculateType();
					}
					String userAccountControl = (String) getAttribute(attributes.get(USER_ACCOUNT_CONTROL_ATTRIBUTE));
					if (StringUtil.isNullOrEmpty(userAccountControl)) {
						throw new ConnectorException(
								"Cannot read the required attribute userAccountControl. Please check the permissions of your service account. Read userAccountControl must be enabled.");
					}

					// Overrides calculated password status, prevent the user
					// changing the password at all
					if (passwordStatus.getType().equals(PasswordStatusType.expired)) {
						if (userAccountControl.length() != 0) {
							if (UserAccountControl.isValueSet(Integer.valueOf(userAccountControl),
									UserAccountControl.DONT_EXPIRE_PASSWORD_FLAG)) {
								passwordStatus.setType(PasswordStatusType.neverExpires);
							}
						}
					}
					if (!passwordChangeAllowed) {
						passwordStatus.setType(PasswordStatusType.noChangeAllowed);
					}

					// Calculate password status
					AccountStatus accountStatus = directoryIdentity.getAccountStatus();
					accountStatus.setExpire(trimDate(getDateAttribute(result, ACCOUNT_EXPIRES_ATTRIBUTE)));
					accountStatus.setLocked(trimDate(getDateAttribute(result, LOCKOUT_TIME_ATTRIBUTE)));
					accountStatus.setUnlocked(null);
					if (userAccountControl.length() != 0) {
						if (UserAccountControl.isValueSet(Integer.valueOf(userAccountControl),
								UserAccountControl.ACCOUNTDISABLE_FLAG)) {
							accountStatus.setDisabled(true);
						}
					}
					accountStatus.calculateType();

					// Now if looked, calculate when unlocked
					if (accountStatus.getType().equals(AccountStatusType.locked)) {
						accountStatus.setUnlocked(
								trimDate(new Date(accountStatus.getLocked().getTime() - (lockoutDuration / 1000))));
					}

					if (config.isEnableRoles()) {
						ActiveDirectoryGroup primaryGroup = null;
						try {
							Long rid = Long
									.parseLong((String) getAttribute(attributes.get(PRIMARY_GROUP_ID_ATTRIBUTE)));
							primaryGroup = groupsByRID.get(rid);
							if (primaryGroup != null) {
								directoryIdentity.addRole(primaryGroup);
							}
						} catch (NumberFormatException e) {
						}
						// Should we include - is the user in any role in the
						// included list (or is this include filter empty, thus
						// all)
						boolean foundRole = false;

						Iterator<String> groupDnsItr= getUsersGroups(result);
						List<String> groupDns = new ArrayList<String>();
						List<String> nonPrimaryGroupDns = new ArrayList<String>();
						if (primaryGroup != null) {
							groupDns.add(primaryGroup.getDn().toString());
						}
						while (groupDnsItr.hasNext()) {
							String n = groupDnsItr.next();
							groupDns.add(n);
							nonPrimaryGroupDns.add(n);
						}

						// TODO nested groups

						/*
						 * If we are using a "server side" role filter, then we already know the user is
						 * part of the role
						 */
						if (getActiveDirectoryConfiguration().getRoleMode().equals(RoleMode.serverDistinguishedNames)) {
							foundRole = true;
						} else {
							// All the following is for filtering users based on
							// their group membership
							// 'NAM' side, rather than on the server.

							// https://social.technet.microsoft.com/Forums/windowsserver/en-US/f5ec5ed6-a909-46df-a52d-97aeb2b5c29e/active-directory-memberof-attribute

							// At a DC for the domain that contains the user,
							// memberOf for the user is complete with respect to
							// membership for groups in that domain; however,
							// memberOf does not contain the user's membership
							// in domain local and global groups in other
							// domains.
							//
							// At a GC server, memberOf for the user is complete
							// with respect to all universal group
							// memberships.
							//
							//
							// To combat this we pre-load the groups and lookup
							// by
							// name in memory

							if (getActiveDirectoryConfiguration().isFilteredByRoleDistinguishedName()) {

								if (getConfiguration().getIncludedRolesDN().size() > 0) {
									for (String r : groupDns) {
										if (containsGroupName(r, getConfiguration().getIncludedRolesDN())) {
											foundRole = true;
											break;
										}
									}
								} else {
									foundRole = true;
								}

								if (foundRole && !getConfiguration().getExcludedRolesDN().isEmpty()) {
									for (String r : groupDns) {
										if (containsGroupName(r, getConfiguration().getExcludedRolesDN())) {
											foundRole = false;
											break;
										}
									}
								}
							} else {
								groupDnsItr = groupDns.iterator();
								while (groupDnsItr.hasNext()) {
									addRoles(groups, groupsByRID, userGroups, config, directoryIdentity, groupDnsItr.next());
								}

								if (getConfiguration().isFilteredByRolePrincipalName() && getConfiguration().getIncludedRoles().size() > 0) {
									for (Role r : directoryIdentity.getRoles()) {
										if (containsGroupName(r.getPrincipalName(), getConfiguration().getIncludedRoles())) {
											foundRole = true;
											break;
										}
									}
								} else {
									foundRole = true;
								}

								// If we are including, making sure we don't
								// need to
								// exlucde
								if (getConfiguration().isFilteredByRolePrincipalName() && foundRole && !getConfiguration().getExcludedRoles().isEmpty()) {
									for (Role r : directoryIdentity.getRoles()) {
										if (containsGroupName(r.getPrincipalName(), getConfiguration().getExcludedRoles())) {
											foundRole = false;
											break;
										}
									}
								}

							}
						}

						if (!foundRole) {
							// No role matches filter
							return null;
						}

						// All except the first group which is already
						// added
						for (String dn : nonPrimaryGroupDns) {
							addRoles(groups, groupsByRID, userGroups, config, directoryIdentity, dn);
						}

					} else {
						directoryIdentity.setRoles(new Role[0]);
					}
					return directoryIdentity;
				}

				public boolean isApplyFilters() {
					return true;
				}
			}, configureSearchControls(ldapService.getSearchControls()), opContext);
		} catch (NamingException e) {
			processNamingException(e);
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}
	
	private void addRoles(final Map<String, ActiveDirectoryGroup> groups, final Map<Long, ActiveDirectoryGroup> groupsByRID, final Map<String, List<String>> userGroups,
			ActiveDirectoryConfiguration config, DirectoryIdentity directoryIdentity, String dn) {
		String dnKey = makeDnKey(dn);
		if (groups.containsKey(dnKey)) {
			directoryIdentity.addRole(groups.get(dnKey));
		} else {
			//
			// NOTE: For now, only load these groups
			// dynamically if there are any role filters. When roles are
			// actually used, this may have to be removed
			// (and another solution to the horrible performance found
			// maybe).
			//
			try {
				if(isIncluded(dn)) {
					LOG.info("Loading role " + dnKey + " because we have role filter or using PSOs and it is not in the cache");
					ActiveDirectoryGroup activeDirectoryGroup = (ActiveDirectoryGroup)getRoleByName(dnKey);
					if (activeDirectoryGroup != null) {
						mapGroup(groups, groupsByRID, userGroups, activeDirectoryGroup, dn);
						directoryIdentity.addRole(activeDirectoryGroup);
					}
				}
				else {
					LOG.info("Ignoring " + dnKey + " because it is not included in the main DN filter.");
				}
			} catch (Exception ex) {
				LOG.info("Ignoring " + dnKey + " because of " + ex.getMessage());
			}
		}
	}

	protected void mapGroup(final Map<String, ActiveDirectoryGroup> groups, final Map<Long, ActiveDirectoryGroup> groupsByRID, final Map<String, List<String>> userGroups,
			ActiveDirectoryGroup group, String dn) {
		groups.put(makeDnKey(dn), group);
		groupsByRID.put(group.getRid(), group);

		for (String s : group.getMember()) {
			String dnKey = makeDnKey(s);
			List<String> l = userGroups.get(dnKey);
			if (l == null) {
				l = new ArrayList<String>();
				userGroups.put(dnKey, l);
			}
			l.add(dn);
		}
		Util.memDbg(String.format("Group cache size %6d  Group: %s ", groups.size(), dn));
	}

	private boolean isPasswordChangeRequired(SearchResult result) throws NamingException {
		try {
			Attributes attributes = result.getAttributes();
			String value = (String) getAttribute(attributes.get(PWD_LAST_SET_ATTRIBUTE));
			long val = Long.parseLong(value);
			if (val == 0) {
				return true;
			}
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	private static String makeDnKey(String dn) {
		/**
		 * LDP: Removed as part of Invalid name fix
		 */
		// https://jira.springsource.org/browse/LDAP-109
		// https://jira.springsource.org/browse/LDAP-109
//		dn = dn.replace("\\\\", "\\\\\\");
//		dn = dn.replace("/", "\\/");
		return dn.toLowerCase();
	}
	
	private boolean containsGroupName(String name, Set<String> groups) {
		for (String g : groups) {
			if (g.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	private Date getAgedDate(int age, Date date) {
		if (age == 0) {
			return null;
		} else {
			Calendar cal = Calendar.getInstance();
			if (date != null) {
				cal.setTime(date);
				cal.add(Calendar.DAY_OF_YEAR, age);
			} else {
				// Set a fixed date way in the future
				cal.setTimeInMillis(0);
				cal.set(Calendar.YEAR, 3999);
			}
			return trimDate(cal.getTime());
		}
	}

	private String getOtherName(String username, String userPrincipalName, String domain,
			ActiveDirectoryConfiguration config) {
		String otherName;
		if (userPrincipalName.equals("")
				|| (!domain.equalsIgnoreCase(config.getDomain()) && userPrincipalName.indexOf('@') == -1)) {
			otherName = username + "@" + domain;
		} else {
			otherName = userPrincipalName;
		}
		return otherName;
	}

	private String getDomain(Name dn) {
		Enumeration<String> e = dn.getAll();
		String domain = "";
		while (e.hasMoreElements()) {
			String el = e.nextElement();
			if (el.toLowerCase().startsWith("dc=")) {
				domain = el.substring(3) + (domain.equals("") ? "" : "." + domain);
			}
		}
		return domain;
	}

	private String selectUsername(SearchResult searchResult) throws NamingException {

		ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

		Attributes attributes = searchResult.getAttributes();

		String domain = getDomain(new LdapName(searchResult.getNameInNamespace()));
		boolean isPrimaryDomain = domain.equalsIgnoreCase(config.getDomain());
		String samAccountName = StringUtil.nonNull((String) getAttribute(attributes.get(SAM_ACCOUNT_NAME_ATTRIBUTE)));
		String userPrincipalName = StringUtil
				.nonNull((String) getAttribute(attributes.get(USER_PRINCIPAL_NAME_ATTRIBUTE)));
		String username = StringUtil.getBeforeLast(userPrincipalName, "@");

		if (username.equals(userPrincipalName)) {
			/** there is no username in UPN, WE HAVE to use the samAccountName **/
			if (isPrimaryDomain) {
				return samAccountName;
			} else {
				return samAccountName + "@" + domain;
			}
		}

		return config.isUsernameSamAccountName() && isPrimaryDomain ? samAccountName
				: fixUserPrincipalName(userPrincipalName, domain);
	}
	
	private String selectGroupName(Attributes attributes, LdapName nameInNamespace) throws NamingException {
		ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();
		String domain = getDomain(nameInNamespace);
		boolean isPrimaryDomain = domain.equalsIgnoreCase(config.getDomain());
		String samAccountName = StringUtil.nonNull((String) getAttribute(attributes.get(SAM_ACCOUNT_NAME_ATTRIBUTE)));
		String groupPrincipalName = StringUtil
				.nonNull((String) getAttribute(attributes.get(getConfiguration().getRoleNameAttribute())));
		String group = StringUtil.getBeforeLast(groupPrincipalName, "@");

		if (group.equals(groupPrincipalName)) {
			/** there is no group in UPN, WE HAVE to use the samAccountName **/
			if (isPrimaryDomain) {
				return samAccountName;
			} else {
				return samAccountName + "@" + domain;
			}
		}

		return config.isGroupSamAccountName() && isPrimaryDomain ? samAccountName
				: fixUserPrincipalName(groupPrincipalName, domain);
	}

	/**
	 * For some reason the userPrincipalName's domain must be upper case. If not
	 * then the user can't login as the domain cannot be found.
	 * 
	 * @param userPrincipalName
	 * @return String
	 */
	private String fixUserPrincipalName(String userPrincipalName, String domain) {

		ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();
		int indexOf = userPrincipalName.lastIndexOf("@");
		if (indexOf == -1 && domain.equalsIgnoreCase(config.getDomain())) {
			return userPrincipalName;
		} else {
			if (indexOf == -1) {
				userPrincipalName = userPrincipalName + "@" + domain;
			}
		}

		if (StringUtil.getAfterLast(userPrincipalName, "@").equalsIgnoreCase(domain)) {
			return StringUtil.getBefore(userPrincipalName, "@");
		} else {
			return userPrincipalName;
		}
	}

	private long getBaseLongAttribute(String attributeName) {
		try {
			String value = getAttributeValue(getConfiguration().getBaseDn(), attributeName);
			long val = Long.parseLong(value);
			return val;
		} catch (NumberFormatException nfe) {
		}
		return 0;
	}

	private Date trimDate(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (cal.get(Calendar.YEAR) > 3999) {
			cal.setTimeInMillis(0);
			cal.set(Calendar.YEAR, 3999);
		}
		return cal.getTime();
	}

	private Date getDateAttribute(SearchResult result, String dateAttribute) throws NamingException {

		try {
			Attributes attributes = result.getAttributes();

			String value = StringUtil.nonNull((String) getAttribute(attributes.get(dateAttribute)));

			/**
			 * LDP - Long.MAX_VALUE also indicates non-expiry of account/password
			 */
			long val = Long.parseLong(value);
			if (val > 0 && val < Long.MAX_VALUE) {
				return ActiveDirectoryDateUtil.adTimeToJavaDate(val);
			}
		} catch (NumberFormatException nfe) {
		}
		return null;
	}

	/**
	 * The userAccountControl flag is used to check this. If they don't have one or
	 * it's not an Integer (neither of which should actually be possible), then we
	 * simply assume it's not allowed.
	 * 
	 * @param attributes
	 * @return true if the last password change date should be required
	 * @throws NamingException
	 */
	private boolean isPasswordChangeAllowed(SearchResult result) throws NamingException {

		Attributes attributes = result.getAttributes();

		String attributeValue = StringUtil
				.nonNull((String) getAttribute(attributes.get(USER_ACCOUNT_CONTROL_ATTRIBUTE)));

		if (attributeValue.length() == 0) {
			return false;
		}

		try {
			int userAccountControl = Integer.valueOf(attributeValue);
			return UserAccountControl.isPasswordChangePermitted(userAccountControl);
		} catch (NumberFormatException nfe) {
			return true;
		}
	}

	private boolean isPasswordNeverExpire(SearchResult result) throws NamingException {
		Attributes attributes = result.getAttributes();

		String attributeValue = StringUtil
				.nonNull((String) getAttribute(attributes.get(USER_ACCOUNT_CONTROL_ATTRIBUTE)));
		if (attributeValue.length() == 0) {
			return false;
		}
		try {
			int userAccountControl = Integer.valueOf(attributeValue);
			return UserAccountControl.isPasswordNeverExpire(userAccountControl);
		} catch (NumberFormatException nfe) {
			return true;
		}
	}

	@Override
	protected Filter buildRoleFilter(String roleName, boolean isWildcard) {

		ActiveDirectoryConfiguration activeDirectoryConfiguration = getActiveDirectoryConfiguration();

		Or inner = new Or();
		if(!getConfiguration().getRoleNameAttribute().equals(SAM_ACCOUNT_NAME_ATTRIBUTE))
			inner.add(new Eq(SAM_ACCOUNT_NAME_ATTRIBUTE, roleName));
		inner.add(new Eq(getConfiguration().getRoleNameAttribute(), roleName));

		String groupPrincipalName = roleName;
		if (!activeDirectoryConfiguration.isGroupSamAccountName() && !roleName.equals(WILDCARD_SEARCH) && !isWildcard) {
			int idx = groupPrincipalName.indexOf('@');
			if (idx == -1) {
				groupPrincipalName += "@" + activeDirectoryConfiguration.getDomain();
				inner.add(new Eq(getConfiguration().getRoleNameAttribute(), groupPrincipalName));
			}
		}

		And outer = new And();
		outer.add(new Not(new Eq(OBJECT_CLASS_ATTRIBUTE, "computer")));
		outer.add(new Eq(OBJECT_CLASS_ATTRIBUTE, getConfiguration().getRoleObjectClass()));
		outer.add(inner);

		return outer;
	}

	@Override
	protected Filter buildIdentityFilter(String identityName) {
		ActiveDirectoryConfiguration activeDirectoryConfiguration = getActiveDirectoryConfiguration();

		Or inner = new Or();
		inner.add(new Eq(SAM_ACCOUNT_NAME_ATTRIBUTE, identityName));
		inner.add(new Eq(USER_PRINCIPAL_NAME_ATTRIBUTE, identityName));

		String userPrincipalName = identityName;
		if (!activeDirectoryConfiguration.isUsernameSamAccountName() && !userPrincipalName.equals(WILDCARD_SEARCH)) {
			int idx = userPrincipalName.indexOf('@');
			if (idx == -1) {
				userPrincipalName += "@" + activeDirectoryConfiguration.getDomain();
				inner.add(new Eq(USER_PRINCIPAL_NAME_ATTRIBUTE, userPrincipalName));
			}
		}

		And outer = new And();
		outer.add(new Not(new Eq(OBJECT_CLASS_ATTRIBUTE, "computer")));
		outer.add(new Eq(OBJECT_CLASS_ATTRIBUTE, "user"));
		outer.add(inner);

		return outer;
	}

	private Iterator<String> getUsersGroups(SearchResult result) throws NamingException {
		Attributes attributes = result.getAttributes();

		Attribute attribute = attributes.get(MEMBER_OF_ATTRIBUTE);
		if (attribute != null) {
			NamingEnumeration<?> all = attribute.getAll();

			List<String> values = new ArrayList<String>();

			while (all.hasMore()) {
				values.add((String) all.next());
			}

			return values.iterator();
		}
		return STRING_ITERATOR;
	}

	@Override
	protected void assertPasswordChangeIsAllowed(Identity identity, char[] oldPassword, char[] password)
			throws ConnectorException {
		if (identity.getPasswordStatus().isNeedChange()) {
			return;
		}
		Date lastPasswordChange = identity.getPasswordStatus().getLastChange();
		int days = getMinimumPasswordAge();
		if (days > 0) {
			if (lastPasswordChange != null && !Util.isDatePast(lastPasswordChange, days)) {
				throw new PasswordChangeTooSoonException(lastPasswordChange);
			}
		}
	}

	@Override
	protected ActiveDirectoryGroup mapRole(String dn, Attributes attributes, LdapName nameInNamespace, OperationContext opContext)
			throws NamingException, InvalidNameException {
		String commonName = selectGroupName(attributes, nameInNamespace);
		if (commonName.length() != 0) {
			Util.memDbg(String.format("Group %s ", dn));
			String guid = UUID.nameUUIDFromBytes((byte[]) getAttribute(attributes.get(OBJECT_GUID_ATTRIBUTE)))
					.toString();
			byte[] sid = (byte[]) getAttribute(attributes.get(OBJECT_SID_ATTRIBUTE));
			
			Iterator<String> parentGroups = getGroups(attributes);
			List<String> memberOf = new ArrayList<String>();
			while (parentGroups.hasNext()) {
				memberOf.add(parentGroups.next());
			}

			String[] member = getStringAttributes(attributes, MEMBER_ATTRIBUTE);
			if (member == null) {
				member = new String[0];
			}
			
			ActiveDirectoryGroup group = new ActiveDirectoryGroup(guid, commonName, new LdapName(dn), sid, memberOf.toArray(new String[0]), member);

			NamingEnumeration<? extends Attribute> en = attributes.getAll();
			while (en.hasMoreElements()) {
				Attribute attribute = en.nextElement();
				group.setAttribute(attribute.getID(), getElements(attribute));
			}

			LdapName ou = new LdapName((String) getAttribute(
					attributes.get(getActiveDirectoryConfiguration().getDistinguishedNameAttribute())));
			ou.remove(ou.size() - 1);
			group.setAttribute(OU_ATTRIBUTE, ou.toString());

			return group;
		}

		return null;
	}

	private ActiveDirectoryConfiguration getActiveDirectoryConfiguration() {
		return (ActiveDirectoryConfiguration) getConfiguration();
	}

	protected ADPasswordCharacteristics loadCharacteristics(SearchResult pso) throws NamingException, IOException {

		boolean complex = false;

		Attributes attributes = pso.getAttributes();

		String value = getStringAttribute(attributes, "msDS-PasswordComplexityEnabled");
		if (!StringUtil.isNullOrEmpty(value)) {
			complex = Boolean.parseBoolean(value);
		}

		PasswordCharacteristics defaults = null;

		// Min length
		String minPwdLengthField = getStringAttribute(attributes, "msDS-MinimumPasswordLength");
		if (minPwdLengthField == null) {
			LOG.warn("msDS-MinimumPasswordLength is null. Please check your PSO configuration.");
			if (defaults == null) {
				defaults = getPasswordCharacteristics();
			}
			minPwdLengthField = String.valueOf(defaults.getMinimumSize());
		}

		// History
		String historyLength = getStringAttribute(attributes, "msDS-PasswordHistoryLength");
		if (historyLength == null) {
			LOG.warn("msDS-PasswordHistoryLength is null. Please check your PSO configuration.");
			if (defaults == null) {
				defaults = getPasswordCharacteristics();
			}
			String attr = defaults.getAttributes()
					.get("activeDirectory." + ActiveDirectoryConnector.PWD_HISTORY_LENGTH);
			historyLength = attr == null ? "0" : attr;
		}

		// Max age
		String maxAge = getStringAttribute(attributes, "msDS-MaximumPasswordAge");
		if (maxAge == null) {
			LOG.warn("msDS-MaximumPasswordAge is null. Please check your PSO configuration.");
			if (defaults == null) {
				defaults = getPasswordCharacteristics();
			}
			String attr = defaults.getAttributes()
					.get("activeDirectory." + ActiveDirectoryConnector.MAXIMUM_PASSWORD_AGE_ATTRIBUTE);
			maxAge = attr == null ? "0" : attr;
		}

		String minAge = getStringAttribute(attributes, "msDS-MinimumPasswordAge");
		if (minAge == null) {
			LOG.warn("msDS-MinimumPasswordAge is null. Please check your PSO configuration.");
			if (defaults == null) {
				defaults = getPasswordCharacteristics();
			}
			String attr = defaults.getAttributes()
					.get("activeDirectory." + ActiveDirectoryConnector.MINIMUM_PASSWORD_AGE_ATTRIBUTE);
			minAge = attr == null ? "0" : attr;
		}

		String precedence = getStringAttribute(attributes, "msDS-PasswordSettingsPrecedence");
		if (precedence == null) {
			LOG.warn("msDS-PasswordSettingsPrecedence is null. Please check your PSO configuration.");
			precedence = "0";
		}

		return new ADPasswordCharacteristics(complex, Integer.parseInt(minPwdLengthField),
				Integer.parseInt(historyLength), ActiveDirectoryDateUtil.adTimeToJavaDays(Long.parseLong(maxAge)),
				ActiveDirectoryDateUtil.adTimeToJavaDays(Long.parseLong(minAge)), Integer.parseInt(precedence),
				getStringAttribute(attributes, "cn"), pso.getNameInNamespace());

	}


	public class CompoundIterator<I> implements Iterator<I> {

		private List<Iterator<? extends I>> iterators = new LinkedList<>();
		private Iterator<Iterator<? extends I>> iteratorsIterator;
		private Iterator<? extends I> iterator;
		private I element;
		private I current;

		public CompoundIterator() {
		}

		@SuppressWarnings("unchecked")
		public CompoundIterator(Iterator<? extends I>... iterators) {
			this.iterators.addAll(Arrays.asList(iterators));
		}

		public void addIterator(Iterator<? extends I> iterator) {
			if (iteratorsIterator != null)
				throw new IllegalStateException("Cannot add iterators after started iterating.");
			iterators.add(iterator);
		}

		@Override
		public void remove() {
			if(current == null)
				throw new IllegalStateException();
			iterator.remove();
		}

		@Override
		public boolean hasNext() {
			checkNext();
			return iterator != null;
		}

		private void checkNext() {
			if (element == null) {
				if (iteratorsIterator == null)
					iteratorsIterator = iterators.iterator();

				while (true) {
					if (iterator == null)
						if (iteratorsIterator.hasNext())
							iterator = iteratorsIterator.next();
						else
							break;

					if (iterator.hasNext()) {
						element = iterator.next();
						break;
					} else
						iterator = null;
				}
			}
		}

		@Override
		public I next() {
			checkNext();
			if (element == null)
				throw new NoSuchElementException();
			try {
				return element;
			} finally {
				current = element;
				element = null;
			}
		}
	}
}