/* HEADER */
package com.identity4j.connector.activedirectory;

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

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;
import org.springframework.ldap.core.simple.SimpleLdapOperations;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.NotFilter;
import org.springframework.ldap.filter.OrFilter;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.Media;
import com.identity4j.connector.directory.DirectoryConnector;
import com.identity4j.connector.directory.DirectoryExceptionParser;
import com.identity4j.connector.directory.DirectoryIdentity;
import com.identity4j.connector.directory.PrincipalContextMapper;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PasswordPolicyViolationException;
import com.identity4j.connector.principal.AccountStatus;
import com.identity4j.connector.principal.AccountStatusType;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.PasswordStatus;
import com.identity4j.connector.principal.PasswordStatusType;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.StringUtil;
import com.identity4j.util.Util;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;
import com.identity4j.util.passwords.PasswordCharacteristics;

public class ActiveDirectoryConnector extends DirectoryConnector {
	final static Log LOG = LogFactory.getLog(ActiveDirectoryConnector.class);
	private static final String SAM_ACCOUNT_NAME_ATTRIBUTE = "sAMAccountName";
	private static final String USER_PRINCIPAL_NAME_ATTRIBUTE = "userPrincipalName";
	private static final String OBJECT_GUID_ATTRIBUTE = "objectGUID";
	private static final String USER_ACCOUNT_CONTROL_ATTRIBUTE = "userAccountControl";
	private static final String DISTINGUISHED_NAME_ATTRIBUTE = "distinguishedName";
	private static final String ACCOUNT_EXPIRES_ATTRIBUTE = "accountExpires";
	private static final String LOCKOUT_TIME_ATTRIBUTE = "lockoutTime";
	private static final String LOCKOUT_DURATION_ATTRIBUTE = "lockoutDuration";
	private static final String LAST_LOGON_ATTRIBUTE = "lastLogon";
	private static final String LAST_LOGON_TIMESTAMP_ATTRIBUTE = "lastLogontimeStamp";
	private static final String PRIMARY_GROUP_ID_ATTRIBUTE = "primaryGroupId";
	private static final String PWD_LAST_SET_ATTRIBUTE = "pwdLastSet";
	static final String PWD_HISTORY_LENGTH = "pwdHistoryLength";
	static final String MINIMUM_PASSWORD_AGE_ATTRIBUTE = "minPwdAge";
	static final String MAXIMUM_PASSWORD_AGE_ATTRIBUTE = "maxPwdAge";
	private static final String COMMON_NAME_ATTRIBUTE = "cn";
	private static final String MEMBER_OF_ATTRIBUTE = "memberOf";
	private static final String MEMBER_ATTRIBUTE = "member";
	private static final String MAIL_ATTRIBUTE = "mail";
	private static final String MOBILE_PHONE_NUMBER_ATTRIBUTE = "mobile";
	private static final String PHONE_NUMBER_ATTRIBUTE = "telephoneNumber";
	private static final String OTHER_PHONE_NUMBER_ATTRIBUTE = "otherTelephone";
	private static final String OBJECT_SID_ATTRIBUTE = "objectSID";
	private static final String PWD_PROPERTIES_ATTRIBUTE = "pwdProperties";
	private static final String OU_ATTRIBUTE = "ou";

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		if(!capabilities.contains(ConnectorCapability.hasPasswordPolicy)) {
			capabilities.add(ConnectorCapability.hasPasswordPolicy);
			capabilities.add(ConnectorCapability.caseInsensitivePrincipalNames);
		}
		return capabilities;
	}
	
	/**
	 * These are the attributes we need for operation, but are not stored as
	 * actual attributes in our local database
	 */
	private static Collection<String> DEFAULT_USER_ATTRIBUTES = Arrays
			.asList(new String[] { MEMBER_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE,
					OU_ATTRIBUTE, DISTINGUISHED_NAME_ATTRIBUTE, PRIMARY_GROUP_ID_ATTRIBUTE });
	/**
	 * These are attributes we need for operation and want to store as
	 * attributes as well.
	 */
	private static Collection<String> ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE = Arrays
			.asList(new String[] { USER_PRINCIPAL_NAME_ATTRIBUTE,
					SAM_ACCOUNT_NAME_ATTRIBUTE, USER_ACCOUNT_CONTROL_ATTRIBUTE,
					LAST_LOGON_ATTRIBUTE, LAST_LOGON_TIMESTAMP_ATTRIBUTE,
					PWD_LAST_SET_ATTRIBUTE, OU_ATTRIBUTE });

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

	private static final int CHANGE_PASSWORD_AT_NEXT_LOGON_FLAG = 0;
	private static final int CHANGE_PASSWORD_AT_NEXT_LOGON_CANCEL_FLAG = -1;

	// Bit mask values for pwdProperties
	private static final int DOMAIN_PASSWORD_COMPLEX = 0x01;

	private static Map<String, String> childDomainControllers = new HashMap<String, String>();

	private List<String> identityAttributesToRetrieve = new ArrayList<String>(
			ALL_USER_ATTRIBUTES);

	// Controls for Win2008 R2 password history on admin reset
	final byte[] controlData = {48,(byte)132,0,0,0,3,2,1,1};
	BasicControl[] controls = new BasicControl[1];
	final String LDAP_SERVER_POLICY_HINTS_OID = "1.2.840.113556.1.4.2066";
	
	// TODO not yet used
	// private static final int DOMAIN_PASSWORD_NO_ANON_CHANGE = 0x02;
	// private static final int DOMAIN_PASSWORD_NO_CLEAR_CHANGE = 0x04;
	// private static final int DOMAIN_LOCKOUT_ADMINS = 0x08;
	// private static final int DOMAIN_PASSWORD_STORE_CLEARTEXT = 0x10;
	// private static final int DOMAIN_REFUSE_PASSWORD_CHANGE = 0x20;

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
			SimpleLdapOperations ldapTemplate = getLdapTemplate();
			Identity identity = getIdentityByName(principalName);
			String identityOU = identity
					.getAttribute(DISTINGUISHED_NAME_ATTRIBUTE);
			DistinguishedName usersDn = new DistinguishedName(identityOU);
			ldapTemplate.unbind(usersDn);
		} catch (org.springframework.ldap.NamingException nme) {
			checkNamingException("Failed to update account.", nme);
		} catch (ConnectorException ce) {
			throw ce;
		} catch (Exception e) {
			throw new ConnectorException("Failed to create account.", e);
		}
	}

	@Override
	public void updateIdentity(final Identity identity)
			throws ConnectorException {
		try {
			SimpleLdapOperations ldapTemplate = getLdapTemplate();

			Identity oldIdentity = getIdentityByName(identity
					.getPrincipalName());

			final ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

			// OU may have been provided
			String identityOU = identity
					.getAttribute(DISTINGUISHED_NAME_ATTRIBUTE);
			DistinguishedName usersDn = new DistinguishedName(identityOU);
			String principalName = identity.getPrincipalName();
			DirContextOperations context = ldapTemplate.lookupContext(usersDn);

			// First check for attributes that are to be unset
			for (Map.Entry<String, String[]> entry : oldIdentity
					.getAttributes().entrySet()) {
				// Do not remove attributes that are required for operation,
				if (!ALL_USER_ATTRIBUTES.contains(entry.getKey())
						&& !identity.getAttributes()
								.containsKey(entry.getKey())) {
					// Util.removeMe("REMOVE" + entry.getKey());
					context.setAttributeValues(entry.getKey(), new Object[0]);
				}
			}

			for (Map.Entry<String, String[]> entry : identity.getAttributes()
					.entrySet()) {
				if (!isExcludeForUpdate(entry.getKey())) {
					if (!oldIdentity.getAttributes()
							.containsKey(entry.getKey())) {
						// New
						if (entry.getValue().length > 0
								&& entry.getValue()[0].length() > 0) {
							// Util.removeMe("NEW " + entry.getKey()
							// + " = New val: "
							// + identity.getAttribute(entry.getKey()));
							context.addAttributeValue(entry.getKey(),
									identity.getAttribute(entry.getKey()));
						}
					} else {
						String oldValue = oldIdentity.getAttribute(entry
								.getKey());
						String newValue = identity.getAttribute(entry.getKey());
						if (Util.differs(oldValue, newValue)) {
							// Util.removeMe(entry.getKey() + " = Old val: "
							// + oldValue + " New val: " + newValue);
							context.setAttributeValue(entry.getKey(), newValue);
						}
					}
				}
			}

			if (Util.differs(oldIdentity.getFullName(), identity.getFullName())) {
				context.setAttributeValue(COMMON_NAME_ATTRIBUTE,
						identity.getFullName());
			}
			String principalNameWithDomain = principalName + "@"
					+ config.getDomain();
			if (Util.differs(
					oldIdentity.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE),
					principalNameWithDomain)) {
				context.setAttributeValue(USER_PRINCIPAL_NAME_ATTRIBUTE,
						principalNameWithDomain);
			}

			addContactAttribute(identity, context,
					com.identity4j.connector.Media.mobile,
					MOBILE_PHONE_NUMBER_ATTRIBUTE);

			ldapTemplate.modifyAttributes(context);

		} catch (org.springframework.ldap.NamingException nme) {
			nme.printStackTrace();
			checkNamingException("Failed to update account.", nme);
		} catch (ConnectorException ce) {
			throw ce;
		} catch (Exception e) {
			throw new ConnectorException("Failed to create account.", e);
		}
	}

	private boolean isExcludeForUpdate(String attributeName) {
		return ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE.contains(attributeName);
	}

	@Override
	public Identity createIdentity(final Identity identity, char[] password)
			throws ConnectorException {
		try {
			SimpleLdapOperations ldapTemplate = getLdapTemplate();

			final ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

			// OU may have been provided
			String identityOU = identity.getAttribute(OU_ATTRIBUTE);
			DistinguishedName usersDn;
			if (StringUtil.isNullOrEmpty(identityOU)) {
				DistinguishedName dname = new DistinguishedName(getRootDn());
				usersDn = new DistinguishedName(dname);
				usersDn.add("CN", "Users");
			} else {
				usersDn = new DistinguishedName(identityOU);
			}

			DistinguishedName userDn = new DistinguishedName(usersDn);
			String principalName = identity.getPrincipalName();
			userDn.add("CN", identity.getFullName());
			DirContextAdapter context = new DirContextAdapter(userDn);

			Name baseDn = getConfiguration().getBaseDn();
			if (!userDn.toString().endsWith(baseDn.toString())) {
				throw new ConnectorException("The User DN (" + userDn
						+ ") must be a child of the Base DN (" + baseDn
						+ " configured for the Active Directory connector.");
			}

			// First copy in the generic attributes
			for (Map.Entry<String, String[]> entry : identity.getAttributes()
					.entrySet()) {
				String[] value = entry.getValue();
				// TODO not entirely sure about this
				if (value.length > 0) {
					context.setAttributeValue(entry.getKey(), value[0]);
				}
			}

			/*
			 * Set up the attributes for the primary details. Some of these may
			 * already have been in the generic attributes
			 */

			context.setAttributeValues(OBJECT_CLASS_ATTRIBUTE, new String[] {
					"top", "user", "person", "organizationalPerson" });
			context.setAttributeValue(COMMON_NAME_ATTRIBUTE,
					identity.getFullName());
			String principalNameWithDomain = principalName + "@"
					+ config.getDomain();
			context.setAttributeValue(USER_PRINCIPAL_NAME_ATTRIBUTE,
					principalNameWithDomain);

			addContactAttribute(identity, context,
					com.identity4j.connector.Media.mobile,
					MOBILE_PHONE_NUMBER_ATTRIBUTE);

			ldapTemplate.bind(userDn, context, null);

			// Now set the password
			DirectoryIdentity directoryIdentity = (DirectoryIdentity) getIdentityByName(principalNameWithDomain);

			byte[] encodedPassword = DefaultEncoderManager.getInstance()
					.encode(password, config.getIdentityPasswordEncoding(),
							"UTF-8", null, null);
			Attribute attribute = new BasicAttribute(
					config.getIdentityPasswordAttribute(), encodedPassword);
			ModificationItem item = new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, attribute);
			LdapOperations ldapOperations = ldapTemplate.getLdapOperations();
			ldapOperations.modifyAttributes(directoryIdentity.getDn(),
					new ModificationItem[] { item });
			setForcePasswordChangeAtNextLogon(directoryIdentity, false);
			enableIdentity(directoryIdentity);

			return directoryIdentity;
		} catch (org.springframework.ldap.NamingException nme) {
			checkNamingException("Failed to create account.", nme);
			return null;
		} catch (ConnectorException ce) {
			throw ce;
		} catch (Exception e) {
			throw new ConnectorException("Failed to create account.", e);
		}
	}

	protected void addContactAttribute(Identity identity,
			final Attributes attrs, Media type, String attrName) {
		String contactDetail = identity.getAddress(type);
		if (!StringUtil.isNullOrEmpty(contactDetail)) {
			attrs.put(attrName, contactDetail);
		} else {
			attrs.remove(attrName);
		}
	}

	protected void addContactAttribute(Identity identity,
			final DirContextOperations attrs, Media type, String attrName) {
		String contactDetail = identity.getAddress(type);
		if (!StringUtil.isNullOrEmpty(contactDetail)) {
			// Util.removeMe("Setting contact details ");
			attrs.setAttributeValue(attrName, contactDetail);
		} else {
			// Util.removeMe("Removing contacyt details ");
			// attrs.setAttributeValues(attrName, new Object[0]);
		}
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
			LdapOperations ldapOperations = getLdapTemplate()
					.getLdapOperations();
			ldapOperations.modifyAttributes(
					((DirectoryIdentity) identity).getDn(),
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
			LdapOperations ldapOperations = getLdapTemplate()
					.getLdapOperations();
			ldapOperations.modifyAttributes(
					((DirectoryIdentity) identity).getDn(),
					items.toArray(new ModificationItem[items.size()]));
			identity.getAccountStatus().lock();
		} catch (Exception e) {
			throw new ConnectorException("Lock account failure during write", e);
		}
	}

	@Override
	public void disableIdentity(Identity identity) throws ConnectorException {
		if (!(identity instanceof DirectoryIdentity)) {
			throw new IllegalArgumentException(
					"May only disable LDAP identities.");
		}
		if (identity.getAccountStatus().getType()
				.equals(AccountStatusType.disabled)) {
			throw new IllegalStateException("Account already disabled.");
		}
		try {
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
			LdapOperations ldapOperations = getLdapTemplate()
					.getLdapOperations();
			ldapOperations.modifyAttributes(directoryIdentity.getDn(),
					items.toArray(new ModificationItem[items.size()]));
		} catch (org.springframework.ldap.NamingException nme) {
			checkNamingException("Failed to disable account.", nme);
		} catch (Exception e) {
			throw new ConnectorException("Lock account failure during write", e);
		}
	}

	@Override
	public void enableIdentity(Identity identity) throws ConnectorException {
		if (!(identity instanceof DirectoryIdentity)) {
			throw new IllegalArgumentException(
					"May only disable LDAP identities.");
		}
		if (!identity.getAccountStatus().getType()
				.equals(AccountStatusType.disabled)) {
			throw new IllegalStateException("Account already enabled.");
		}
		try {
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
			LdapOperations ldapOperations = getLdapTemplate()
					.getLdapOperations();
			ldapOperations.modifyAttributes(directoryIdentity.getDn(),
					items.toArray(new ModificationItem[items.size()]));
		} catch (org.springframework.ldap.NamingException nme) {
			checkNamingException("Failed to enable account.", nme);
		} catch (Exception e) {
			throw new ConnectorException("Lock account failure during write", e);
		}
	}

	@Override
	protected void setForcePasswordChangeAtNextLogon(
			DirectoryIdentity identity, boolean forcePasswordChangeAtLogon) {
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
					new BasicAttribute(PWD_LAST_SET_ATTRIBUTE, String
							.valueOf(CHANGE_PASSWORD_AT_NEXT_LOGON_CANCEL_FLAG))));
		}

		SimpleLdapOperations ldapOperations = buildLdapTemplateForDomain(
				getActiveDirectoryConfiguration().getServiceAccountDn(),
				getActiveDirectoryConfiguration().getServiceAccountPassword(),
				getDomain(identity.getDn()));
		ldapOperations.getLdapOperations().modifyAttributes(identity.getDn(),
				items.toArray(new ModificationItem[items.size()]));
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
		return searchForResults(filter, new PrincipalContextMapper<Role>() {

			protected boolean isAttributeMapped(Attribute attribute) {
				return ALL_ROLE_ATTRIBUTES.contains(attribute.getID());
			}

			@Override
			protected Role mapFromContext(DirContextOperations result) {
				return mapRole(result);
			}
		}, configureRoleSearchControls(new SearchControls()));
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
			byte[] encodedPassword1 = DefaultEncoderManager.getInstance()
					.encode(oldPassword,
							directoryConfiguration
									.getIdentityPasswordEncoding(), "UTF-8",
							null, null);
			byte[] encodedPassword2 = DefaultEncoderManager.getInstance()
					.encode(password,
							directoryConfiguration
									.getIdentityPasswordEncoding(), "UTF-8",
							null, null);
			ModificationItem item1 = new ModificationItem(
					DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute(directoryConfiguration
							.getIdentityPasswordAttribute(), encodedPassword1));
			ModificationItem item2 = new ModificationItem(
					DirContext.ADD_ATTRIBUTE,
					new BasicAttribute(directoryConfiguration
							.getIdentityPasswordAttribute(), encodedPassword2));
			LdapOperations ldapOperations = getLdapTemplate(
					(DirectoryIdentity) identity, oldPassword)
					.getLdapOperations();
			ldapOperations.modifyAttributes(
					((DirectoryIdentity) identity).getDn(),
					new ModificationItem[] { item1, item2 });
		} catch (org.springframework.ldap.NamingException nme) {
			try {
				throw new ConnectorException("Failed to set password. Reason code " + processNamingException(nme) + ". Please see the logs for more detail.");
			}
			catch (PasswordChangeRequiredException pcre) {
				LOG.warn("Could not use change password because 'Change Password At Next Login' was set. Falling back to setPassword. Depending on the version of Active Directory in use, this may bypass password history checks.");
				setPassword(identity, password, false);
			}
		}
	}
	
	protected String processNamingException(org.springframework.ldap.NamingException nme) {
		
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

	protected void checkNamingException(String errorText,
			org.springframework.ldap.NamingException nme)
			throws ConnectorException {
		processNamingException(nme);
		DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
		String reason = dep.getReason();
		LOG.error(errorText + ". Reason code give was " + reason, nme);
		throw new ConnectorException(
				"Failed to perform operation. Reason code " + reason
						+ ". Please see the logs for more detail.");
	}
	
	protected SimpleLdapOperations getLdapTemplate(
			DirectoryIdentity directoryIdentity, char[] password) {
		return buildLdapTemplateForDomain(directoryIdentity.getDn().toString(),
				String.valueOf(password), getDomain(directoryIdentity.getDn()));
	}

	protected void setPassword(Identity identity, char[] password,
			boolean forcePasswordChangeAtLogon) throws ConnectorException {
		DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;

		setPassword(
				buildLdapTemplateForDomain(getActiveDirectoryConfiguration().getServiceAccountDn(),
						getActiveDirectoryConfiguration().getServiceAccountPassword(),
						getDomain(((DirectoryIdentity) identity).getDn())), 
							directoryIdentity, password, forcePasswordChangeAtLogon);
	}

	@Override
	protected Iterator<Identity> getIdentities(String filter) {
		
		final ActiveDirectoryConfiguration config = (ActiveDirectoryConfiguration) getConfiguration();
		final Map<String, ActiveDirectoryGroup> groups = new HashMap<String, ActiveDirectoryGroup>();
		final Map<Long, ActiveDirectoryGroup> groupsByRID = new HashMap<Long, ActiveDirectoryGroup>();
		
		if(config.isEnableRoles()) {
			Iterator<Role> it = getRoles();
			while(it.hasNext()) {
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
		return searchForResults(filter, new PrincipalContextMapper<Identity>() {

			protected boolean isAttributeMapped(Attribute attribute) {
				return DEFAULT_USER_ATTRIBUTES.contains(attribute.getID())
						|| identityAttributesToRetrieve.contains(attribute
								.getID());
			}

			@Override
			public final Identity mapFromContext(DirContextOperations result) {

				String guid = getByteValue(OBJECT_GUID_ATTRIBUTE, result);
				Name udn = result.getDn();
				String domain = getDomain(udn);
				String username = selectUsername(result);
				DirectoryIdentity directoryIdentity = new DirectoryIdentity(
						guid, username, udn);
				String userPrincipalName = StringUtil.nonNull(result
						.getStringAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE));

				// If service account, mark as 'system'
				String serviceAccountDn = config.getServiceAccountDn();
				if (userPrincipalName.equals(serviceAccountDn)) {
					directoryIdentity.setSystem(true);
				}

				String otherName = getOtherName(username, userPrincipalName,
						domain, config);
				directoryIdentity.setAddress(Media.email,
						result.getStringAttribute(MAIL_ATTRIBUTE));
				directoryIdentity.setFullName(result
						.getStringAttribute(COMMON_NAME_ATTRIBUTE));
				directoryIdentity.setOtherName(otherName);
				// String phoneNumber =
				// result.getStringAttribute(PHONE_NUMBER_ATTRIBUTE);
				// if (!StringUtil.isNullOrEmpty(phoneNumber)) {
				// directoryIdentity.setContactDetails(ContactDetailsType.phone,
				// phoneNumber);
				// }
				String phoneNumber = result
						.getStringAttribute(MOBILE_PHONE_NUMBER_ATTRIBUTE);
				if (!StringUtil.isNullOrEmpty(phoneNumber)) {
					directoryIdentity.setAddress(Media.mobile, phoneNumber);
				}
				// phoneNumber =
				// result.getStringAttribute(OTHER_PHONE_NUMBER_ATTRIBUTE);
				// if (!StringUtil.isNullOrEmpty(phoneNumber)) {
				// directoryIdentity.setContactDetails(ContactDetailsType.otherPhone,
				// phoneNumber);
				// }

				// Last sign on
				String lastLogonTimestamp = result
						.getStringAttribute(LAST_LOGON_TIMESTAMP_ATTRIBUTE);
				if (!StringUtil.isNullOrEmpty(lastLogonTimestamp)) {
					long lastLogonTime = Long.parseLong(lastLogonTimestamp);
					if (lastLogonTime > 0) {
						directoryIdentity
								.setLastSignOnDate(ActiveDirectoryDateUtil
										.adTimeToJavaDate(lastLogonTime));
					}
				}
				String lastLogon = result
						.getStringAttribute(LAST_LOGON_ATTRIBUTE);
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
					passwordStatus.setUnlocked(getAgedDate(minimumPasswordAge,
							passwordLastSet));
				}
				if (!isPasswordNeverExpire(result)) {
					passwordStatus.setExpire(getAgedDate(maximumPasswordAge,
							passwordLastSet));
				}

				String userDn = ActiveDirectoryConfiguration.buildUsername(
						config.getBaseDn().toString(), config.getDomain(),
						directoryIdentity.getPrincipalName());
				if (userDn.equalsIgnoreCase(getConfiguration()
						.getServiceAccountDn())) {
					// Do not allow the service account password to be reset
					passwordStatus.setType(PasswordStatusType.noChangeAllowed);
				} else if (isPasswordChangeRequired(result)) {
					passwordStatus.setType(PasswordStatusType.changeRequired);
				} else {
					passwordStatus.calculateType();
				}
				String userAccountControl = result
						.getStringAttribute(USER_ACCOUNT_CONTROL_ATTRIBUTE);

				// Overrides calculated password status, prevent the user
				// changing the password at all
				if (passwordStatus.getType().equals(PasswordStatusType.expired)) {
					if (userAccountControl.length() != 0) {
						if (UserAccountControl.isValueSet(
								Integer.valueOf(userAccountControl),
								UserAccountControl.DONT_EXPIRE_PASSWORD_FLAG)) {
							passwordStatus.setType(PasswordStatusType.upToDate);
						}
					}
				}
				if (!passwordChangeAllowed) {
					passwordStatus.setType(PasswordStatusType.noChangeAllowed);
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
				if (accountStatus.getType().equals(AccountStatusType.locked)) {
					accountStatus
							.setUnlocked(trimDate(new Date(accountStatus
									.getLocked().getTime()
									- (lockoutDuration / 1000))));
				}

				// TODO add primary group
				// boolean memberOfSupported =
				// result.getStringAttributes(MEMBER_OF_ATTRIBUTE) != null;
				if (config.isEnableRoles()) {
					boolean memberOfSupported = true;
					
					try {
						Long rid = Long.parseLong(result.getStringAttribute(PRIMARY_GROUP_ID_ATTRIBUTE));
						ActiveDirectoryGroup primaryGroup = groupsByRID.get(rid);
						if(primaryGroup!=null) {
							directoryIdentity.addRole(primaryGroup);
						}
					} catch (NumberFormatException e) {
					}
					
					Iterator<String> groupDnsItr = memberOfSupported ? getUsersGroups(result)
							: getGroupsForUser(result);
					while (groupDnsItr.hasNext()) {
						String dn = groupDnsItr.next();

						// https://jira.springsource.org/browse/LDAP-109
						dn = dn.replace("\\\\", "\\\\\\");
						dn = dn.replace("/", "\\/");

						if (groups.containsKey(dn.toLowerCase())) {
							directoryIdentity.addRole(groups.get(dn.toLowerCase()));
						} else {
							DirContextOperations groupContex = getLdapTemplate()
									.lookupContext(dn);
							ActiveDirectoryGroup activeDirectoryGroup = mapRole(groupContex);
							if (activeDirectoryGroup != null) {
								groups.put(dn.toLowerCase(), activeDirectoryGroup);
								groupsByRID.put(activeDirectoryGroup.getRid(), activeDirectoryGroup);
								directoryIdentity.addRole(activeDirectoryGroup);
							}
						}
					}
				} else {
					directoryIdentity.setRoles(new Role[0]);
				}
				return directoryIdentity;
			}

		}, configureSearchControls(new SearchControls()));
	}
	

	private boolean isPasswordChangeRequired(DirContextOperations result) {
		try {
			String value = result.getStringAttribute(PWD_LAST_SET_ATTRIBUTE);
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

	private String selectUsername(DirContextOperations searchResult) {

		ActiveDirectoryConfiguration config = getActiveDirectoryConfiguration();

		String domain = getDomain(searchResult.getDn());
		boolean isPrimaryDomain = domain.equalsIgnoreCase(config.getDomain());
		String samAccountName = StringUtil.nonNull(searchResult
				.getStringAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE));
		String userPrincipalName = StringUtil.nonNull(searchResult
				.getStringAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE));
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
			cal.setTimeInMillis(0);
			cal.set(Calendar.YEAR, 3999);
		}
		return cal.getTime();
	}

	private Date getDateAttribute(DirContextOperations result,
			String dateAttribute) {

		try {
			String value = result.getStringAttribute(dateAttribute);
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
	 */
	private boolean isPasswordChangeAllowed(DirContextOperations result) {
		String attributeValue = StringUtil.nonNull(result
				.getStringAttribute(USER_ACCOUNT_CONTROL_ATTRIBUTE));
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

	private boolean isPasswordNeverExpire(DirContextOperations result) {
		String attributeValue = StringUtil.nonNull(result
				.getStringAttribute(USER_ACCOUNT_CONTROL_ATTRIBUTE));
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

		AndFilter andFilter = new AndFilter();
		andFilter.and(new NotFilter(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE,
				"computer")));
		andFilter.and(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE, "user"));

		OrFilter orFilter = new OrFilter();
		orFilter.or(new LikeFilter(SAM_ACCOUNT_NAME_ATTRIBUTE, identityName));
		orFilter.or(new LikeFilter(USER_PRINCIPAL_NAME_ATTRIBUTE, identityName));
		if (!activeDirectoryConfiguration.isUsernameSamAccountName()) {
			int idx = identityName.indexOf('@');
			if (idx == -1) {
				identityName += "@" + activeDirectoryConfiguration.getDomain();
				orFilter.or(new LikeFilter(USER_PRINCIPAL_NAME_ATTRIBUTE,
						identityName));
			}
		}
		andFilter.and(orFilter);
		System.out.println(andFilter.encode());
		return andFilter.encode();
	}

	private Iterator<String> getUsersGroups(DirContextOperations result) {
		String[] memberOfAttribute = result
				.getStringAttributes(MEMBER_OF_ATTRIBUTE);
		if (memberOfAttribute == null) {
			return Collections.<String> emptyList().iterator();
		}
		return Arrays.asList(memberOfAttribute).iterator();
	}

	private Iterator<String> getGroupsForUser(DirContextOperations result) {
		String filter = buildObjectClassFilter("group", "member",
				result.getNameInNamespace());
		return searchForResults(filter,
				new AbstractParameterizedContextMapper<String>() {
					@Override
					protected String doMapFromContext(
							DirContextOperations result) {
						return result.getNameInNamespace();
					}
				}, configureRoleSearchControls(new SearchControls()));
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
	protected ActiveDirectoryGroup mapRole(DirContextOperations result) {
		String commonName = StringUtil.nonNull(result
				.getStringAttribute(COMMON_NAME_ATTRIBUTE));
		if (commonName.length() != 0) {
			String guid = getByteValue(OBJECT_GUID_ATTRIBUTE, result);
			byte[] sid = (byte[]) result
					.getObjectAttribute(OBJECT_SID_ATTRIBUTE);
			return new ActiveDirectoryGroup(guid, commonName, result.getDn(),
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

	protected SimpleLdapOperations buildLdapTemplateForDomain(String domain) {
		return buildLdapTemplateForDomain(getActiveDirectoryConfiguration()
				.getServiceAccountDn(), getActiveDirectoryConfiguration()
				.getServiceAccountPassword(), domain);
	}

	protected SimpleLdapOperations buildLdapTemplateForDomain(String serviceAccountDn, String serviceAccountPassword, String domain, Control... controls) {

		try {

			String[] host = getActiveDirectoryConfiguration()
					.getControllerHosts();
			if (!domain.equalsIgnoreCase(getActiveDirectoryConfiguration()
					.getDomain())) {

				synchronized (childDomainControllers) {

					if (!childDomainControllers.containsKey(domain)) {

						for (String s : getActiveDirectoryConfiguration()
								.getChildDomainControllers()) {
							if (s.toLowerCase().endsWith(domain.toLowerCase())) {
								childDomainControllers.put(domain, s);
								break;
							}
						}

						if (!childDomainControllers.containsKey(domain)) {

							SimpleResolver res = new SimpleResolver();
							Record rec = Record.newRecord(
									org.xbill.DNS.Name.fromString("_ldap._tcp."
											+ domain, org.xbill.DNS.Name.root),
									Type.SRV, DClass.ANY);
							Message query = Message.newQuery(rec);

							Message response = res.send(query);
							Record[] records = response
									.getSectionArray(Section.ANSWER);
							if (records != null && records.length > 0) {
								String dc = records[0].getAdditionalName()
										.toString();
								if (dc.endsWith(".")) {
									dc = dc.substring(0, dc.length() - 1);
								}
								LOG.info("Found child domain controller " + dc
										+ " for domain " + domain);
								childDomainControllers.put(domain, dc);
							} else {
								throw new UncategorizedLdapException(
										"Unable to resolve domain controller for "
												+ domain);
							}
						}
					}

					host = new String[] { getActiveDirectoryConfiguration()
							.buildProviderUrl(true, false,
									childDomainControllers.get(domain)) };
				}

			} else {
				// Build URLs but we don't want to use the global catalog as we
				// want access to the domain
				host = getActiveDirectoryConfiguration().getProviderURLList(
						false);
			}

			LdapContextSource contextSource = new LdapContextSource();
			// contextSource.setBase(configuration.getBaseDn().toString());
			contextSource.setPassword(serviceAccountPassword);
			contextSource.setUrls(host);
			contextSource.setUserDn(serviceAccountDn);
			contextSource
					.setBaseEnvironmentProperties(getActiveDirectoryConfiguration()
							.getConnectorConfigurationParameters());
			contextSource.afterPropertiesSet();

			LdapContext ldapContext;
			ldapContext = (LdapContext)contextSource.getReadWriteContext();
			if(controls!=null && controls.length > 0) {
				ldapContext.setRequestControls(controls);
			} 
			
			SimpleLdapTemplate ldapTemplate = new SimpleLdapTemplate(new SingleContextSource(ldapContext));
			LdapTemplate ldapOperations = (LdapTemplate) ldapTemplate.getLdapOperations();
			ldapOperations.setIgnorePartialResultException(true);
			
			return ldapTemplate;
		} catch (org.springframework.ldap.NamingException ne) {
			throw ne;
		} catch (Exception expt) {
			throw new UncategorizedLdapException(
					"Failed to build LdapTemplate", expt);
		}

	}

}