/* HEADER */
package com.identity4j.connector.jndi.directory;

/*
 * #%L
 * Idenity4J LDAP Directory JNDI
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.util.DummySSLSocketFactory;
import com.identity4j.util.MultiMap;

/**
 */
public abstract class AbstractDirectoryConfiguration extends AbstractConnectorConfiguration {

	public enum RoleMode {
		disabled, principalNames, distinguishedNames, serverDistinguishedNames
	}

	static Log LOG = LogFactory.getLog(AbstractDirectoryConfiguration.class);

	/**
	 * Configuration property key for Hostname
	 */
	public static final String DIRECTORY_HOSTNAME = "directory.hostname";
	/**
	 * Configuration property key for Hostname
	 */
	public static final String DIRECTORY_BACKUP_HOSTNAMES = "directory.backupHostnames";
	/**
	 * Configuration property key for Base DN
	 */
	public static final String DIRECTORY_BASE_DN = "directory.baseDn";
	/**
	 * Configuration property key for OU for user creation
	 */
	public static final String DIRECTORY_USER_OU = "directory.userOU";
	/**
	 * Configuration property key for Service Account Username
	 */
	public static final String DIRECTORY_SERVICE_ACCOUNT_USERNAME = "directory.serviceAccountUsername";
	/**
	 * Configuration property key for Service Account Password
	 */
	public static final String DIRECTORY_SERVICE_ACCOUNT_PASSWORD = "directory.serviceAccountPassword";
	/**
	 * Configuration property key for domain
	 */
	public static final String DIRECTORY_DOMAIN = "directory.domain";
	/**
	 * Configuration property key for security protocol
	 */
	public static final String DIRECTORY_SECURITY_PROTOCOL = "directory.protocol";

	public static final String DIRECTORY_EXCLUDES = "directory.excludes";

	public static final String DIRECTORY_INCLUDES = "directory.includes";

	public static final String DIRECTORY_ROLE_MODE = "directory.roleMode";

	public static final String DIRECTORY_INCLUDE_ROLES = "directory.includeRoles";

	public static final String DIRECTORY_EXCLUDE_ROLES = "directory.excludeRoles";

	public static final String DIRECTORY_INCLUDE_ROLES_DN = "directory.includeRolesDN";

	public static final String DIRECTORY_EXCLUDE_ROLES_DN = "directory.excludeRolesDN";

	public static final String DIRECCTORY_IDENTITY_CREATION_OBJECT_CLASSES = "direcctory.identityCreationObjectClasses";

	public static final String DIRECTORY_IDENTITY_OBJECT_CLASS = "directory.identityObjectClass";

	public static final String DIRECTORY_IDENTITY_NAME_ATTRIBUTE = "directory.identityNameAttribute";

	public static final String DIRECTORY_IDENTITY_CN_ATTRIBUTE = "directory.identityCNAttribute";

	public static final String DIRECTORY_IDENTITY_FULL_NAME_ATTRIBUTE = "directory.identityFullNameAttribute";

	public static final String DIRECTORY_UNIQUE_MEMBER_ATTRIBUTE = "directory.uniqueMemberAttribute";

	public static final String DIRECTORY_MEMBER_OF_ATTRIBUTE = "directory.memberOfAttribute";

	public static final String DIRECTORY_ROLE_GUID_ATTRIBUTE = "directory.roleGuidAttribute";

	public static final String DIRECTORY_ROLE_NAME_ATTRIBUTE = "directory.roleNameAttribute";

	public static final String DIRECTORY_ROLE_OBJECT_CLASS = "directory.roleObjectClass";

	public static final String DIRECTORY_IDENTITY_PASSWORD_ENCODING = "directory.identityPasswordEncoding";

	public static final String DIRECTORY_DISTINGUISHED_NAME_ATTRIBUTE = "directory.distinguishedNameAttribute";

	public static final String DIRECTORY_IDENTITY_PASSWORD_ATTRIBUTE = "directory.identityPasswordAttribute";

	public static final String DIRECTORY_IDENTITY_ROLE_NAME_ATTRIBUTE = "directory.identityRoleNameAttribute";

