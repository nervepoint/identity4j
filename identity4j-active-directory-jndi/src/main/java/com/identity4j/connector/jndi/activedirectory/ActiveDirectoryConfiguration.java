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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.identity4j.connector.Connector;
import com.identity4j.connector.jndi.directory.AbstractDirectoryConfiguration;
import com.identity4j.util.MultiMap;
import com.identity4j.util.MultiMapException;
import com.identity4j.util.StringUtil;
import com.identity4j.util.validator.IpAddressValidator;

public class ActiveDirectoryConfiguration extends AbstractDirectoryConfiguration {

	public static final String CN_USERS = "CN=Users";
	public static final String CN_BUILTIN = "CN=Builtin";
	private static final String USE_GLOBAL_CATALOG = "directory.isGlobalCatalog";
	private static final String CHILD_DOMAIN_CONTROLLERS = "directory.childDomainControllers";

	public static final String ACTIVE_DIRECTORY_AUTHENTICATION = "activeDirectory.authenticationType";
	public static final String ACTIVE_DIRECTORY_CACHE_FILTERED_GROUPS = "activeDirectory.cachedFilteredGroups";
	public static final String ACTIVE_DIRECTORY_INCLUDE_DEFAULT_USERS = "activeDirectory.includeDefaultUsers";
	public static final String ACTIVE_DIRECTORY_INCLUDE_BUILTN_GROUPS = "activeDirectory.includeBuiltInGroups";
	public static final String ACTIVE_DIRECTORY_USERNAME_IS_SAMACCOUNTNAME = "activeDirectory.usernameSamAccountName";
	public static final String ACTIVE_DIRECTORY_GROUP_IS_SAMACCOUNTNAME = "activeDirectory.groupSamAccountName";
	
	@Deprecated
	public static final String ACTIVE_DIRECTORy_ENFORCE_PASSWORD_RULES = "activeDirectory.enforcePasswordRules";
	
	public static final String ACTIVE_DIRECTORY_ENFORCE_PASSWORD_RULES = "activeDirectory.enforcePasswordRules";
	
	public ActiveDirectoryConfiguration(MultiMap configurationParameters) {
		super(addConfigurationParameters(configurationParameters));
	}

	private static MultiMap addConfigurationParameters(
			MultiMap configurationParameters) {
		String baseDn = configurationParameters.getString(DIRECTORY_BASE_DN);
		if (StringUtil.isNullOrEmpty(baseDn)) {
			configurationParameters.set(DIRECTORY_BASE_DN,
					getBaseDN(configurationParameters));
		}

		String username = buildUsername(configurationParameters);
		configurationParameters.set(DIRECTORY_SERVICE_ACCOUNT_USERNAME,
				username);

		setIncludeBuiltInGroups(configurationParameters);
		setIncludeDefaultUsers(configurationParameters);

		configurationParameters.set("directory.identityObjectClass", "user");
		configurationParameters.set("directory.identityNameAttribute",
				"samAccountName");
		configurationParameters.set("directory.identityGuidAttribute",
				"objectGUID");
		configurationParameters.set("directory.identityPasswordAttribute",
				"unicodePwd");
		configurationParameters.set("directory.identityPasswordEncoding",
				"unicode");
		configurationParameters.set("directory.roleObjectClass", "group");
		configurationParameters.set("directory.roleNameAttribute",
				"cn");
		configurationParameters
				.set("directory.roleGuidAttribute", "objectGUID");
		configurationParameters.set("direcctory.identityCreationObjectClasses", "user");
		configurationParameters.set("directory.distinguishedNameAttribute", "distinguishedName");
		return configurationParameters;
	}
	
