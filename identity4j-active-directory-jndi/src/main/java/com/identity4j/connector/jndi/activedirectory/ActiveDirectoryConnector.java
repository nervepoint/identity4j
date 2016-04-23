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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
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
import com.identity4j.connector.principal.Principal;
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
	public static final String PASSWORD_POLICY_APPLIES = "msDS-PSOApplied";
	
	/**
	 * This is a special attribute we add to mimic the Office365 ImmutableID
	 */
	public static final String IMMUTABLE_ID_ATTR = "ImmutableID";

	public static final String GLOBAL = "-2147483646";
	public static final String DOMAIN_LOCAL = "-2147483644";
	public static final String UNIVERSAL = "-2147483640";
	
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
					LAST_LOGON_ATTRIBUTE, 
					LAST_LOGON_TIMESTAMP_ATTRIBUTE,
					PWD_LAST_SET_ATTRIBUTE, 
					OU_ATTRIBUTE, 
					COMMON_NAME_ATTRIBUTE, 
					IMMUTABLE_ID_ATTR });

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

	private static Collection<String> CORE_IDENTITY_ATTRIBUTES = Arrays.asList(new
	 String[] { COMMON_NAME_ATTRIBUTE, SAM_ACCOUNT_NAME_ATTRIBUTE, 
			 USER_PRINCIPAL_NAME_ATTRIBUTE, OBJECT_CLASS_ATTRIBUTE });

	private static Collection<String> ALL_ROLE_ATTRIBUTES = Arrays
			.asList(new String[] { OBJECT_SID_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE,
					COMMON_NAME_ATTRIBUTE, DISTINGUISHED_NAME_ATTRIBUTE });

	public static final int CHANGE_PASSWORD_AT_NEXT_LOGON_FLAG = 0;
	public static final int CHANGE_PASSWORD_AT_NEXT_LOGON_CANCEL_FLAG = -1;

	// Bit mask values for pwdProperties
	public static final int DOMAIN_PASSWORD_COMPLEX = 0x01;

	private List<String> identityAttributesToRetrieve = new ArrayList<String>(
			ALL_USER_ATTRIBUTES);

	// Controls for Win2008 R2 password history on admin reset
	final byte[] controlData = { 48, (byte) 132, 0, 0, 0, 3, 2, 1, 1 };
	final String LDAP_SERVER_POLICY_HINTS_OID = "1.2.840.113556.1.4.2066";


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
				getMinimumPasswordAge(),
				0,
				"Default Group Policy",
				null);
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
	
	protected void processAttributes(
			List<ModificationItem> modificationItems, 
			Principal previousState, 
			Principal newState) {
		
		for (Map.Entry<String, String[]> entry : newState.getAttributes()
				.entrySet()) {
			if (!isExcludeForUpdate(entry.getKey())) {
				if (!previousState.getAttributes()
						.containsKey(entry.getKey())) {
					// New
					if (entry.getValue().length > 0) {

						String[] value = entry.getValue();
						if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
							Attribute attr = new BasicAttribute(entry.getKey());
							for(String val : value) {
								attr.add(val);
							}
							modificationItems.add(new ModificationItem(
									DirContext.ADD_ATTRIBUTE, attr));
						}
					}
				} else {
					String[] oldValue = previousState.getAttributes().get(entry.getKey());
					String[] newValue = newState.getAttributes().get(entry.getKey());
					if (!ArrayUtils.isEquals(oldValue, newValue)) {

						String[] value = entry.getValue();
						if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
							Attribute attr = new BasicAttribute(entry.getKey());
							for(String val : value) {
								attr.add(val);
							}
							modificationItems.add(new ModificationItem(
									DirContext.REPLACE_ATTRIBUTE, attr));
						} else {
							Attribute attr = new BasicAttribute(entry.getKey());
							modificationItems.add(new ModificationItem(
									DirContext.REMOVE_ATTRIBUTE, attr));
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
			String roleOu = role.getAttribute(DISTINGUISHED_NAME_ATTRIBUTE);
			LdapName roleDn = new LdapName(roleOu);
			String principalName = role.getPrincipalName();

			processAttributes(modificationItems, oldRole, role);

			if (Util.differs(
					oldRole.getAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE),
					principalName)) {
				Attribute attribute = new BasicAttribute(
						SAM_ACCOUNT_NAME_ATTRIBUTE, principalName);
				modificationItems.add(new ModificationItem(
						DirContext.REPLACE_ATTRIBUTE, attribute));
			}


			ldapService.update(roleDn,
					modificationItems.toArray(new ModificationItem[0]));

			
			// Update roles
//			List<Role> toRemove = new ArrayList<Role>(Arrays.asList(oldIdentity.getRoles()));
//			List<Role> toAdd = new ArrayList<Role>(Arrays.asList(identity.getRoles()));
//			toRemove.removeAll(Arrays.asList(identity.getRoles()));
//			toAdd.removeAll(Arrays.asList(oldIdentity.getRoles()));
//			
//			for(Role r : toRemove) {
//				revokeRole(usersDn, r);
//			}
//			
//			for(Role r : toAdd) {
//				assignRole(usersDn, r);
//			}
			
			if (Util.differs(
					oldRole.getAttribute(COMMON_NAME_ATTRIBUTE),
					role.getAttribute(COMMON_NAME_ATTRIBUTE))) {
				LdapName newDN = new LdapName(roleDn.toString());
				newDN.remove(newDN.size() - 1);
				newDN.add(newDN.size(),
						"CN=" + role.getAttribute(COMMON_NAME_ATTRIBUTE));
				ldapService.rename(roleDn, newDN);
			} else if (Util.differs(oldRole.getAttribute(OU_ATTRIBUTE),
					role.getAttribute(OU_ATTRIBUTE))) {
				LdapName newDN = new LdapName("CN="
						+ role.getAttribute(COMMON_NAME_ATTRIBUTE) + ","
						+ role.getAttribute(OU_ATTRIBUTE));
				ldapService.rename(roleDn, newDN);
			}

		} catch (NamingException e) {
			LOG.error("Problem in update role", e);
		} catch (IOException e) {
			LOG.error("Problem in update role", e);
		}
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

			processAttributes(modificationItems, oldIdentity, identity);

			String principalNameWithDomain = principalName + "@"
					+ config.getDomain();
			if (!principalName.equalsIgnoreCase(
					oldIdentity.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE))
					&& !principalNameWithDomain.equalsIgnoreCase(
							oldIdentity.getAttribute(USER_PRINCIPAL_NAME_ATTRIBUTE))) {
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
				revokeRole(usersDn, r);
			}
			
			for(Role r : toAdd) {
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
			throw new ConnectorException(e);
		} catch (IOException e) {
			LOG.error("Problem in update identity", e);
			throw new ConnectorException(e);
		}

	}

	private boolean isExcludeForUpdate(String attributeName) {
		return ATTRIBUTES_TO_EXCLUDE_FROM_UPDATE.contains(attributeName);
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
				throw new ConnectorException("The User DN (" + roleDn
						+ ") must be a child of the Base DN (" + baseDn
						+ " configured for the Active Directory connector.");
			}

			/*
			 * Set up the attributes for the primary details. Some of these may
			 * already have been in the generic attributes
			 */
			List<Attribute> attributes = new ArrayList<Attribute>();

			// First copy in the generic attributes
			for (Map.Entry<String, String[]> entry : role.getAttributes()
					.entrySet()) {
				String[] value = entry.getValue();
				if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
					Attribute attr = new BasicAttribute(entry.getKey());
					for(String val : value) {
						attr.add(val);
					}
					attributes.add(attr);
				}
			}

			String[] objectClasses = new String[] { "top", "group" };
			BasicAttribute objectClassAttributeValues = new BasicAttribute(
					OBJECT_CLASS_ATTRIBUTE);
			for (String objectClass : objectClasses) {
				objectClassAttributeValues.add(objectClass);
			}
			attributes.add(objectClassAttributeValues);
			
			String sAMAccountName = role.getPrincipalName();
			
			if(StringUtils.isEmpty(sAMAccountName)) {
				sAMAccountName = principalName;
				if(sAMAccountName.contains("@")) {
					sAMAccountName = sAMAccountName.substring(0, sAMAccountName.indexOf("@"));
				}
			}
			
			attributes.add(new BasicAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE,
					sAMAccountName));

			ldapService.bind(roleDn, attributes.toArray(new Attribute[0]));
			
			return getRoleByName(role.getPrincipalName());

		} catch (InvalidNameException e) {
			LOG.error("Problem in create group", e);
			throw new ConnectorException("Failed to create group; " + e.getMessage(), e);
		} catch (NamingException e) {
			LOG.error("Problem in create group", e);
			throw new ConnectorException("Failed to create group; " + e.getMessage(), e);
		} catch (IOException e) {
			LOG.error("Problem in create group", e);
			throw new ConnectorException("Failed to create group; " + e.getMessage(), e);
		}
		
		
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

			StringBuilder tmp = new StringBuilder();
			tmp.append(identity.getAttribute("givenName"));
			String initials = identity.getAttribute("initials");
			if(StringUtils.isNotBlank(initials)) {
				tmp.append(" ");
				tmp.append(initials);
				tmp.append(".");
			}
			tmp.append(" ");
			tmp.append(identity.getAttribute("sn"));
			
			userDn.add("CN=" +  tmp.toString());

			identity.setFullName(tmp.toString());
			identity.setAttribute("cn", tmp.toString());
			
			Name baseDn = getConfiguration().getBaseDn();
			if (!userDn.toString().toLowerCase().endsWith(baseDn.toString().toLowerCase())) {
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
				if(CORE_IDENTITY_ATTRIBUTES.contains(entry.getKey())) {
					continue;
				}
				String[] value = entry.getValue();
				if (value.length > 0 && !StringUtils.isEmpty(value[0])) {
					if(value.length==1) {
						attributes.add(new BasicAttribute(entry.getKey(), value[0]));
					} else {
						Attribute attr = new BasicAttribute(entry.getKey());
						for(String val : value) {
							LOG.info("Setting " + entry.getKey() + " = " + val);
							attr.add(val);
						}
						attributes.add(attr);
					}
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
			}
			
			attributes.add(new BasicAttribute(SAM_ACCOUNT_NAME_ATTRIBUTE,
					sAMAccountName));

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
		
		throw new ConnectorException("Failed to create identity " + identity.getPrincipalName());
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

			try {
				ldapService.update(((DirectoryIdentity) identity).getDn(),
						items.toArray(new ModificationItem[items.size()]));
			} catch (NamingException e) {
				throw new ConnectorException(
						"Failed to change password. Reason code "
								+ processNamingException(e));
			} catch (IOException e) {
				throw new ConnectorException("Failed to connect: " + e.getMessage(), e);
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
				
				public boolean isApplyFilters() {
					return true;
				}
			});
		} catch (NamingException e) {
			LOG.error("Problem in getting roles", e);
		} catch (IOException e) {
			LOG.error("Problem in getting roles", e);
		}
		return ROLE_ITERATOR;

	}
	
	private String buildPSOFilter() {
		return ldapService.buildObjectClassFilter("msDS-PasswordSettings", "cn", WILDCARD_SEARCH);
	}

	public Iterator<ADPasswordCharacteristics> getPasswordPolicies() {
		
		try {
			return ldapService.search(buildPSOFilter(), new ResultMapper<ADPasswordCharacteristics>() {

				@Override
				public ADPasswordCharacteristics apply(SearchResult result) throws NamingException, IOException {
					return loadCharacteristics(result);
				}
				public boolean isApplyFilters() {
					return false;
				}
			});
		} catch (NamingException e) {
			LOG.error("Problem in getting PSOs", e);
		} catch (IOException e) {
			LOG.error("Problem in getting PSOs", e);
		}
		
		return CollectionUtil.emptyIterator(ADPasswordCharacteristics.class);
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
						"Failed to change password. Reason code "
								+ processNamingException(nme));
			} catch (PasswordChangeRequiredException pcre) {
				LOG.warn("Could not use change password because 'Change Password At Next Login' was set. Falling back to setPassword. Depending on the version of Active Directory in use, this may bypass password history checks.");
				setPassword(identity, password, false, PasswordResetType.USER);
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
					"Could not perform the requested operation. Please configure the server to connect to your Active Directory securely over SSL.");
		} else if (reason.equals("80090308") && "773".equals(dep.getData())) {
			throw new PasswordChangeRequiredException(
					"Cannot change password when changePasswordPasswordAtNextLogin is set, must use setPassword");
		} else if (reason.equals("80090308") && "525".equals(dep.getData())) {
			throw new ConnectorException(
					"The user cannot be found.");
		} else if (reason.equals("80090308") && "52e".equals(dep.getData())) {
			throw new ConnectorException(
					"Invalid credentials");
		} else if (reason.equals("80090308") && "530".equals(dep.getData())) {
			throw new ConnectorException(
					"Not permitted to logon at this time");
		} else if (reason.equals("80090308") && "531".equals(dep.getData())) {
			throw new ConnectorException(
					"Not permitted to logon at this workstation");
		} else if (reason.equals("80090308") && "532".equals(dep.getData())) {
			throw new ConnectorException(
					"The password has expired");
		} else if (reason.equals("80090308") && "533".equals(dep.getData())) {
			throw new ConnectorException(
					"Account disabled");
		} else if (reason.equals("80090308") && "534".equals(dep.getData())) {
			throw new ConnectorException(
					"The user has not been granted the requested logon type at this machine");
		} else if (reason.equals("80090308") && "701".equals(dep.getData())) {
			throw new ConnectorException(
					"Account expired");
		} else if (reason.equals("80090308") && "775".equals(dep.getData())) {
			throw new ConnectorException(
					"User account is locked");
		} else if(reason.equals("00000005") && "0".equals(dep.getData())) {
			throw new ConnectorException("The administrator does not allow you to change your password.");
		}
		LOG.error(nme.getMessage() + ". Reason code give was " + reason, nme);
		
		super.processNamingException(nme);
		
		return reason;

	}

	protected void checkNamingException(NamingException nme)
			throws ConnectorException {
		processNamingException(nme);
		DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
		String reason = dep.getReason();
		LOG.error("Processed naming exception. Reason code give was " + reason, nme);
		throw new ConnectorException(
				"Failed to perform operation. Reason code " + reason
						+ ". Please see the logs for more detail.");
	}

	protected void setPassword(Identity identity, char[] password,
			boolean forcePasswordChangeAtLogon, PasswordResetType type) throws ConnectorException {
		try {
			
			if(forcePasswordChangeAtLogon) {
				switch(identity.getPasswordStatus().getType()) {
					case neverExpires:
						throw new ConnectorException("You cannot force password change at next login as the user's password is set to never expire.");
					case noChangeAllowed:
						throw new ConnectorException("You cannot force password change at next login as the user is not allowed to change their password.");
					default:
					{
					
					}
				}
			}
			String newQuotedPassword = "\"" + new String(password) + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

			if(type == PasswordResetType.USER) {
				ldapService.setPassword(((DirectoryIdentity) identity).getDn()
						.toString(), newUnicodePassword, 
						new BasicControl(LDAP_SERVER_POLICY_HINTS_OID, true, controlData));
			} else {
				ldapService.setPassword(((DirectoryIdentity) identity).getDn()
						.toString(), newUnicodePassword);
			}
			
			setForcePasswordChangeAtNextLogon((DirectoryIdentity)identity, forcePasswordChangeAtLogon);
		
		} catch (NamingException e) {
			LOG.error("Problem in set password for identity", e);
			processNamingException(e);
		} catch (IOException e) {
			LOG.error("Problem in set password for identity", e);
			throw new ConnectorException(e);
		}

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
					return true;
				}

				@Override
				public Identity apply(SearchResult result)
						throws NamingException {
					Attributes attributes = result.getAttributes();

					byte[] guidBytes = (byte[]) getAttribute(attributes.get(OBJECT_GUID_ATTRIBUTE));
					String guid = UUID.nameUUIDFromBytes(guidBytes).toString();
					Name udn = new LdapName(result.getNameInNamespace());
					String domain = getDomain(udn);
					String username = selectUsername(result);
					DirectoryIdentity directoryIdentity = new DirectoryIdentity(
							guid, username, udn);

					// Generate Immutable ID for MSOL services
					String guidBase64 = org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.encodeBase64(guidBytes, false));
					directoryIdentity.setAttribute(IMMUTABLE_ID_ATTR, guidBase64.trim());

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
										.setType(PasswordStatusType.neverExpires);
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
				
				public boolean isApplyFilters() {
					return true;
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
			
			public boolean isApplyFilters() {
				return true;
			}
		});

	}

	@Override
	protected void assertPasswordChangeIsAllowed(Identity identity,
			char[] oldPassword, char[] password) throws ConnectorException {
		if(identity.getPasswordStatus().isNeedChange()) {
			return;
		}
		Date lastPasswordChange = identity.getPasswordStatus().getLastChange();
		if (lastPasswordChange != null
				&& !Util.isDatePast(lastPasswordChange, getMinimumPasswordAge())) {
			throw new PasswordChangeTooSoonException(lastPasswordChange);
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
					.get(OBJECT_SID_ATTRIBUTE));
		
			ActiveDirectoryGroup group = new ActiveDirectoryGroup(guid, 
					commonName, 
					new LdapName(dn),
					sid);
			
			NamingEnumeration<? extends Attribute> en = attributes.getAll();
			while(en.hasMoreElements()) {
				Attribute attribute = en.nextElement();
				group.setAttribute(attribute.getID(),
						getElements(attribute));
			}
			
			
			LdapName ou = new LdapName((String) getAttribute(attributes
					.get(DISTINGUISHED_NAME_ATTRIBUTE)));
			ou.remove(ou.size() - 1);
			group.setAttribute(OU_ATTRIBUTE, ou.toString());
			
			
			return group;
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
	
	protected Object getAttributeValue(Attributes attrs, String attrName) throws NamingException {
		Attribute attr = attrs.get(attrName);
		if(attr==null) {
			return null;
		}
		return attr.get();
	}
	
	protected String getStringAttribute(Attributes attrs, String attrName) throws NamingException {
		return (String) getAttributeValue(attrs, attrName);
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
            String attr = defaults.getAttributes().get("activeDirectory." + ActiveDirectoryConnector.PWD_HISTORY_LENGTH);
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

        return new ADPasswordCharacteristics(complex, Integer.parseInt(minPwdLengthField), Integer.parseInt(historyLength),
                        ActiveDirectoryDateUtil.adTimeToJavaDays(Long.parseLong(maxAge)),
                        ActiveDirectoryDateUtil.adTimeToJavaDays(Long.parseLong(minAge)), Integer.parseInt(precedence),
                        getStringAttribute(attributes, "cn"), pso.getNameInNamespace());

    }


}