	public static final String DIRECTORY_IDENTITY_ROLE_GUID_ATTRIBUTE = "directory.identityRoleGuidAttribute";

	public static final String DIRECTORY_IDENTITY_GUID_ATTRIBUTE = "directory.identityGuidAttribute";

	public static final String DIRECTORY_IDENTITY_MOBILE_ATTRIBUTE = "directory.identityMobileAttribute";

	public static final String DIRECTORY_IDENTITY_EMAIL_ATTRIBUTE = "directory.identityEmailAttribute";

	public static final String ADDITIONAL_USER_ATTRIBUTES = "directory.additionalUserattributes";
	

	/**
	 * Follow referrals?
	 */
	public static final String DIRECTORY_FOLLOW_REFERRALS = "directory.followReferrals";

	/**
	 * Connect timeout
	 */
	public static final String DIRECTORY_CONNECT_TIMEOUT = "directory.timeout";

	/**
	 * Read timeout
	 */
	public static final String DIRECTORY_READ_TIMEOUT = "directory.readTimeout";

	/**
	 * Max page size
	 */
	public static final String DIRECTORY_MAX_PAGE_SIZE = "directory.maxPageSize";

	/**
	 */
	public static final char PORT_SEPARATOR = ':';

	/**
	 */
	public static final String COMMON_NAME = "CN=";

	/**
	 * Constant for value of {@link #DIRECTORY_SECURITY_PROTOCOL} when SSL should be
	 * used
	 */
	public static final String SSL = "ssl";

	/**
	 * Constant for value of {@link #DIRECTORY_SECURITY_PROTOCOL} when insecure
	 * connection should be used
	 */
	public static final String PLAIN = "plain";

	public static final String LDAP_PROTOCOL = "ldap://";
	public static final String LDAPS_PROTOCOL = "ldaps://";
	private final Name baseDn;
	private final Collection<Name> includes;
	private final Collection<Name> excludes;
	private final Set<String> includeRoles;
	private final Set<String> excludeRoles;
	private final Set<String> includeRolesDN;
	private final Set<String> excludeRolesDN;
	private RoleMode roleMode;

	String securityProtocol = SSL;

