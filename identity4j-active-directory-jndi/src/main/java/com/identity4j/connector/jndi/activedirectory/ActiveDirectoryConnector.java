/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.Media;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PasswordPolicyViolationException;
import com.identity4j.connector.jndi.directory.DirectoryConnector;
import com.identity4j.connector.jndi.directory.DirectoryExceptionParser;
import com.identity4j.connector.jndi.directory.DirectoryIdentity;
import com.identity4j.connector.jndi.directory.LdapService.ResultMapper;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.AccountStatusType;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.CollectionUtil;
import com.identity4j.util.StringUtil;
import com.identity4j.util.Util;
import com.identity4j.util.passwords.PasswordCharacteristics;

public class ActiveDirectoryConnector extends DirectoryConnector {

	public static final Iterator<String> STRING_ITERATOR = CollectionUtil
			.emptyIterator(String.class);

	final static Log LOG = LogFactory.getLog(ActiveDirectoryConnector.class);

	public static final String SAM_ACCOUNT_NAME_ATTRIBUTE = "sAMAccountName";
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

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		if (!capabilities.contains(ConnectorCapability.hasPasswordPolicy)) {
			capabilities.add(ConnectorCapability.hasPasswordPolicy);
			capabilities.add(ConnectorCapability.caseInsensitivePrincipalNames);
			capabilities.add(ConnectorCapability.accountLocking);
			capabilities.add(ConnectorCapability.accountDisable);
		}
		return capabilities;
	}

	/**
	 * These are the attributes we need for operation, but are not stored as
	 * actual attributes in our local database
	 */
	private static Collection<String> DEFAULT_USER_ATTRIBUTES = Arrays
			.asList(new String[] { MEMBER_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE,
					OU_ATTRIBUTE, DISTINGUISHED_NAME_ATTRIBUTE,
					PRIMARY_GROUP_ID_ATTRIBUTE });
	/**
	 * These are attributes we need for operation and want to store as
	 * attributes as well.
	 */
	private static Collection<String> ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE = Arrays
			.asList(new String[] { USER_ACCOUNT_CONTROL_ATTRIBUTE,
					LAST_LOGON_ATTRIBUTE, LAST_LOGON_TIMESTAMP_ATTRIBUTE,
					PWD_LAST_SET_ATTRIBUTE, OU_ATTRIBUTE, COMMON_NAME_ATTRIBUTE });

	/**
	 * These are attributes we need for operation and want to store as
	 * attributes as well.
	 */
	private static Collection<String> ALL_USER_ATTRIBUTES = Arrays
			.asList(new String[] { SAM_ACCOUNT_NAME_ATTRIBUTE,
					USER_PRINCIPAL_NAME_ATTRIBUTE,
					USER_ACCOUNT_CONTROL_ATTRIBUTE, ACCOUNT_EXPIRES_ATTRIBUTE,
					LOCKOUT_TIME_ATTRIBUTE, LOCKOUT_DURATION_ATTRIBUTE,
					LAST_LOGON_ATTRIBUTE, LAST_LOGON_TIMESTAMP_ATTRIBUTE,
					PWD_LAST_SET_ATTRIBUTE, MINIMUM_PASSWORD_AGE_ATTRIBUTE,
					MAXIMUM_PASSWORD_AGE_ATTRIBUTE, COMMON_NAME_ATTRIBUTE,
					MEMBER_OF_ATTRIBUTE, OBJECT_SID_ATTRIBUTE,
					PWD_PROPERTIES_ATTRIBUTE, MAIL_ATTRIBUTE,
					PHONE_NUMBER_ATTRIBUTE, MOBILE_PHONE_NUMBER_ATTRIBUTE,
					OTHER_PHONE_NUMBER_ATTRIBUTE, OU_ATTRIBUTE,
					DISTINGUISHED_NAME_ATTRIBUTE });

	// private static Collection<String> ALL_ROLE_ATTRIBUTES = Arrays.asList(new
	// String[] { MEMBER_OF_ATTRIBUTE, MEMBER_ATTRIBUTE,
	// OBJECT_SID_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE, COMMON_NAME_ATTRIBUTE });

	private static Collection<String> ALL_ROLE_ATTRIBUTES = Arrays
			.asList(new String[] { OBJECT_SID_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE,
					COMMON_NAME_ATTRIBUTE, DISTINGUISHED_NAME_ATTRIBUTE });

	public static final int CHANGE_PASSWORD_AT_NEXT_LOGON_FLAG = 0;
	public static final int CHANGE_PASSWORD_AT_NEXT_LOGON_CANCEL_FLAG = -1;

	// Bit mask values for pwdProperties
	public static final int DOMAIN_PASSWORD_COMPLEX = 0x01;

	// private static Map<String, String> childDomainControllers = new
	// HashMap<String, String>();

	private List<String> identityAttributesToRetrieve = new ArrayList<String>(
			ALL_USER_ATTRIBUTES);

	// Controls for Win2008 R2 password history on admin reset
	final byte[] controlData = { 48, (byte) 132, 0, 0, 0, 3, 2, 1, 1 };
	BasicControl[] controls = new BasicControl[1];
	final String LDAP_SERVER_POLICY_HINTS_OID = "1.2.840.113556.1.4.2066";

	// TODO not yet used
	// public static final int DOMAIN_PASSWORD_NO_ANON_CHANGE = 0x02;
	// public static final int DOMAIN_PASSWORD_NO_CLEAR_CHANGE = 0x04;
	// public static final int DOMAIN_LOCKOUT_ADMINS = 0x08;
	// public static final int DOMAIN_PASSWORD_STORE_CLEARTEXT = 0x10;
	// public static final int DOMAIN_REFUSE_PASSWORD_CHANGE = 0x20;

	@Override
	public PasswordCharacteristics getPasswordCharacteristics() {
		boolean complex = false;
		String value = getAttributeValue(getRootDn(), PWD_PROPERTIES_ATTRIBUTE);
		if (!StringUtil.isNullOrEmpty(value)) {
			int val = Integer.parseInt(value);
			complex = (val & DOMAIN_PASSWORD_COMPLEX) != 0;
		}
		String minPwdLengthField = getAttributeValue(getRootDn(),
				"minPwdLength");
		return new ADPasswordCharacteristics(complex,
				minPwdLengthField == null ? 6 : Integer
						.parseInt(minPwdLengthField),
				getPasswordHistoryLength(), getMaximumPasswordAge(),
				getMinimumPasswordAge());
	}

	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) {
		super.onOpen(parameters);
		Collection<String> connectorIdentityAttributesToRetrieve = parameters
				.getIdentityAttributesToRetrieve();
		if (connectorIdentityAttributesToRetrieve != null) {
			for (String s : connectorIdentityAttributesToRetrieve) {
				if (!identityAttributesToRetrieve.contains(s)) {
					identityAttributesToRetrieve.add(s);
				}
			}
		}
	}

	@Override
	public void deleteIdentity(String principalName) throws ConnectorException {
		try {
			Identity identity = getIdentityByName(principalName);
			String identityOU = identity
					.getAttribute(DISTINGUISHED_NAME_ATTRIBUTE);

			ldapService.unbind(new LdapName(identityOU));
		} catch (InvalidNameException e) {
			LOG.error("Problem in delete identity", e);
		} catch (NamingException e) {
			LOG.error("Problem in delete identity", e);
		} catch (IOException e) {
			LOG.error("Problem in delete identity", e);
		}
	}

	private void assignRole(LdapName userDn, Role role) throws NamingException, IOException {
		
		List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
		Attribute attribute = new BasicAttribute(MEMBER_ATTRIBUTE, userDn.toString());
		modificationItems.add(new ModificationItem(
				DirContext.ADD_ATTRIBUTE, attribute));
		
		ldapService.update(((ActiveDirectoryGroup)role).getDn(),
				modificationItems.toArray(new ModificationItem[0]));
	}
	
	private void revokeRole(LdapName userDn, Role role) throws NamingException, IOException {
		
		List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
		Attribute attribute = new BasicAttribute(MEMBER_ATTRIBUTE, userDn.toString());
		modificationItems.add(new ModificationItem(
				DirContext.REMOVE_ATTRIBUTE, attribute));

		ldapService.update(((ActiveDirectoryGroup)role).getDn(),
				modificationItems.toArray(new ModificationItem[0]));
	}
	
	@Override
	public void updateIdentity(final Identity identity)
			throws ConnectorException {

		try {
			List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();
			Identity oldIdentity = getIdentityByName(identity
					.getPrincipalName());

			final ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

			// OU may have been provided
			String identityOU = identity
					.getAttribute(DISTINGUISHED_NAME_ATTRIBUTE);
			LdapName usersDn = new LdapName(identityOU);
			String principalName = identity.getPrincipalName();

			for (Map.Entry<String, String[]> entry : identity.getAttributes()
					.entrySet()) {
				if (!isExcludeForUpdate(entry.getKey())) {
					if (!oldIdentity.getAttributes()
							.containsKey(entry.getKey())) {
						// New
						if (entry.getValue().length > 0
								&& entry.getValue()[0].length() > 0) {

							Attribute attribute = new BasicAttribute(
									entry.getKey(), identity.getAttribute(entry
											.getKey()));
							modificationItems.add(new ModificationItem(
									DirContext.ADD_ATTRIBUTE, attribute));
						}
					} else {
						String oldValue = oldIdentity.getAttribute(entry
								.getKey());
						String newValue = identity.getAttribute(entry.getKey());
						if (Util.differs(oldValue, newValue)) {

							Attribute attribute = new BasicAttribute(
									entry.getKey(), newValue);
							modificationItems.add(new ModificationItem(
									DirContext.REPLACE_ATTRIBUTE, attribute));
						}
					}
				}
			}

			String principalNameWithDomain = principalName + "@"
					+ config.getDomain();
			if (Util.differs(
					oldIdentity.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE),
					principalNameWithDomain)) {
				Attribute attribute = new BasicAttribute(
						USER_PRINCIPAL_NAME_ATTRIBUTE, principalNameWithDomain);
				modificationItems.add(new ModificationItem(
						DirContext.REPLACE_ATTRIBUTE, attribute));
			}

			String contactDetail = identity
					.getAddress(com.identity4j.connector.Media.mobile);
			if (!StringUtil.isNullOrEmpty(contactDetail)) {
				Attribute attribute = new BasicAttribute(
						MOBILE_PHONE_NUMBER_ATTRIBUTE, contactDetail);
				modificationItems.add(new ModificationItem(
						DirContext.REPLACE_ATTRIBUTE, attribute));
			}

			ldapService.update(usersDn,
					modificationItems.toArray(new ModificationItem[0]));

			
			// Update roles
			List<Role> toRemove = new ArrayList<Role>(Arrays.asList(oldIdentity.getRoles()));
			List<Role> toAdd = new ArrayList<Role>(Arrays.asList(identity.getRoles()));
			toRemove.removeAll(Arrays.asList(identity.getRoles()));
			toAdd.removeAll(Arrays.asList(oldIdentity.getRoles()));
			
			for(Role r : toRemove) {
				LOG.info("REMOVEME: revoking " + r.getPrincipalName());
				revokeRole(usersDn, r);
			}
			
			for(Role r : toAdd) {
				LOG.info("REMOVEME: assigning " + r.getPrincipalName());
				assignRole(usersDn, r);
			}
			
			if (Util.differs(oldIdentity.getFullName(), identity.getFullName())) {
				LdapName newDN = new LdapName(usersDn.getSuffix(1).toString());
				newDN.add(0, "CN=" + identity.getFullName());
				ldapService.rename(usersDn, newDN);
			} else if (Util.differs(
					oldIdentity.getAttribute(COMMON_NAME_ATTRIBUTE),
					identity.getAttribute(COMMON_NAME_ATTRIBUTE))) {
				LdapName newDN = new LdapName(usersDn.toString());
				newDN.remove(newDN.size() - 1);
				newDN.add(newDN.size(),
						"CN=" + identity.getAttribute(COMMON_NAME_ATTRIBUTE));
				ldapService.rename(usersDn, newDN);
			} else if (Util.differs(oldIdentity.getAttribute(OU_ATTRIBUTE),
					identity.getAttribute(OU_ATTRIBUTE))) {
				LdapName newDN = new LdapName("CN="
						+ identity.getAttribute(COMMON_NAME_ATTRIBUTE) + ","
						+ identity.getAttribute(OU_ATTRIBUTE));
				ldapService.rename(usersDn, newDN);
			}

		} catch (NamingException e) {
			LOG.error("Problem in update identity", e);
		} catch (IOException e) {
			LOG.error("Problem in update identity", e);
		}

	}

	private boolean isExcludeForUpdate(String attributeName) {
		return ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE.contains(attributeName);
	}

	@Override
	public Identity createIdentity(final Identity identity, char[] password)
			throws ConnectorException {
		try {
			final ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

			// OU may have been provided
			String identityOU = identity.getAttribute(OU_ATTRIBUTE);
			LdapName usersDn;
			if (StringUtil.isNullOrEmpty(identityOU)) {
				usersDn = new LdapName(getRootDn().toString());
				if (StringUtil.isNullOrEmpty(config.getOU())) {
					usersDn.add(new Rdn("CN=Users"));
				} else {
					usersDn.add(new Rdn(config.getOU()));
				}
			} else {
				usersDn = new LdapName(identityOU);
			}

			LdapName userDn = new LdapName(usersDn.toString());
			String principalName = identity.getPrincipalName();

			userDn.add("CN=" + identity.getFullName());

			Name baseDn = getConfiguration().getBaseDn();
			if (!userDn.toString().endsWith(baseDn.toString())) {
				throw new ConnectorException("The User DN (" + userDn
						+ ") must be a child of the Base DN (" + baseDn
						+ " configured for the Active Directory connector.");
			}

			/*
			 * Set up the attributes for the primary details. Some of these may
			 * already have been in the generic attributes
			 */
			List<Attribute> attributes = new ArrayList<Attribute>();

			// First copy in the generic attributes
			for (Map.Entry<String, String[]> entry : identity.getAttributes()
					.entrySet()) {
				String[] value = entry.getValue();
				// TODO not entirely sure about this
				if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
					LOG.info("Setting attribute " + entry.getKey() + " " + value[0]);
					attributes.add(new BasicAttribute(entry.getKey(), value[0]));
				}
			}

			String[] objectClasses = new String[] { "top", "user", "person",
					"organizationalPerson" };
			BasicAttribute objectClassAttributeValues = new BasicAttribute(
					OBJECT_CLASS_ATTRIBUTE);
			for (String objectClass : objectClasses) {
				objectClassAttributeValues.add(objectClass);
			}
			attributes.add(objectClassAttributeValues);

			attributes.add(new BasicAttribute(COMMON_NAME_ATTRIBUTE, identity.getFullName()));
			
			String upn = identity.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE);
			
			if(StringUtils.isEmpty(upn)) {
				if(!principalName.contains("@")) {
					upn = principalName + "@"
							+ config.getDomain();
					attributes.add(new BasicAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE,
							upn));
				} else {
					attributes.add(new BasicAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE,
							principalName));
				}
			} else {
				attributes.add(new BasicAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE,
						upn));
			}
			
			String sAMAccountName = identity.getAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE);
			
			if(StringUtils.isEmpty(sAMAccountName)) {
				sAMAccountName = principalName;
				if(sAMAccountName.contains("@")) {
					sAMAccountName = sAMAccountName.substring(0, sAMAccountName.indexOf("@"));
				}
				attributes.add(new BasicAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE,
						sAMAccountName));
			}
			

			if (!StringUtil.isNullOrEmpty(identity
					.getAddress(com.identity4j.connector.Media.mobile))) {
				attributes
						.add(new BasicAttribute(
								MOBILE_PHONE_NUMBER_ATTRIBUTE,
								identity.getAddress(com.identity4j.connector.Media.mobile)));
			}

			ldapService.bind(userDn, attributes.toArray(new Attribute[0]));
			ldapService.setPassword(userDn.toString(), password);

			DirectoryIdentity directoryIdentity = (DirectoryIdentity) getIdentityByName(upn);
			setForcePasswordChangeAtNextLogon(directoryIdentity, false);
			enableIdentity(directoryIdentity);

			for(Role r : identity.getRoles()) {
				LOG.info("REMOVEME: assigning " + r.getPrincipalName());
				assignRole(usersDn, r);
			}
			
			return directoryIdentity;

		} catch (InvalidNameException e) {
			LOG.error("Problem in create identity", e);
		} catch (NamingException e) {
			LOG.error("Problem in create identity", e);
		} catch (IOException e) {
			LOG.error("Problem in create identity", e);
		}
		return identity;
	}

	@Override
	public void unlockIdentity(Identity identity) throws ConnectorException {
		if (!(identity instanceof DirectoryIdentity)) {
			throw new IllegalArgumentException(
					"May only unlock LDAP identities.");
		}
		if (identity.getAccountStatus().getType()
				.equals(AccountStatusType.expired)) {
			throw new IllegalStateException("May not unlock expired accounts.");
		}
		if (identity.getAccountStatus().getType()
				.equals(AccountStatusType.unlocked)) {
			throw new IllegalStateException("Account already unlocked.");
		}
		try {
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute(LOCKOUT_TIME_ATTRIBUTE, "0")));
			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
			identity.getAccountStatus().unlock();
		} catch (Exception e) {
			throw new ConnectorException("Lock account failure during write", e);
		}
	}

	@Override
	public void lockIdentity(Identity identity) throws ConnectorException {
		if (!(identity instanceof DirectoryIdentity)) {
			throw new IllegalArgumentException("May only lock LDAP identities.");
		}
		if (identity.getAccountStatus().getType()
				.equals(AccountStatusType.expired)) {
			throw new IllegalStateException("May not lock expired accounts.");
		}
		if (identity.getAccountStatus().getType()
				.equals(AccountStatusType.locked)) {
			throw new IllegalStateException("Account already locked.");
		}
		try {
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute(LOCKOUT_TIME_ATTRIBUTE, String
							.valueOf(ActiveDirectoryDateUtil
									.javaDataToADTime(new Date())))));
			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
			identity.getAccountStatus().lock();
		} catch (Exception e) {
			throw new ConnectorException("Lock account failure during write", e);
		}
	}

	@Override
	public void disableIdentity(Identity identity) throws ConnectorException {
		try {
			if (!(identity instanceof DirectoryIdentity)) {
				throw new IllegalArgumentException(
						"May only disable LDAP identities.");
			}
			if (identity.getAccountStatus().getType()
					.equals(AccountStatusType.disabled)) {
				throw new IllegalStateException("Account already disabled.");
			}

			DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute(
							USER_ACCOUNT_CONTROL_ATTRIBUTE,
							String.valueOf(UserAccountControl.getUserAccountControlFlag(
									Integer.valueOf(directoryIdentity
											.getAttributeOrDefault(
													USER_ACCOUNT_CONTROL_ATTRIBUTE,
													"0")), false, Collections
											.<String, Boolean> emptyMap())))));

			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
		} catch (NamingException e) {
			LOG.error("Problem in disable identity", e);
		} catch (IOException e) {
			LOG.error("Problem in disable identity", e);
		}

	}

	@Override
	public void enableIdentity(Identity identity) throws ConnectorException {
		try {
			if (!(identity instanceof DirectoryIdentity)) {
				throw new IllegalArgumentException(
						"May only disable LDAP identities.");
			}
			if (!identity.getAccountStatus().getType()
					.equals(AccountStatusType.disabled)) {
				throw new IllegalStateException("Account already enabled.");
			}

			DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			items.add(new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute(
							USER_ACCOUNT_CONTROL_ATTRIBUTE,
							String.valueOf(UserAccountControl.getUserAccountControlFlag(
									Integer.valueOf(directoryIdentity
											.getAttributeOrDefault(
													USER_ACCOUNT_CONTROL_ATTRIBUTE,
													"0")), true, Collections
											.<String, Boolean> emptyMap())))));

			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
		} catch (NamingException e) {
			LOG.error("Problem in enable identity", e);
		} catch (IOException e) {
			LOG.error("Problem in enable identity", e);
		}

	}

	protected void setForcePasswordChangeAtNextLogon(
			DirectoryIdentity identity, boolean forcePasswordChangeAtLogon) {
		try {
			Collection<ModificationItem> items = new ArrayList<ModificationItem>();
			if (forcePasswordChangeAtLogon) {
				items.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute(PWD_LAST_SET_ATTRIBUTE, String
								.valueOf(CHANGE_PASSWORD_AT_NEXT_LOGON_FLAG))));
			} else {
				items.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
						new BasicAttribute(PWD_LAST_SET_ATTRIBUTE)));
				items.add(new ModificationItem(
						DirContext.ADD_ATTRIBUTE,
						new BasicAttribute(
								PWD_LAST_SET_ATTRIBUTE,
								String.valueOf(CHANGE_PASSWORD_AT_NEXT_LOGON_CANCEL_FLAG))));
			}

			ldapService.update(((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
		} catch (NamingException e) {
			LOG.error("Problem in force password change at next logon", e);
		} catch (IOException e) {
			LOG.error("Problem in force password change at next logon", e);
		}
	}

	private int getMinimumPasswordAge() {
		return getPasswordAge(MINIMUM_PASSWORD_AGE_ATTRIBUTE);
	}

	private int getMaximumPasswordAge() {
		int maxPasswordAgeDays = ((ActiveDirectoryConfiguration) getConfiguration())
				.getMaxPasswordAgeDays();
		return maxPasswordAgeDays < 1 ? getPasswordAge(MAXIMUM_PASSWORD_AGE_ATTRIBUTE)
				: maxPasswordAgeDays;
	}

	private int getPasswordHistoryLength() {
		String value = getAttributeValue(getRootDn(), PWD_HISTORY_LENGTH);
		if (StringUtil.isNullOrEmpty(value)) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	private int getPasswordAge(String attributeName) {
		String value = getAttributeValue(getConfiguration().getBaseDn(),
				attributeName);
		if (StringUtil.isNullOrEmpty(value)) {
			return 0;
		}
		return ActiveDirectoryDateUtil.adTimeToJavaDays(Long.parseLong(value));
	}

	protected Iterator<Role> getRoles(String filter) {

		try {
			return ldapService.search(filter, new ResultMapper<Role>() {

				@Override
				public Role apply(SearchResult result) throws NamingException {
					return mapRole(result);
				}
			});
		} catch (NamingException e) {
			LOG.error("Problem in getting roles", e);
		} catch (IOException e) {
			LOG.error("Problem in getting roles", e);
		}
		return ROLE_ITERATOR;

	}

	@Override
	protected SearchControls configureSearchControls(
			SearchControls searchControls) {
		searchControls = super.configureSearchControls(searchControls);
		List<String> attr = new ArrayList<String>(identityAttributesToRetrieve);
		attr.addAll(DEFAULT_USER_ATTRIBUTES);
		searchControls.setReturningAttributes(attr.toArray(new String[0]));
		return searchControls;
	}

	@Override
	protected SearchControls configureRoleSearchControls(
			SearchControls searchControls) {
		searchControls = super.configureSearchControls(searchControls);
		List<String> attr = new ArrayList<String>(ALL_ROLE_ATTRIBUTES);
		searchControls.setReturningAttributes(attr.toArray(new String[0]));
		return searchControls;
	}

	@Override
	protected final void changePassword(Identity identity, char[] oldPassword,
			char[] password) {
		try {
			ActiveDirectoryConfiguration directoryConfiguration = (ActiveDirectoryConfiguration) getConfiguration();
			String newQuotedPassword = "\"" + new String(password) + "\"";
			String oldQuotedPassword = "\"" + new String(oldPassword) + "\"";

			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
			byte[] oldUnicodePassword = oldQuotedPassword.getBytes("UTF-16LE");

			ModificationItem item1 = new ModificationItem(
					DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute(directoryConfiguration
							.getIdentityPasswordAttribute(), oldUnicodePassword));
			ModificationItem item2 = new ModificationItem(
					DirContext.ADD_ATTRIBUTE,
					new BasicAttribute(directoryConfiguration
							.getIdentityPasswordAttribute(), newUnicodePassword));
			ldapService.update(((DirectoryIdentity) identity).getDn(),
					new ModificationItem[] { item1, item2 });

		} catch (NamingException nme) {
			try {
				throw new ConnectorException(
						"Failed to set password. Reason code "
								+ processNamingException(nme)
								+ ". Please see the logs for more detail.");
			} catch (PasswordChangeRequiredException pcre) {
				LOG.warn("Could not use change password because 'Change Password At Next Login' was set. Falling back to setPassword. Depending on the version of Active Directory in use, this may bypass password history checks.");
				setPassword(identity, password, false);
			}
		} catch (IOException e) {
			LOG.error("Problem in change password for identity", e);
		}
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
				message += " Your password policy also has a minimum password age of "
						+ minPasswordAge
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
					"The new password does not comply with the rules enforced by Active Directory. It is also likely you very recently made another password change.");
		} else if (reason.equals("00000524")) {
			throw new ConnectorException(
					"Attempt to create account with username that already exists.");
		} else if (reason.equals("0000001F")) {
			throw new ConnectorException(
					"Could not perform the requested operation. Please configure the server to connect to your Active Directory securely over SSL. http://bit.ly/16wQTMi");
		} else if (reason.equals("80090308") && "773".equals(dep.getData())) {
			throw new PasswordChangeRequiredException(
					"Cannot change password when changePasswordPasswordAtNextLogin is set, must use setPassword");
		}
		LOG.error(nme.getMessage() + ". Reason code give was " + reason, nme);
		return reason;

	}

	protected void checkNamingException(String errorText, NamingException nme)
			throws ConnectorException {
		processNamingException(nme);
		DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
		String reason = dep.getReason();
		LOG.error(errorText + ". Reason code give was " + reason, nme);
		throw new ConnectorException(
				"Failed to perform operation. Reason code " + reason
						+ ". Please see the logs for more detail.");
	}

	protected void setPassword(Identity identity, char[] password,
			boolean forcePasswordChangeAtLogon) throws ConnectorException {
		try {
			String newQuotedPassword = "\"" + new String(password) + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

			ldapService.setPassword(((DirectoryIdentity) identity).getDn()
					.toString(), newUnicodePassword);
		} catch (NamingException e) {
			LOG.error("Problem in set password for identity", e);
		} catch (IOException e) {
			LOG.error("Problem in set password for identity", e);
		}

	}

	@Override
	protected Iterator<Identity> getIdentities(String filter) {

		final ActiveDirectoryConfiguration config = (ActiveDirectoryConfiguration) getConfiguration();
		final Map<String, ActiveDirectoryGroup> groups = new HashMap<String, ActiveDirectoryGroup>();
		final Map<Long, ActiveDirectoryGroup> groupsByRID = new HashMap<Long, ActiveDirectoryGroup>();

		if (config.isEnableRoles()) {
			Iterator<Role> it = getRoles();
			while (it.hasNext()) {
				ActiveDirectoryGroup group = (ActiveDirectoryGroup) it.next();
				String dn = group.getDn().toString();
				// https://jira.springsource.org/browse/LDAP-109
				dn = dn.replace("\\\\", "\\\\\\");
				dn = dn.replace("/", "\\/");

				groups.put(dn.toLowerCase(), group);
				groupsByRID.put(group.getRid(), group);
			}
		}

		final int minimumPasswordAge = getMinimumPasswordAge();
		final int maximumPasswordAge = getMaximumPasswordAge();
		final long lockoutDuration = getBaseLongAttribute(LOCKOUT_DURATION_ATTRIBUTE);

		try {
			return ldapService.search(filter, new ResultMapper<Identity>() {

				private boolean isAttributeMapped(Attribute attribute) {
//					return DEFAULT_USER_ATTRIBUTES.contains(attribute.getID())
//							|| identityAttributesToRetrieve.contains(attribute
//									.getID());
					return true;
				}

				private String[] getElements(Attribute attribute)
						throws NamingException {
					Collection<String> values = new ArrayList<String>();
					for (int index = 0; index < attribute.size(); index++) {
						// TODO how to decide how non-string attributes are
						// converted
						final Object object = attribute.get(index);
						if (object instanceof byte[]) {
							values.add(StringUtil
									.convertByteToString((byte[]) object));
						} else if (object instanceof String
								|| object instanceof Number
								|| object instanceof Boolean) {
							values.add(object.toString());
						} else {
							LOG.warn("Unknown attribute class, assuming String.");
							values.add(object.toString());
						}
					}
					return values.toArray(new String[values.size()]);
				}

				@Override
				public Identity apply(SearchResult result)
						throws NamingException {
					Attributes attributes = result.getAttributes();

					String guid = UUID.nameUUIDFromBytes(
							(byte[]) getAttribute(attributes
									.get(OBJECT_GUID_ATTRIBUTE))).toString();
					Name udn = new LdapName(result.getNameInNamespace());
					String domain = getDomain(udn);
					String username = selectUsername(result);
					DirectoryIdentity directoryIdentity = new DirectoryIdentity(
							guid, username, udn);

					for (NamingEnumeration<? extends Attribute> attributeEmun = result
							.getAttributes().getAll(); attributeEmun
							.hasMoreElements();) {
						Attribute attribute = attributeEmun.nextElement();
						if (isAttributeMapped(attribute)) {
							directoryIdentity.setAttribute(attribute.getID(),
									getElements(attribute));
						}
					}

					String userPrincipalName = StringUtil
							.nonNull((String) getAttribute(attributes
									.get(USER_PRINCIPAL_NAME_ATTRIBUTE)));

					// If service account, mark as 'system'
					String serviceAccountDn = config.getServiceAccountDn();
					if (userPrincipalName.equals(serviceAccountDn)) {
						directoryIdentity.setSystem(true);
					}

					String otherName = getOtherName(username,
							userPrincipalName, domain, config);
					directoryIdentity.setAddress(Media.email,
							(String) getAttribute(attributes
									.get(MAIL_ATTRIBUTE)));
					directoryIdentity
							.setFullName((String) getAttribute(attributes
									.get(COMMON_NAME_ATTRIBUTE)));
					directoryIdentity.setOtherName(otherName);

					String phoneNumber = (String) getAttribute(attributes
							.get(MOBILE_PHONE_NUMBER_ATTRIBUTE));
					if (!StringUtil.isNullOrEmpty(phoneNumber)) {
						directoryIdentity.setAddress(Media.mobile, phoneNumber);
					}

					LdapName ou = new LdapName((String) getAttribute(attributes
							.get(DISTINGUISHED_NAME_ATTRIBUTE)));
					ou.remove(ou.size() - 1);
					directoryIdentity.setAttribute(OU_ATTRIBUTE, ou.toString());

					// Last sign on
					String lastLogonTimestamp = (String) getAttribute(attributes
							.get(LAST_LOGON_TIMESTAMP_ATTRIBUTE));
					if (!StringUtil.isNullOrEmpty(lastLogonTimestamp)) {
						long lastLogonTime = Long.parseLong(lastLogonTimestamp);
						if (lastLogonTime > 0) {
							directoryIdentity
									.setLastSignOnDate(ActiveDirectoryDateUtil
											.adTimeToJavaDate(lastLogonTime));
						}
					}
					String lastLogon = (String) getAttribute(attributes
							.get(LAST_LOGON_ATTRIBUTE));

					if (directoryIdentity.getLastSignOnDate() == null
							&& !StringUtil.isNullOrEmpty(lastLogon)) {
						long lastLogonTime = Long.parseLong(lastLogon);
						if (lastLogonTime > 0) {
							directoryIdentity
									.setLastSignOnDate(ActiveDirectoryDateUtil
											.adTimeToJavaDate(lastLogonTime));
						}
					}

					// Calculate the password status
					PasswordStatus passwordStatus = directoryIdentity
							.getPasswordStatus();
					Date passwordLastSet = trimDate(getDateAttribute(result,
							PWD_LAST_SET_ATTRIBUTE));
					passwordStatus.setLastChange(passwordLastSet);
					boolean passwordChangeAllowed = isPasswordChangeAllowed(result);
					if (passwordChangeAllowed) {
						passwordStatus.setUnlocked(getAgedDate(
								minimumPasswordAge, passwordLastSet));
					}
					if (!isPasswordNeverExpire(result)
							&& passwordLastSet != null) {
						passwordStatus.setExpire(getAgedDate(
								maximumPasswordAge, passwordLastSet));
					}

					String userDn = ActiveDirectoryConfiguration.buildUsername(
							config.getBaseDn().toString(), config.getDomain(),
							directoryIdentity.getPrincipalName());
					if (userDn.equalsIgnoreCase(getConfiguration()
							.getServiceAccountDn())) {
						// Do not allow the service account password to be reset
						passwordStatus
								.setType(PasswordStatusType.noChangeAllowed);
					} else if (isPasswordChangeRequired(result)) {
						passwordStatus
								.setType(PasswordStatusType.changeRequired);
					} else {
						passwordStatus.calculateType();
					}
					String userAccountControl = (String) getAttribute(attributes
							.get(USER_ACCOUNT_CONTROL_ATTRIBUTE));

					// Overrides calculated password status, prevent the user
					// changing the password at all
					if (passwordStatus.getType().equals(
							PasswordStatusType.expired)) {
						if (userAccountControl.length() != 0) {
							if (UserAccountControl.isValueSet(
									Integer.valueOf(userAccountControl),
									UserAccountControl.DONT_EXPIRE_PASSWORD_FLAG)) {
								passwordStatus
										.setType(PasswordStatusType.upToDate);
							}
						}
					}
					if (!passwordChangeAllowed) {
						passwordStatus
								.setType(PasswordStatusType.noChangeAllowed);
					}

					// Calculate password status
					AccountStatus accountStatus = directoryIdentity
							.getAccountStatus();
					accountStatus.setExpire(trimDate(getDateAttribute(result,
							ACCOUNT_EXPIRES_ATTRIBUTE)));
					accountStatus.setLocked(trimDate(getDateAttribute(result,
							LOCKOUT_TIME_ATTRIBUTE)));
					accountStatus.setUnlocked(null);
					if (userAccountControl.length() != 0) {
						if (UserAccountControl.isValueSet(
								Integer.valueOf(userAccountControl),
								UserAccountControl.ACCOUNTDISABLE_FLAG)) {
							accountStatus.setDisabled(true);
						}
					}
					accountStatus.calculateType();

					// Now if looked, calculate when unlocked
					if (accountStatus.getType()
							.equals(AccountStatusType.locked)) {
						accountStatus.setUnlocked(trimDate(new Date(
								accountStatus.getLocked().getTime()
										- (lockoutDuration / 1000))));
					}

					if (config.isEnableRoles()) {
						boolean memberOfSupported = true;

						try {
							Long rid = Long
									.parseLong((String) getAttribute(attributes
											.get(PRIMARY_GROUP_ID_ATTRIBUTE)));
							ActiveDirectoryGroup primaryGroup = groupsByRID
									.get(rid);
							if (primaryGroup != null) {
								directoryIdentity.addRole(primaryGroup);
							}
						} catch (NumberFormatException e) {
						}

						Iterator<String> groupDnsItr;
						try {
							groupDnsItr = memberOfSupported ? getUsersGroups(result)
									: getGroupsForUser(result);

							while (groupDnsItr.hasNext()) {
								String dn = groupDnsItr.next();

								// https://jira.springsource.org/browse/LDAP-109
								dn = dn.replace("\\\\", "\\\\\\");
								dn = dn.replace("/", "\\/");

								if (groups.containsKey(dn.toLowerCase())) {
									directoryIdentity.addRole(groups.get(dn
											.toLowerCase()));
								} else {
									Attributes roleAttributes;
									try {
										roleAttributes = ldapService
												.lookupContext(new LdapName(dn));
										ActiveDirectoryGroup activeDirectoryGroup = mapRole(
												dn, roleAttributes);
										if (activeDirectoryGroup != null) {
											groups.put(dn.toLowerCase(),
													activeDirectoryGroup);
											groupsByRID.put(
													activeDirectoryGroup
															.getRid(),
													activeDirectoryGroup);
											directoryIdentity
													.addRole(activeDirectoryGroup);
										}

									} catch (IOException e) {
										LOG.error("Problem in getting role", e);
									}
								}
							}
						} catch (IOException e) {
							LOG.error("Problem in getting roles", e);
						}

					} else {
						directoryIdentity.setRoles(new Role[0]);
					}
					return directoryIdentity;
				}
			});
		} catch (NamingException e) {
			LOG.error("Problem in fetching identity", e);
		} catch (IOException e) {
			LOG.error("Problem in fetching identity", e);
		}
		return null;

	}

	private boolean isPasswordChangeRequired(SearchResult result)
			throws NamingException {
		try {
			Attributes attributes = result.getAttributes();
			String value = (String) getAttribute(attributes
					.get(PWD_LAST_SET_ATTRIBUTE));
			long val = Long.parseLong(value);
			if (val == 0) {
				return true;
			}
		} catch (NumberFormatException nfe) {
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
			}
			cal.add(Calendar.DAY_OF_YEAR, age);
			return trimDate(cal.getTime());
		}
	}

	private String getOtherName(String username, String userPrincipalName,
			String domain, ActiveDirectoryConfiguration config) {
		String otherName;
		if (userPrincipalName.equals("")
				|| (!domain.equalsIgnoreCase(config.getDomain()) && userPrincipalName
						.indexOf('@') == -1)) {
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
				domain = el.substring(3)
						+ (domain.equals("") ? "" : "." + domain);
			}
		}
		return domain;
	}

	private String selectUsername(SearchResult searchResult)
			throws NamingException {

		ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

		Attributes attributes = searchResult.getAttributes();

		String domain = getDomain(new LdapName(
				searchResult.getNameInNamespace()));
		boolean isPrimaryDomain = domain.equalsIgnoreCase(config.getDomain());
		String samAccountName = StringUtil
				.nonNull((String) getAttribute(attributes
						.get(SAM_ACCOUNT_NAME_ATTRIBUTE)));
		String userPrincipalName = StringUtil
				.nonNull((String) getAttribute(attributes
						.get(USER_PRINCIPAL_NAME_ATTRIBUTE)));
		String username = StringUtil.getBeforeLast(userPrincipalName, "@");
		// need to strip username first and check if this is blank
		// if accounts are migrated from NT4 it's entirely possible
		// the UPN is @something.com instead of me@something.com
		if (username.equals(userPrincipalName)) {
			// there is no username in UPN, WE HAVE to use the samAccountName
			if (isPrimaryDomain) {
				return samAccountName;
			} else {
				return samAccountName + "@" + domain;
			}
		}

		return config.isUsernameSamAccountName() && isPrimaryDomain ? samAccountName
				: fixUserPrincipalName(userPrincipalName, domain);
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

		if (StringUtil.getAfterLast(userPrincipalName, "@").equalsIgnoreCase(
				domain)) {
			return StringUtil.getBefore(userPrincipalName, "@");
		} else {
			return userPrincipalName;
		}
	}

	private long getBaseLongAttribute(String attributeName) {
		try {
			String value = getAttributeValue(getConfiguration().getBaseDn(),
					attributeName);
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
			return null;
			// cal.setTimeInMillis(0);
			// cal.set(Calendar.YEAR, 3999);
		}
		return cal.getTime();
	}

	private Date getDateAttribute(SearchResult result, String dateAttribute)
			throws NamingException {

		try {
			Attributes attributes = result.getAttributes();

			String value = StringUtil.nonNull((String) getAttribute(attributes
					.get(dateAttribute)));

			long val = Long.parseLong(value);
			if (val > 0) {
				return ActiveDirectoryDateUtil.adTimeToJavaDate(val);
			}
		} catch (NumberFormatException nfe) {
		}
		return null;
	}

	/**
	 * The userAccountControl flag is used to check this. If they don't have one
	 * or it's not an Integer (neither of which should actually be possible),
	 * then we simply assume it's not allowed.
	 * 
	 * @param attributes
	 * @return true if the last password change date should be required
	 * @throws NamingException
	 */
	private boolean isPasswordChangeAllowed(SearchResult result)
			throws NamingException {

		Attributes attributes = result.getAttributes();

		String attributeValue = StringUtil
				.nonNull((String) getAttribute(attributes
						.get(USER_ACCOUNT_CONTROL_ATTRIBUTE)));

		if (attributeValue.length() == 0) {
			return false;
		}

		try {
			int userAccountControl = Integer.valueOf(attributeValue);
			return UserAccountControl
					.isPasswordChangePermitted(userAccountControl);
		} catch (NumberFormatException nfe) {
			// LOG.error("User account control setting invalid = '" +
			// attributeValue + "', assuming password change supported");
			return true;
		}
	}

	private boolean isPasswordNeverExpire(SearchResult result)
			throws NamingException {
		Attributes attributes = result.getAttributes();

		String attributeValue = StringUtil
				.nonNull((String) getAttribute(attributes
						.get(USER_ACCOUNT_CONTROL_ATTRIBUTE)));
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
	protected String buildIdentityFilter(String identityName) {
		ActiveDirectoryConfiguration activeDirectoryConfiguration = getActiveDirectoryConfiguration();

		String identityNameWithDomainSearchFilter = "";
		String userPrincipalName = identityName;
		if (!activeDirectoryConfiguration.isUsernameSamAccountName()
				&& !userPrincipalName.equals(WILDCARD_SEARCH)) {
			int idx = userPrincipalName.indexOf('@');
			if (idx == -1) {
				userPrincipalName += "@"
						+ activeDirectoryConfiguration.getDomain();
				identityNameWithDomainSearchFilter = String.format("(%s=%s)",
						USER_PRINCIPAL_NAME_ATTRIBUTE, userPrincipalName);
			}
		}

		String filter = String.format("(&(!(%s=computer))(%s=user)(|"
				+ "(%s=%s)" + "(%s=%s)" + "%s))", OBJECT_CLASS_ATTRIBUTE,
				OBJECT_CLASS_ATTRIBUTE, SAM_ACCOUNT_NAME_ATTRIBUTE,
				identityName, USER_PRINCIPAL_NAME_ATTRIBUTE, identityName,
				identityNameWithDomainSearchFilter);

		return filter;
	}

	private Iterator<String> getUsersGroups(SearchResult result)
			throws NamingException {
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

	private Iterator<String> getGroupsForUser(SearchResult result)
			throws NamingException, IOException {
		String filter = ldapService.buildObjectClassFilter("group", "member",
				result.getNameInNamespace());

		return ldapService.search(filter, new ResultMapper<String>() {

			@Override
			public String apply(SearchResult result) throws NamingException {
				return result.getNameInNamespace();
			}
		});

	}

	@Override
	protected void assertPasswordChangeIsAllowed(Identity identity,
			char[] oldPassword, char[] password) throws ConnectorException {
		Date lastPasswordChange = identity.getPasswordStatus().getLastChange();
		if (lastPasswordChange != null
				&& !Util.isDatePast(lastPasswordChange, getMinimumPasswordAge())) {
			// TODO throw exception
			// throw new PasswordChangeTooSoonException(calendar.getTime());
		}
	}

	@Override
	protected ActiveDirectoryGroup mapRole(SearchResult result)
			throws NamingException {

		Attributes attributes = result.getAttributes();
		return mapRole(result.getNameInNamespace(), attributes);
	}

	protected ActiveDirectoryGroup mapRole(String dn, Attributes attributes)
			throws NamingException, InvalidNameException {
		String commonName = StringUtil.nonNull((String) getAttribute(attributes
				.get(COMMON_NAME_ATTRIBUTE)));
		if (commonName.length() != 0) {
			String guid = UUID
					.nameUUIDFromBytes(
							(byte[]) getAttribute(attributes
									.get(OBJECT_GUID_ATTRIBUTE))).toString();
			byte[] sid = (byte[]) getAttribute(attributes
					.get(OBJECT_GUID_ATTRIBUTE));
			return new ActiveDirectoryGroup(guid, commonName, new LdapName(dn),
					sid);
		}
		return null;
	}

	private ActiveDirectoryConfiguration getActiveDirectoryConfiguration() {
		return (ActiveDirectoryConfiguration) getConfiguration();
	}

	protected Name getRootDn() {
		final Name baseDn = getActiveDirectoryConfiguration().getBaseDn();
		for (int i = 0; i < baseDn.size(); i++) {
			if (!baseDn.get(i).toLowerCase().startsWith("dc=")) {
				final Name suffix = baseDn.getPrefix(i);
				return suffix;
			}
		}
		return (Name) baseDn.clone();
	}

	private Object getAttribute(Attribute attribute) throws NamingException {
		return attribute != null ? attribute.get() : null;
	}

}