	public boolean isIncludeBuiltInGroups() {
		final List<String> includes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_INCLUDES));
		final List<String> excludes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_EXCLUDES));
		return ( includes.contains(CN_BUILTIN) || configurationParameters.getBoolean(ACTIVE_DIRECTORY_INCLUDE_BUILTN_GROUPS) ) && !excludes.contains(CN_BUILTIN);
	}
	
	public boolean isIncludeDefaultUsers() {
		final List<String> includes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_INCLUDES));
		final List<String> excludes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_EXCLUDES));
		return ( configurationParameters.getBoolean(ACTIVE_DIRECTORY_INCLUDE_DEFAULT_USERS) || includes.contains(CN_USERS) ) && !excludes.contains(CN_USERS);
	}

	private static void setIncludeBuiltInGroups(MultiMap configurationParameters) {
		final List<String> includes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_INCLUDES));
		final List<String> excludes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_EXCLUDES));
		if (!includes.contains(CN_BUILTIN) && !excludes.contains(CN_BUILTIN)) {
			if (configurationParameters.getBoolean(ACTIVE_DIRECTORY_INCLUDE_BUILTN_GROUPS)) {
				if (configurationParameters.getStringArray(DIRECTORY_INCLUDES).length != 0) {
					configurationParameters.setMore(DIRECTORY_INCLUDES, CN_BUILTIN);
				}
			} else {
				configurationParameters.setMore(DIRECTORY_EXCLUDES, CN_BUILTIN);
			}
		}
	}

	private static void setIncludeDefaultUsers(MultiMap configurationParameters) {
		final List<String> includes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_INCLUDES));
		final List<String> excludes = Arrays.asList(configurationParameters
				.getStringArray(DIRECTORY_EXCLUDES));
		if (!includes.contains(CN_USERS) && !excludes.contains(CN_USERS)) {
			if (configurationParameters.getBoolean(ACTIVE_DIRECTORY_INCLUDE_DEFAULT_USERS)) {
				if (configurationParameters.getStringArray(DIRECTORY_INCLUDES).length != 0) {
					configurationParameters.setMore(DIRECTORY_INCLUDES, CN_USERS);
				}
			} else {
				configurationParameters.setMore(DIRECTORY_EXCLUDES, CN_USERS);
			}
		}
	}

	private static String buildDefaultBaseDn(String domain) {
		StringBuilder buffer = new StringBuilder();
		for (String token : domain.split("\\.")) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append("DC=" + token);
		}
		return buffer.toString();
	}

	protected static String buildUsername(MultiMap configurationParameters) {
		String domain = getDomain(configurationParameters);
		String baseDn = configurationParameters
				.getStringOrFail(DIRECTORY_BASE_DN);
		return buildUsername(baseDn, domain,
				configurationParameters
						.getStringOrFail(DIRECTORY_SERVICE_ACCOUNT_USERNAME));
	}

	protected static String buildUsername(String baseDn, String domain,
			String username) {
		if (username.toUpperCase().startsWith(COMMON_NAME)) {
			boolean containsBaseDn = username.toUpperCase().endsWith(
					baseDn.toUpperCase());
			if (containsBaseDn) {
				return username;
			}
			return username.endsWith(",") ? username + baseDn : username + ","
					+ baseDn;
		}
		if (StringUtil.isNullOrEmpty(domain)
				|| username.toUpperCase().endsWith(domain.toUpperCase())) {
			return username;
		}
		return username + "@" + domain.toLowerCase();
	}

	public final int getMaxPasswordAgeDays() {
		return configurationParameters.getIntegerOrDefault(
				"activeDirectory.maxPwdAge", 0);
	}

	public final int getMinPasswordAgeDays() {
		return configurationParameters.getIntegerOrDefault(
				"activeDirectory.minPwdAge", 0);
	}

	public final int getPasswordHistoryLength() {
		return configurationParameters.getIntegerOrDefault(
				"activeDirectory.pwdHistoryLength", 0);
	}

	public final String[] getChildDomainControllers() {
		return configurationParameters
				.getStringArrayOrFail(CHILD_DOMAIN_CONTROLLERS);
	}

	public String[] getProviderURLList(boolean useGlobalCatalog) {
		List<String> l = new ArrayList<String>();
		boolean ssl = getSecurityProtocol().equalsIgnoreCase("ssl");
		for (String host : getControllerHosts()) {
			l.add(buildProviderUrl(ssl, useGlobalCatalog, host));
		}
		return l.toArray(new String[0]);
	}

	public String buildProviderUrl(boolean ssl, String... controllerHosts) {
		return buildProviderUrl(ssl,
				configurationParameters.getBooleanOrDefault(USE_GLOBAL_CATALOG,
						false), controllerHosts);
	}

	public String buildProviderUrl(boolean ssl, boolean useGlobalCatalogPort,
			String... controllerHosts) {
		StringBuilder builder = new StringBuilder();
		for (String controllerHost : buildProviderUrls(controllerHosts)) {
			int idx;
			if ((idx = controllerHost.indexOf(':')) > -1) {
				int port = Integer.parseInt(controllerHost.substring(idx + 1));
				switch (port) {
				case 389:
				case 3268:
					builder.append(LDAP_PROTOCOL).append(controllerHost)
							.append(":").append(port);
					break;
				case 636:
				case 3269:
					builder.append(LDAPS_PROTOCOL).append(controllerHost)
							.append(":").append(port);
					break;
				default:
					builder.append(ssl ? LDAPS_PROTOCOL : LDAP_PROTOCOL)
							.append(controllerHost).append(":").append(port);
				}
			} else {
				builder.append(ssl ? LDAPS_PROTOCOL : LDAP_PROTOCOL).append(
						controllerHost);
				if (useGlobalCatalogPort) {
					builder.append(":" + (ssl ? 3269 : 3268));
				} else {
					builder.append(":").append(ssl ? 636 : 389);
				}

				builder.append(" ");
			}

		}
		return builder.toString().trim();
	}

	public String getBaseDN() {
		return getBaseDN(configurationParameters);
	}

	public String getDomain() {
		return getDomain(configurationParameters);
	}

	private static String getBaseDN(MultiMap configurationParameters) {
		String baseDn = configurationParameters.getString(DIRECTORY_BASE_DN);
		if (StringUtil.isNullOrEmpty(baseDn)) {
			String domain = getDomain(configurationParameters);
			if (StringUtil.isNullOrEmpty(domain)) {
				throw new MultiMapException("Missing value for attribute '"
						+ DIRECTORY_BASE_DN + "'.");
			}
			return buildDefaultBaseDn(domain);
		} else {
			return baseDn;
		}
	}

	private static String getDomain(MultiMap configurationParameters) {
		String[] domains = configurationParameters.getStringArrayOrDefault(DIRECTORY_DOMAIN);
		if (domains.length > 0 && !domains[0].equals("")) {
			return domains[0];
		} else {
			String baseDn = configurationParameters.getString(DIRECTORY_BASE_DN);
			String[] hostNames = configurationParameters.getStringArrayOrFail(DIRECTORY_HOSTNAME);
			if(hostNames.length == 0) {
				throw new MultiMapException("Missing value for " + DIRECTORY_HOSTNAME);
			}
			String hostName = getControllerHostWithoutPort(hostNames[0]);
			if (IpAddressValidator.isHostName(hostName)) {
				int indexOf = hostName.indexOf('.');
				return indexOf == -1 ? "" : hostName.substring(indexOf + 1, hostName.length());
			} else if (!StringUtil.isNullOrEmpty(baseDn)) {
				/* NOTE: This should probably be used in preference to the hostname, but this
				 * preserves backwards compatibility
				 */
				StringBuilder b = new StringBuilder();
				String[] p = baseDn.split(",");
				for (String i : p) {
					i = i.trim();
					if(i.startsWith("DC=")) {
						i = i.substring(3);
						if(b.length() > 0)
							b.append('.');
						b.append(i);
					}
				}
				return b.toString();
			}
		}
		return "";
	}

	@Override
	protected Map<String, String> getInitialConfigurationParameters() {
		return Collections.singletonMap("java.naming.ldap.attributes.binary",
				"objectSID objectGUID");
	}
	
	public boolean isCacheFilteredGroups() {
		return getConfigurationParameters().getBooleanOrDefault(ACTIVE_DIRECTORY_CACHE_FILTERED_GROUPS, true);
	}

	
	public boolean isPasswordRulesEnforced() {
		return getConfigurationParameters().getBoolean(ACTIVE_DIRECTORy_ENFORCE_PASSWORD_RULES);
	}
	
	public boolean isUsernameSamAccountName() {
		return configurationParameters.getBooleanOrDefault(
				ACTIVE_DIRECTORY_USERNAME_IS_SAMACCOUNTNAME, Boolean.FALSE);
	}
	
	public boolean isGroupSamAccountName() {
		return configurationParameters.getBooleanOrDefault(
				ACTIVE_DIRECTORY_GROUP_IS_SAMACCOUNTNAME, Boolean.FALSE);
	}

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return ActiveDirectoryConnector.class;
	}

}