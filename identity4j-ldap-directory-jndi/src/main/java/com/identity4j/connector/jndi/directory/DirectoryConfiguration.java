/* HEADER */
package com.identity4j.connector.jndi.directory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.connector.util.DummySSLSocketFactory;
import com.identity4j.util.MultiMap;

/**
 */
public class DirectoryConfiguration extends AbstractConnectorConfiguration {


	static Log LOG = LogFactory.getLog(DirectoryConfiguration.class);

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
	/**
	 * Configuration property key for excludes
	 */
	public static final String DIRECTORY_EXCLUDES = "directory.excludes";
	/**
	 * Configuration property key for excludes
	 */
	public static final String DIRECTORY_INCLUDES = "directory.includes";
	
	/**
	 * Configuration property key for role reconcilliation
	 */
	public static final String DIRECTORY_ENABLE_ROLES = "directory.enableRoles";
	
	/**
	 * Follow referrals?
	 */
	public static final String DIRECTORY_FOLLOW_REFERRALS = "directory.followReferrals";
	
	/**
     */
	public static final char PORT_SEPARATOR = ':';
	
	/**
     */
	public static final String COMMON_NAME = "CN=";

	/**
	 * Constant for value of {@link #DIRECTORY_SECURITY_PROTOCOL} when SSL
	 * should be used
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
	private final boolean enableRoles;

	String securityProtocol = SSL;

	/**
	 * @param configurationParameters
	 */
    public DirectoryConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
		try {
			baseDn = new LdapName(configurationParameters.getStringOrDefault(DIRECTORY_BASE_DN, ""));
		
			includes = getNames(configurationParameters.getStringArrayOrDefault(DIRECTORY_INCLUDES));
			excludes = getNames(configurationParameters.getStringArrayOrDefault(DIRECTORY_EXCLUDES));
			includes.removeAll(excludes);
			securityProtocol = configurationParameters.getStringOrDefault(DIRECTORY_SECURITY_PROTOCOL, SSL);
			enableRoles = configurationParameters.getBooleanOrDefault(DIRECTORY_ENABLE_ROLES, false);
			if (includes.isEmpty()) {
				includes.add(baseDn);
			}
		} catch (NamingException ne) {
			throw new Error(ne);
		}
	}

	/**
	 * <p>
	 * The host name or IP address of the directory to connect to. If an IP
	 * address is used this should be in dotted decimal notation. Otherwise the
	 * fully qualified hostname should be specified in the standard dns format
	 * </p>
	 * <p>
	 * Examples: <code>192.168.1.200:443</code>, <code>192.168.1.200</code> or
	 * <code>host.directory.com</code>
	 * </p>
	 * 
	 * @return controller hosts
	 */
	public final String[] getControllerHosts() {
		List<String> l = new ArrayList<String>(Arrays.asList(configurationParameters.getStringArrayOrFail(DIRECTORY_HOSTNAME)));
		String[] tmp = configurationParameters.getStringArrayOrFail(DIRECTORY_BACKUP_HOSTNAMES);
		for(String t : tmp) {
			if(StringUtils.isNotBlank(t)) {
				l.add(t);
			}
		}
		return l.toArray(new String[0]);
	}
	
	/**
	 * <p>
	 * The host name or IP address of the directory to connect to, without the port
	 * number if one is set. If an IP
	 * address is used this should be in dotted decimal notation. Otherwise the
	 * fully qualified hostname should be specified in the standard dns format
	 * </p>
	 * <p>
	 * Examples: <code>192.168.1.200</code> or
	 * <code>host.directory.com</code>
	 * </p>
	 * 
	 * @return controller hosts
	 */
	public final String[] getControllerHostnames() {
		List<String> l = new ArrayList<String>();
		for(String h : getControllerHosts()) {
			l.add(getControllerHostWithoutPort(h));
		}
		return l.toArray(new String[0]);
	}

	/**
	 * Get a list of distinguished names to exclude from the search. These are
	 * relative to the Base DN. If the list is empty, all paths should be
	 * included unless explicit excludes have been set.
	 * 
	 * @return paths to exclude
	 * @throws InvalidNameException
	 */
	public Collection<Name> getIncludes() {
		return includes;
	}

	/**
	 * Get a list of distinguished names to exclude from the search. These are
	 * relative to the Base DN. If the list is empty, all paths should be
	 * included unless explicit includes have been set.
	 * 
	 * @return paths to exclude
	 * @throws InvalidNameException
	 */
	public Collection<Name> getExcludes() {
		return excludes;
	}
	
	/**
	 * Get if roles should be enabled at all.
	 * 
	 * @return reconcile roles
	 */
	public boolean isEnableRoles() {
		return enableRoles;
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
	 * The security protocol to use, this defaults to SSL. This value is used
	 * for the <code>javax.naming.Context.SECURITY_PROTOCOL</code> parameter.
	 * 
	 * @return security protocol
	 */
	public final String getSecurityProtocol() {
		return securityProtocol;
	}

	/**
	 * Should referrals be followed. The Manage Referral control <a
	 * href=http://www.ietf.org/rfc/rfc3296.txt">(RFC 3296)</a> tells the LDAP
	 * server to return referral entries as ordinary entries (instead of
	 * returning "referral" error responses or continuation references). If you
	 * are using the LDAP v3 and have set Context.REFERRAL to "ignore", then the
	 * LDAP service provider will automatically send this control along with the
	 * request. If you are using the LDAP v2, then the control will not be sent
	 * because it is not applicable in that protocol. When you set
	 * Context.REFERRAL to any other value, the control will not be sent
	 * regardless of the protocol version.
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
		return configurationParameters.getStringOrDefault("directory.initialContextFactory", "com.sun.jndi.ldap.LdapCtxFactory");
	}

	/**
	 * The value to use for <code>com.sun.jndi.ldap.connect.timeout</code>, the
	 * default is 30.
	 * 
	 * @return initial context factory
	 */
	public final int getTimeout() {
		return configurationParameters.getIntegerOrDefault("directory.timeout", Integer.valueOf(30)) * 1000;
	}

	/**
	 * The maximum size of the page to return when using paged queries.
	 * 
	 * @return maximum page size
	 */
	public int getMaxPageSize() {
		return configurationParameters.getIntegerOrDefault("directory.maxPageSize", 1000);
	}

	/**
	 * The value to use for <code>java.naming.ldap.version</code>, the default
	 * is 3.
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

	private Collection<Name> getNames(String... values) throws InvalidNameException {
		Collection<Name> names = new ArrayList<Name>();
		for (String value : values) {
			if(StringUtils.isNotBlank(value)) {
				names.add(new LdapName(value).addAll(0, getBaseDn()));
			}
		}
		return names;
	}

	/**
	 * <p>
	 * The connector performs all operations on the directory using this
	 * account. The distinguished name of this account should be supplied in
	 * LDAP format, that is, with the Common Name (cn) of the account first
	 * followed by the container in which this account resides, then that
	 * container's container etc. The elements of the distinguished name should
	 * be separated using commas.
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
	 * {@link DirectoryConfiguration#getServiceAccountDn()}
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
		return configurationParameters.getStringOrFail("directory.identityObjectClass");
	}

	/**
	 * The attribute name which is used to match against the identity username.
	 * 
	 * @return identity name attribute
	 */
	public final String getIdentityNameAttribute() {
		return configurationParameters.getStringOrFail("directory.identityNameAttribute");
	}

	/**
	 * The attribute name which is used to match against the identity guid.
	 * 
	 * @return identity guid attribute
	 */
	public final String getIdentityGuidAttribute() {
		return configurationParameters.getStringOrFail("directory.identityGuidAttribute");
	}

	/**
	 * The attribute name which is used to set the identity password.
	 * 
	 * @return password attribute
	 */
	public final String getIdentityPasswordAttribute() {
		return configurationParameters.getStringOrFail("directory.identityPasswordAttribute");
	}

	/**
	 * The type of password encoding used for this directory.
	 * 
	 * @return password encoding type
	 */
	public final String getIdentityPasswordEncoding() {
		return configurationParameters.getStringOrFail("directory.identityPasswordEncoding");
	}

	/**
	 * The class name required for an object to be considered a role.
	 * 
	 * @return role object class
	 */
	public final String getRoleObjectClass() {
		return configurationParameters.getStringOrFail("directory.roleObjectClass");
	}

	/**
	 * The attribute name which is used to match against the role name.
	 * 
	 * @return role name attribute
	 */
	public final String getRoleNameAttribute() {
		return configurationParameters.getStringOrFail("directory.roleNameAttribute");
	}

	/**
	 * The attribute name which is used to match against the role name guid.
	 * 
	 * @return role name guid attribute
	 */
	public final String getRoleGuidAttribute() {
		return configurationParameters.getStringOrFail("directory.roleGuidAttribute");
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
					if (LOG.isWarnEnabled())
						LOG.warn("Unexpected LDAP port in controller host " + controllerHost);
					filteredControllerHosts.add(controllerHost);
				}
			}
		}

		configurationParameters.set(DIRECTORY_SECURITY_PROTOCOL, securityProtocol);

		variables.put(Context.PROVIDER_URL,
			buildProviderUrl(getSecurityProtocol().equalsIgnoreCase(SSL), filteredControllerHosts.toArray(new String[0])));
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
					LOG.warn("Unexpected LDAP port in controller host " + controllerHost);
					builder.append(ssl ? LDAPS_PROTOCOL : LDAP_PROTOCOL).append(controllerHost).append(":").append(port);
				}
			} else {
				builder.append(ssl ? LDAPS_PROTOCOL : LDAP_PROTOCOL).append(controllerHost).append(":").append(ssl ? 636 : 389);
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
		buffer.append("', Timeout='" + getTimeout());
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
}