	/**
	 * @param configurationParameters
	 */
	public AbstractDirectoryConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
		try {
			baseDn = new LdapName(configurationParameters.getStringOrDefault(DIRECTORY_BASE_DN, ""));

			includes = getNames(configurationParameters.getStringArrayOrDefault(DIRECTORY_INCLUDES));
			excludes = getNames(configurationParameters.getStringArrayOrDefault(DIRECTORY_EXCLUDES));
			includes.removeAll(excludes);
			securityProtocol = configurationParameters.getStringOrDefault(DIRECTORY_SECURITY_PROTOCOL, SSL);
			try {
				roleMode = RoleMode.valueOf(configurationParameters.getStringOrDefault(DIRECTORY_ROLE_MODE,
						RoleMode.principalNames.name()));
			} catch (IllegalStateException ise) {
				LOG.warn(String.format("Invalid role mode, reverting to %s", RoleMode.principalNames));
				roleMode = RoleMode.principalNames;
			}
			includeRoles = new HashSet<String>(
					Arrays.asList(configurationParameters.getStringArrayOrDefault(DIRECTORY_INCLUDE_ROLES)));
			excludeRoles = new HashSet<String>(
					Arrays.asList(configurationParameters.getStringArrayOrDefault(DIRECTORY_EXCLUDE_ROLES)));
			includeRolesDN = new HashSet<String>(
					Arrays.asList(configurationParameters.getStringArrayOrDefault(DIRECTORY_INCLUDE_ROLES_DN)));
			excludeRolesDN = new HashSet<String>(
					Arrays.asList(configurationParameters.getStringArrayOrDefault(DIRECTORY_EXCLUDE_ROLES_DN)));
			if (includes.isEmpty()) {
				includes.add(baseDn);
			}
			setIdentityAttributesToRetrieve(Arrays.asList(configurationParameters.getStringArrayOrDefault(ADDITIONAL_USER_ATTRIBUTES)));
		} catch (NamingException ne) {
			throw new Error(ne);
		}
	}

	/**
	 * <p>
	 * The host name or IP address of the directory to connect to. If an IP address
	 * is used this should be in dotted decimal notation. Otherwise the fully
	 * qualified hostname should be specified in the standard dns format
	 * </p>
	 * <p>
	 * Examples: <code>192.168.1.200:443</code>, <code>192.168.1.200</code> or
	 * <code>host.directory.com</code>
	 * </p>
	 * 
	 * @return controller hosts
	 */
	public final String[] getControllerHosts() {
		List<String> l = new ArrayList<String>(
				Arrays.asList(configurationParameters.getStringArrayOrFail(DIRECTORY_HOSTNAME)));
		String[] tmp = configurationParameters.getStringArrayOrFail(DIRECTORY_BACKUP_HOSTNAMES);
		for (String t : tmp) {
			if (StringUtils.isNotBlank(t)) {
				l.add(t);
			}
		}
		return l.toArray(new String[0]);
	}

	/**
	 * <p>
	 * The host name or IP address of the directory to connect to, without the port
	 * number if one is set. If an IP address is used this should be in dotted
	 * decimal notation. Otherwise the fully qualified hostname should be specified
	 * in the standard dns format
	 * </p>
	 * <p>
	 * Examples: <code>192.168.1.200</code> or <code>host.directory.com</code>
	 * </p>
	 * 
	 * @return controller hosts
	 */
	public final String[] getControllerHostnames() {
		List<String> l = new ArrayList<String>();
		for (String h : getControllerHosts()) {
			l.add(getControllerHostWithoutPort(h));
		}
		return l.toArray(new String[0]);
	}

	/**
	 * Get a list of distinguished names to exclude from the search. These are
	 * relative to the Base DN. If the list is empty, all paths should be included
	 * unless explicit excludes have been set.
	 * 
	 * @return paths to exclude
	 * @throws InvalidNameException
	 */
	public Collection<Name> getIncludes() {
		return includes;
	}

	/**
	 * Get a list of distinguished names to exclude from the search. These are
	 * relative to the Base DN. If the list is empty, all paths should be included
	 * unless explicit includes have been set.
	 * 
	 * @return paths to exclude
	 * @throws InvalidNameException
	 */
	public Collection<Name> getExcludes() {
		return excludes;
	}

	public Set<String> getIncludedRolesDN() {
		return includeRolesDN;
	}

	public Set<String> getExcludedRolesDN() {
		return excludeRolesDN;
	}

	public Set<String> getIncludedRoles() {
		return includeRoles;
	}

	public Set<String> getExcludedRoles() {
		return excludeRoles;
	}

	/**
	 * Get if roles should be enabled at all.
	 * 
	 * @return reconcile roles
	 */
	public boolean isEnableRoles() {
		return !roleMode.equals(RoleMode.disabled);
	}

	public RoleMode getRoleMode() {
		return roleMode;
	}

	protected String[] buildProviderUrls(String... controllerHosts) {
		Collection<String> hosts = new ArrayList<String>();
		for (String controllerHost : controllerHosts) {
			hosts.add(controllerHost);
		}
		return hosts.toArray(new String[hosts.size()]);
	}

	protected static final String getControllerHostWithoutPort(String value) {
		int indexOf = value.lastIndexOf(PORT_SEPARATOR);
		return indexOf == -1 ? value : value.substring(0, indexOf);
	}

	/**
	 * The service authentication mechanism to use, the default is simple. This
	 * value is used when for the
	 * <code>javax.naming.Context.SECURITY_AUTHENTICATION</code> parameter when
	 * performing service activities e.g. listing and managing principals.
	 * 
	 * @return service authentication type
	 */
	public final String getServiceAuthenticationType() {
		return configurationParameters.getStringOrDefault("directory.serviceAuthenticationType", "simple");
	}

	public final boolean requiresServiceAuthentication() {
		return !"none".equals(getServiceAuthenticationType());
	}

	/**
	 * The security protocol to use, this defaults to SSL. This value is used for
	 * the <code>javax.naming.Context.SECURITY_PROTOCOL</code> parameter.
	 * 
	 * @return security protocol
	 */
	public final String getSecurityProtocol() {
		return securityProtocol;
	}

	/**
	 * Should referrals be followed. The Manage Referral control <a
	 * href=http://www.ietf.org/rfc/rfc3296.txt">(RFC 3296)</a> tells the LDAP
	 * server to return referral entries as ordinary entries (instead of returning
	 * "referral" error responses or continuation references). If you are using the
	 * LDAP v3 and have set Context.REFERRAL to "ignore", then the LDAP service
	 * provider will automatically send this control along with the request. If you
	 * are using the LDAP v2, then the control will not be sent because it is not
	 * applicable in that protocol. When you set Context.REFERRAL to any other
	 * value, the control will not be sent regardless of the protocol version.
	 * 
	 * @return follow referrals
	 */
	public final boolean isFollowReferrals() {
		return configurationParameters.getBooleanOrDefault(DIRECTORY_FOLLOW_REFERRALS, Boolean.FALSE);
	}

	/**
	 * The initial context factory to use, the defaults is
	 * <code>com.sun.jndi.ldap.LdapCtxFactory</code>. This value is used for the
	 * <code>javax.naming.Context.INITIAL_CONTEXT_FACTORY</code> parameter.
	 * 
	 * @return initial context factory
	 */
	public final String getInitialContextFactory() {
		return configurationParameters.getStringOrDefault("directory.initialContextFactory",
				"com.sun.jndi.ldap.LdapCtxFactory");
	}

	/**
	 * The value to use for <code>com.sun.jndi.ldap.connect.timeout</code>, the
	 * default is 30.
	 * 
	 * @return initial context factory
	 */
	public final int getTimeout() {
		return configurationParameters.getIntegerOrDefault(DIRECTORY_CONNECT_TIMEOUT, Integer.valueOf(30)) * 1000;
	}

	/**
	 * The maximum size of the page to return when using paged queries.
	 * 
	 * @return maximum page size
	 */
	public int getMaxPageSize() {
		return configurationParameters.getIntegerOrDefault(DIRECTORY_MAX_PAGE_SIZE, 1000);
	}

	/**
	 * The value to use for <code>java.naming.ldap.version</code>, the default is 3.
	 * 
	 * @return initial context factory
	 */
	public final String getVersion() {
		return configurationParameters.getStringOrDefault("directory.version", "3");
	}

	/**
	 * The base dn of the directory to connect to.
	 * 
	 * @return base dn
	 */
	public final Name getBaseDn() {
		return baseDn;
	}

	private Collection<Name> getNames(String... values) {
		Collection<Name> names = new ArrayList<Name>();
		for (String value : values) {
			if (StringUtils.isNotBlank(value)) {
				try {
					LdapName name = new LdapName(value);
					if (!name.startsWith(getBaseDn())) {
						name.addAll(0, getBaseDn());
					}
					names.add(name);
				} catch (Exception e) {
					throw new ConnectorException(String.format(
							"%s is not a properly formatted DN. Expected format <container>=<name> for example OU=Employees",
							value));
				}
			}
		}
		return names;
	}

	/**
	 * <p>
	 * The connector performs all operations on the directory using this account.
	 * The distinguished name of this account should be supplied in LDAP format,
	 * that is, with the Common Name (cn) of the account first followed by the
	 * container in which this account resides, then that container's container etc.
	 * The elements of the distinguished name should be separated using commas.
	 * </p>
	 * <p>
	 * For example: <code>cn=admin,ou=employee,o=root</code>
	 * </p>
	 * This value is used for the
	 * <code>javax.naming.Context.SECURITY_PRINCIPAL</code>
	 * 
	 * @return service account dn
	 */
	public final String getServiceAccountDn() {
		return configurationParameters.getStringOrFail(DIRECTORY_SERVICE_ACCOUNT_USERNAME);
	}

	/**
	 * <p>
	 * The password used for the service account @see
	 * {@link AbstractDirectoryConfiguration#getServiceAccountDn()}
	 * </p>
	 * 
	 * @return service account password
	 */
	public final String getServiceAccountPassword() {
		return configurationParameters.getStringOrFail(DIRECTORY_SERVICE_ACCOUNT_PASSWORD);
	}

	/**
	 * The class name required for an object to be considered an identity.
	 * 
	 * @return identity object class
	 */
	public final String getIdentityObjectClass() {
		return configurationParameters.getStringOrFail(DIRECTORY_IDENTITY_OBJECT_CLASS);
	}

	/**
	 * The class names required for an identity to be created.
	 * 
	 * @return identity object class
	 */
	public final List<String> getIdentityCreationObjectClasses() {
		if (configurationParameters.containsKey(DIRECCTORY_IDENTITY_CREATION_OBJECT_CLASSES))
			return Arrays.asList(configurationParameters.getStringOrFail(DIRECTORY_IDENTITY_OBJECT_CLASS).split(","));
		else {
			Set<String> n = new LinkedHashSet<String>();
			n.add("inetOrgPerson");
			n.add(getIdentityObjectClass());
			return new ArrayList<String>(n);
		}
	}

	/**
	 * The attribute name which is used as value of the common name. This may be either 'principalName', 'fullName' or
	 * any other generic property name. When blank, the principal name will be used.
	 * 
	 * @return identity CN attribute
	 */
	public final String getIdentityCNAttribute() {
		return configurationParameters.getStringOrDefault(DIRECTORY_IDENTITY_CN_ATTRIBUTE, "");
	}

	/**
	 * The attribute name which is used to match against the identity full name. Or empty not to 
	 * map this explicitly.
	 * 
	 * @return identity full name attribute
	 */
	public final String getIdentityFullNameAttribute() {
		return configurationParameters.getStringOrDefault(DIRECTORY_IDENTITY_FULL_NAME_ATTRIBUTE, "");
	}

	/**
	 * The attribute name which is used to match against the identity username.
	 * 
	 * @return identity name attribute
	 */
	public final String getIdentityNameAttribute() {
		return configurationParameters.getStringOrFail(DIRECTORY_IDENTITY_NAME_ATTRIBUTE);
	}
	
	/**
	 * The attribute name which is used to match against the identity description.
	 * 
	 * @return identity name attribute
	 */
	public final String getIdentityEmailAttribute() {
		return configurationParameters.getString(DIRECTORY_IDENTITY_EMAIL_ATTRIBUTE);
	}
	
	
	/**
	 * The attribute name which is used to match against the identity description.
	 * 
	 * @return identity name attribute
	 */
	public final String getIdentityMobileAttribute() {
		return configurationParameters.getString(DIRECTORY_IDENTITY_MOBILE_ATTRIBUTE);
	}
	
	
	/**
	 * The attribute name which is used to match against the identity guid.
	 * 
	 * @return identity guid attribute
	 */
	public final String getIdentityGuidAttribute() {
		return configurationParameters.getStringOrFail(DIRECTORY_IDENTITY_GUID_ATTRIBUTE);
	}

	/**
	 * The attribute name which is used to provide the identities primary role GUID.
	 * 
	 * @return identity role guid attribute
	 */
	public final String getIdentityRoleGuidAttribute() {
		return configurationParameters.getStringOrNull(DIRECTORY_IDENTITY_ROLE_GUID_ATTRIBUTE);
	}

	/**
	 * The attribute name which is used to provide the identities primary role name.
	 * 
	 * @return identity role name attribute
	 */
	public final String getIdentityRoleNameAttribute() {
		return configurationParameters.getStringOrNull(DIRECTORY_IDENTITY_ROLE_NAME_ATTRIBUTE);
	}

	/**
	 * The attribute name which is used to set the identity password.
	 * 
	 * @return password attribute
	 */
	public final String getIdentityPasswordAttribute() {
		return configurationParameters.getStringOrFail(DIRECTORY_IDENTITY_PASSWORD_ATTRIBUTE);
	}

	/**
	 * The attribute name which contains the distinguished name.
	 * 
	 * @return distinguished name attribute
	 */
	public final String getDistinguishedNameAttribute() {
		return configurationParameters.getStringOrDefault(DIRECTORY_DISTINGUISHED_NAME_ATTRIBUTE, "dn");
	}

	/**
	 * The type of password encoding used for this directory.
	 * 
	 * @return password encoding type
	 */
	public final String getIdentityPasswordEncoding() {
		return configurationParameters.getStringOrFail(DIRECTORY_IDENTITY_PASSWORD_ENCODING);
	}

	/**
	 * The class name required for an object to be considered a role.
	 * 
	 * @return role object class
	 */
	public final String getRoleObjectClass() {
		return configurationParameters.getStringOrFail(DIRECTORY_ROLE_OBJECT_CLASS);
	}

	/**
	 * The attribute name which is used to match against the role name.
	 * 
	 * @return role name attribute
	 */
	public final String getRoleNameAttribute() {
		return configurationParameters.getStringOrFail(DIRECTORY_ROLE_NAME_ATTRIBUTE);
	}

	/**
	 * The attribute name which is used to match against the role name guid.
	 * 
	 * @return role name guid attribute
	 */
	public final String getRoleGuidAttribute() {
		return configurationParameters.getStringOrFail(DIRECTORY_ROLE_GUID_ATTRIBUTE);
	}

	/**
	 * The attribute name which is used to indicate which group(s) the user or grup
	 * is a part of.
	 * 
	 * @return member of attribute
	 */
	public final String getMemberOfAttribute() {
		return configurationParameters.getStringOrFail(DIRECTORY_MEMBER_OF_ATTRIBUTE);
	}

	/**
	 * The attribute name which is used to indicate which users or groups in are a group.
	 * 
	 * @return member of attribute
	 */
	public final String getUniqueMemberAttribute() {
		return configurationParameters.getStringOrFail(DIRECTORY_UNIQUE_MEMBER_ATTRIBUTE);
	}

	public final Map<String, String> getConnectorConfigurationParameters() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());

		ArrayList<String> filteredControllerHosts = new ArrayList<String>();
		int hostCount = 0;
		for (String controllerHost : getControllerHosts()) {
			int idx;
			hostCount++;
			if ((idx = controllerHost.indexOf(':')) > -1) {
				int port = Integer.parseInt(controllerHost.substring(idx + 1));
				switch (port) {
				case 389:
				case 3268:
					if (hostCount > 1 && securityProtocol.equals(SSL)) {
						if (LOG.isWarnEnabled())
							LOG.warn("Multiple controller hosts with different protocols [PLAIN,SSL] are not allowed");
					} else {
						if (LOG.isWarnEnabled() && securityProtocol.equals(SSL))
							LOG.warn("Switching to PLAIN security protocol");
						securityProtocol = PLAIN;
						filteredControllerHosts.add(controllerHost);
					}
					break;
				case 636:
				case 3269:
					if (securityProtocol.equals(PLAIN)) {
						if (LOG.isWarnEnabled())
							LOG.warn("Multiple controller hosts with different protocols [PLAIN,SSL] are not allowed");
					} else {
						securityProtocol = SSL;
						filteredControllerHosts.add(controllerHost);
					}
					break;
				default:
					filteredControllerHosts.add(controllerHost);
				}
			}
		}

		configurationParameters.set(DIRECTORY_SECURITY_PROTOCOL, securityProtocol);

		variables.put(Context.PROVIDER_URL, buildProviderUrl(getSecurityProtocol().equalsIgnoreCase(SSL),
				filteredControllerHosts.toArray(new String[0])));
		variables.put(Context.SECURITY_PROTOCOL, securityProtocol);

		variables.put(Context.SECURITY_AUTHENTICATION, getServiceAuthenticationType());
		//
		if (SSL.equals(getSecurityProtocol())) {
			variables.put("java.naming.ldap.factory.socket", DummySSLSocketFactory.class.getName());
			// Add the custom socket factory
		}

		if (isFollowReferrals()) {
			variables.put(Context.REFERRAL, "follow");
		}

		variables.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(getTimeout()));
		variables.put("com.sun.jndi.ldap.read.timeout", String.valueOf(getReadTimeout()));
		variables.put("java.naming.ldap.version", getVersion());
		variables.put("com.sun.jndi.ldap.connect.pool", "true");
		variables.put("javax.security.sasl.qop", "auth-conf,auth-int,auth");

		variables.putAll(getInitialConfigurationParameters());
		return variables;
	}

	public String[] getProviderURLList() {
		List<String> l = new ArrayList<String>();
		boolean ssl = getSecurityProtocol().equalsIgnoreCase("ssl");
		for (String host : getControllerHosts()) {
			l.add(buildProviderUrl(ssl, host));
		}
		return l.toArray(new String[0]);
	}

	public String buildProviderUrl(boolean ssl, String... controllerHosts) {
		StringBuilder builder = new StringBuilder();
		for (String controllerHost : buildProviderUrls(controllerHosts)) {
			int idx;
			if ((idx = controllerHost.indexOf(':')) > -1) {
				int port = Integer.parseInt(controllerHost.substring(idx + 1));
				controllerHost = controllerHost.substring(0, idx);
				switch (port) {
				case 389:
				case 3268:
					builder.append(LDAP_PROTOCOL).append(controllerHost).append(":").append(port);
					break;
				case 636:
				case 3269:
					builder.append(LDAPS_PROTOCOL).append(controllerHost).append(":").append(port);
					break;
				default:
					builder.append(ssl ? LDAPS_PROTOCOL : LDAP_PROTOCOL).append(controllerHost).append(":")
							.append(port);
				}
			} else {
				builder.append(ssl ? LDAPS_PROTOCOL : LDAP_PROTOCOL).append(controllerHost).append(":")
						.append(ssl ? 636 : 389);
			}

		}
		return builder.toString().trim();
	}

	protected Map<String, String> getInitialConfigurationParameters() {
		return Collections.emptyMap();
	}

	/**
	 * @see Object#toString()
	 * @return
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(super.toString());
		buffer.append("[ControllerHost='" + getControllerHosts());
		buffer.append("', ServiceAuthenticationType='" + getServiceAuthenticationType());
		buffer.append("', SecurityProtocol='" + getSecurityProtocol());
		buffer.append("', InitialContextFactory='" + getInitialContextFactory());
		buffer.append("', ConnectTimeout='" + getTimeout());
		buffer.append("', ReadTimeout='" + getReadTimeout());
		buffer.append("', Version='" + getVersion());
		buffer.append("', BaseDn='" + getBaseDn());
		buffer.append("', ServiceAccountUsername='" + getServiceAccountDn());
		buffer.append("', ServiceAccountPassword='********");
		buffer.append("', IdentityObjectClass='" + getIdentityObjectClass());
		buffer.append("', IdentityNameAttribute='" + getIdentityNameAttribute());
		buffer.append("', IdentityGuidAttribute='" + getIdentityGuidAttribute());
		buffer.append("', IdentityPasswordAttribute='" + getIdentityPasswordAttribute());
		buffer.append("', IdentityPasswordEncoding='" + getIdentityPasswordEncoding());
		buffer.append("', RoleObjectClass='" + getRoleObjectClass());
		buffer.append("', RoleNameAttribute='" + getRoleNameAttribute());
		buffer.append("', RoleGuidAttribute='" + getRoleGuidAttribute()).append("']");
		return buffer.toString();
	}

	@Override
	public String getUsernameHint() {
		return getServiceAccountDn();
	}

	@Override
	public String getHostnameHint() {
		return configurationParameters.getStringOrNull(DIRECTORY_HOSTNAME);
	}

	public int getReadTimeout() {
		return configurationParameters.getIntegerOrDefault(DIRECTORY_READ_TIMEOUT, 120000);
	}

	public String getOU() {
		return configurationParameters.getString(DIRECTORY_USER_OU);
	}

	public boolean isFilteredByRolePrincipalName() {
		return getRoleMode().equals(RoleMode.principalNames)
				&& (!getIncludedRoles().isEmpty() || !getExcludedRoles().isEmpty());
	}

	public boolean isFilteredByRoleDistinguishedName() {
		return (getRoleMode().equals(RoleMode.distinguishedNames)
				|| getRoleMode().equals(RoleMode.serverDistinguishedNames))
				&& (!getIncludedRolesDN().isEmpty() || !getExcludedRolesDN().isEmpty());
	}

	public boolean isFilteredByRole() {
		return isEnableRoles() && (isFilteredByRoleDistinguishedName() || isFilteredByRolePrincipalName());
	}
}