/* HEADER */
package com.identity4j.connector.directory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.OperationNotSupportedException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.simple.AbstractParameterizedContextMapper;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import org.springframework.ldap.core.simple.SimpleLdapOperations;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.BrowseNode;
import com.identity4j.connector.BrowseableConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.StringUtil;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public class DirectoryConnector extends AbstractConnector implements BrowseableConnector {

	final static Log LOG = LogFactory.getLog(DirectoryConnector.class);
	/**
     */
	public static final String WILDCARD_SEARCH = "*";
	/**
     */
	public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

	private DirectoryConfiguration directoryConfiguration;
	private SimpleLdapOperations ldapTemplate;

	protected static Set<ConnectorCapability> capabilities = new HashSet<ConnectorCapability>(Arrays.asList(new ConnectorCapability[] { 
			ConnectorCapability.passwordChange,
			ConnectorCapability.passwordSet,
			ConnectorCapability.createUser,
			ConnectorCapability.deleteUser,
			ConnectorCapability.updateUser,
			ConnectorCapability.hasFullName,
			ConnectorCapability.hasEmail,
			ConnectorCapability.roles,
			ConnectorCapability.authentication,
			ConnectorCapability.identities,
			ConnectorCapability.tracksLastPasswordChange,
			ConnectorCapability.tracksLastSignOnDate,
	}));
	
	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}
	
	@Override
	public boolean isOpen() {
		try {
			ldapTemplate.lookupContext(directoryConfiguration.getBaseDn());
			return true;
		} catch (NamingException nme) {
			return false;
		}
	}

	@Override
	public boolean isReadOnly() {
		return directoryConfiguration.getSecurityProtocol().equals(DirectoryConfiguration.PLAIN);
	}

	protected final DirectoryConfiguration getConfiguration() {
		return directoryConfiguration;
	}

	protected final SimpleLdapOperations getLdapTemplate() {
		return ldapTemplate;
	}

	@Override
	protected final boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {
		try {
			DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
			SimpleLdapOperations ldapTemplate = getLdapTemplate(directoryIdentity, password);
			ldapTemplate.lookupContext(directoryIdentity.getDn());
			return true;
		} catch (AuthenticationException ae) {
			// http://stackoverflow.com/questions/2672125/what-does-sub-error-code-568-mean-for-ldap-error-49-with-active-directory
			DirectoryExceptionParser dep = new DirectoryExceptionParser(ae);
			if (dep.getData().equals("773")) {
				throw new PasswordChangeRequiredException();
			}
			return false;
		} catch (NamingException nme) {
			return false;
		}
	}

	protected SimpleLdapOperations getLdapTemplate(DirectoryIdentity directoryIdentity, char[] password) {
		return buildLdapTemplate(directoryIdentity.getDn().toString(), String.valueOf(password));
	}

	@Override
	protected void changePassword(Identity identity, char[] oldPassword, char[] password) {
		DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
		SimpleLdapOperations ldapTemplate = getLdapTemplate();
		setPassword(ldapTemplate, directoryIdentity, password, false);
	}

	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType type) throws ConnectorException {
		DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
		buildLdapTemplate();
		setPassword(ldapTemplate, directoryIdentity, password, forcePasswordChangeAtLogon);
	}

	protected void setPassword(SimpleLdapOperations ldapTemplate, DirectoryIdentity identity, char[] password,
			boolean forcePasswordChangeAtLogon) throws ConnectorException {

		try {
			byte[] encodedPassword = DefaultEncoderManager.getInstance().encode(password,
				directoryConfiguration.getIdentityPasswordEncoding(), "UTF-8", null, null);
			Attribute attribute = new BasicAttribute(directoryConfiguration.getIdentityPasswordAttribute(), encodedPassword);
			ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
			LdapOperations ldapOperations = ldapTemplate.getLdapOperations();
			ldapOperations.modifyAttributes(identity.getDn(), new ModificationItem[] { item });
			setForcePasswordChangeAtNextLogon(identity, forcePasswordChangeAtLogon);
		} catch (OperationNotSupportedException nme) {
			throw new ConnectorException("Failed to set password. Reason code " + processNamingException(nme) + ". Please see the logs for more detail.");
		}
	}
	
	protected String processNamingException(org.springframework.ldap.NamingException nme) {
		return getReason(nme);
	}

	protected void setForcePasswordChangeAtNextLogon(DirectoryIdentity identity, boolean forcePasswordChangeAtLogon) {
		// template
	}

	@Override
	public final Identity getIdentityByName(String identityName) throws PrincipalNotFoundException, ConnectorException {
		String identityFilter = buildIdentityFilter(identityName);
		return getPrincipal(identityFilter, getIdentities(identityFilter));
	}

	public final Iterator<Identity> allIdentities() throws ConnectorException {
		return getIdentities(buildIdentityFilter(WILDCARD_SEARCH));
	}

	@Override
	public long countIdentities() throws ConnectorException {
		// There is no method for generic LDAP so it's better to return nothing
		return -1;
	}

	@Override
	public long countRoles() throws ConnectorException {
		// There is no method for generic LDAP so it's better to return nothing
		return -1;
	}

	protected String buildIdentityFilter(String identityName) {
		String identityObjectClass = directoryConfiguration.getIdentityObjectClass();
		String identityNameAttribute = directoryConfiguration.getIdentityNameAttribute();
		return buildObjectClassFilter(identityObjectClass, identityNameAttribute, identityName);
	}

	protected Iterator<Identity> getIdentities(String filter) {
		return searchForResults(filter, new PrincipalContextMapper<Identity>() {
			@Override
			protected Identity mapFromContext(DirContextOperations result) {
				return mapIdentity(result);
			}
		}, configureSearchControls(new SearchControls()));
	}

	protected Identity mapIdentity(DirContextOperations result) {
		String guid = StringUtil.nonNull(result.getStringAttribute(directoryConfiguration.getIdentityGuidAttribute()));
		String identityName = StringUtil.nonNull(result.getStringAttribute(directoryConfiguration.getIdentityNameAttribute()));
		return new DirectoryIdentity(guid, identityName, result.getDn());
	}

	@Override
	public final Role getRoleByName(String roleName) throws PrincipalNotFoundException, ConnectorException {
		if (!getConfiguration().isEnableRoles()) {
			throw new PrincipalNotFoundException("Roles are not enabled");
		}
		String roleNameFilter = buildRoleFilter(roleName);
		return getPrincipal(roleNameFilter, getRoles(roleNameFilter));
	}

	public final Iterator<Role> allRoles() throws ConnectorException {
		if (!getConfiguration().isEnableRoles()) {
			return new ArrayList<Role>().iterator();
		}
		return getRoles(buildRoleFilter(WILDCARD_SEARCH));
	}

	private String buildRoleFilter(String roleName) {
		String roleObjectClass = directoryConfiguration.getRoleObjectClass();
		String roleNameAttribute = directoryConfiguration.getRoleNameAttribute();
		return buildObjectClassFilter(roleObjectClass, roleNameAttribute, roleName);
	}

	protected Iterator<Role> getRoles() {
		return getRoles(buildRoleFilter(WILDCARD_SEARCH));
	}
	protected Iterator<Role> getRoles(String filter) {
		return searchForResults(filter, new PrincipalContextMapper<Role>() {
			@Override
			protected Role mapFromContext(DirContextOperations result) {
				return mapRole(result);
			}
		}, configureRoleSearchControls(new SearchControls()));
	}

	protected Role mapRole(DirContextOperations result) {
		String guid = StringUtil.nonNull(result.getStringAttribute(directoryConfiguration.getRoleGuidAttribute()));
		String roleName = StringUtil.nonNull(result.getStringAttribute(directoryConfiguration.getRoleNameAttribute()));
		return new DirectoryRole(guid, roleName, result.getDn());
	}

	protected final String buildObjectClassFilter(String objectClass, String principalNameFilterAttribute, String principalName) {
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE, objectClass));
		andFilter.and(new LikeFilter(principalNameFilterAttribute, principalName));
		return andFilter.encode();
	}

	protected SearchControls configureSearchControls(SearchControls searchControls) {
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// searchControls.setCountLimit(0);
		searchControls.setReturningObjFlag(true);
		return searchControls;
	}

	protected SearchControls configureRoleSearchControls(SearchControls searchControls) {
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// searchControls.setCountLimit(0);
		searchControls.setReturningObjFlag(true);
		return searchControls;
	}

	protected final <T> Iterator<T> searchForResults(final String filter, final ParameterizedContextMapper<T> contextMapper,
			SearchControls searchControls) {
		final ParameterizedContextMapper<T> filteredMapper = new AbstractFilteredContextMapper<T>(
			directoryConfiguration.getIncludes(), directoryConfiguration.getExcludes()) {
			@Override
			protected T onMapFromContext(DirContextOperations ctx) {
				return contextMapper.mapFromContext(ctx);
			}
		};

		Collection<Name> dns = Collections.singleton(directoryConfiguration.getBaseDn());
		return new SearchResultsIterator<T>(filteredMapper, dns, filter, searchControls);
	}

	class SearchResultsIterator<T extends Object> implements Iterator<T> {

		private ParameterizedContextMapper<T> filteredMapper;
		private Name[] dns;
		private int dnIdx = 0;
		private T next;
		private PagedResultsCookie cookie;
		private Name dn;
		private String filter;
		private Iterator<T> listIterator;
		private SimpleLdapTemplate singleLdapTemplate;
		private SingleContextSource singleContextSource;
		private SearchControls searchControls;

		SearchResultsIterator(ParameterizedContextMapper<T> filteredMapper, Collection<? extends Name> dns, String filter,
				SearchControls searchControls) {
			ContextSource contextSource = buildContextSource(directoryConfiguration, directoryConfiguration.getServiceAccountDn(),
				directoryConfiguration.getServiceAccountPassword());
			singleContextSource = new SingleContextSource(contextSource.getReadOnlyContext());
			singleLdapTemplate = new SimpleLdapTemplate(singleContextSource);
			LdapTemplate ldapOperations = (LdapTemplate) singleLdapTemplate.getLdapOperations();
			ldapOperations.setIgnorePartialResultException(true);

			this.searchControls = searchControls;
			this.filteredMapper = filteredMapper;
			this.dns = dns.toArray(new Name[0]);
			this.filter = filter;
		}

		@Override
		public boolean hasNext() {
			fetchNext();
			if (next == null) {
				close();
			}
			return next != null;
		}

		private void close() {
			if (singleContextSource != null) {
				singleContextSource.destroy();
				singleContextSource = null;
			}
		}

		@Override
		public T next() {
			try {
				if (next == null) {
					fetchNext();
				}
				if (next == null) {
					close();
					throw new NoSuchElementException();
				}
				return next;
			} catch (Error e) {
				close();
				throw e;
			}
		}

		@Override
		public void remove() {
		}

		void fetchNext() {
			// If we haven't started on the next DN, get it
			next = null;
			while (next == null) {
				if (dn == null) {
					if (dnIdx >= dns.length) {
						// There are no more
						next = null;
						return;
					}
					dn = dns[dnIdx++];
					cookie = null;
				}

				while (next == null && dn != null) {
					// If there is no list iterator, create the quest and get
					// the
					// results
					if (listIterator == null) {
						PagedResultsDirContextProcessor control = new PagedResultsDirContextProcessor(
							directoryConfiguration.getMaxPageSize(), cookie);
						List<T> search = singleLdapTemplate.search(dn, filter, searchControls, filteredMapper, control);
						listIterator = search.iterator();
						cookie = control.getCookie();
					} else {
						// Get the next result, skipping nulls
						while (next == null && listIterator.hasNext()) {
							next = listIterator.next();
						}

						if (next == null) {
							// There are no more results in the current list,
							// skip
							// to the page result
							listIterator = null;

							if (cookie != null && cookie.getCookie() == null) {
								// Break out of this loop to get the next DN
								dn = null;
							}
						}
					}
				}
			}
		}

	}

	protected final String getAttributeValue(Name dn, String attributeName) {
		DirContextOperations context = ldapTemplate.lookupContext(dn);
		return context.getStringAttribute(attributeName);
	}

	protected final String getByteValue(String attributeName, DirContextOperations attributes) {
		byte[] objectGuid = (byte[]) attributes.getObjectAttribute(attributeName);
		if (objectGuid == null) {
			throw new IllegalArgumentException(attributeName + " cannot be null");
		}
		return StringUtil.convertByteToString(objectGuid);
	}

	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) {
		directoryConfiguration = (DirectoryConfiguration) parameters;
		try {
			ldapTemplate = buildLdapTemplate();
			Name baseDn = directoryConfiguration.getBaseDn();
			LOG.info("Looking up " + baseDn);
			ldapTemplate.lookupContext(baseDn);

		} catch (org.springframework.ldap.AuthenticationException ae) {
			throw new ConnectorException("Failed to authenticate. Check your username and password.", ae);
		} catch (NameNotFoundException nnfe) {
			LOG.error("Failed to open connector.", nnfe);
			throw new ConnectorException(
				"Failed to connect to directory because it appears your Base DN is incorrect. Check advanced configuration for this directory. ");
		} catch (NamingException nme) {
			nme.printStackTrace();
			DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
			String message = dep.getMessage();
			int code = dep.getCode();
			String reason = dep.getReason();
			if (code == 1 && reason.equals("000020D6")) {
				throw new ConnectorException("Connected OK, but the initial directory could not be read. Is your Base DN correct?");
			} else {
				LOG.error(
					"Connected OK, but an error occurred retrieving information from the directory server (operationsErrror). "
						+ message, nme);
				throw new ConnectorException("Failed to connect. " + message + ". Please see the logs for more detail.");
			}
		}
	}

	private SimpleLdapOperations buildLdapTemplate() {
		return buildLdapTemplate(directoryConfiguration.getServiceAccountDn(), directoryConfiguration.getServiceAccountPassword());
	}

	protected SimpleLdapOperations buildLdapTemplate(String serviceAccountDn, String serviceAccountPassword) {
		ContextSource contextSource = buildContextSource(directoryConfiguration, serviceAccountDn, serviceAccountPassword);
		SimpleLdapTemplate ldapTemplate = new SimpleLdapTemplate(new SingleContextSource(contextSource.getReadOnlyContext()));
		LdapTemplate ldapOperations = (LdapTemplate) ldapTemplate.getLdapOperations();
		ldapOperations.setIgnorePartialResultException(true);
		return ldapTemplate;
	}
	
	protected String getReason(NamingException nme) {
		/*
		 * This is a bit crap. There must be a better way of getting at the
		 * codes? Also, are they AD specific?
		 */
		String message = getMessage(nme);
		if (!StringUtil.isNullOrEmpty(message)) {
			final String string = "LDAP: error code ";
			int ldpx = message.indexOf(string);
			if (ldpx != -1) {
				String err = message.substring(ldpx + string.length());
				StringTokenizer t = new StringTokenizer(err);
				t.nextToken();
				t.nextToken();
				String reason = t.nextToken();
				while (reason.endsWith(":")) {
					reason = reason.substring(0, reason.length() - 1);
				}
				return reason;
			}
		}
		return "Unknown reason";
	}
	

	protected int getCode(NamingException nme) {
		/*
		 * This is a bit crap. There must be a better way of getting at the
		 * codes? Also, are they AD specific?
		 */
		String message = getMessage(nme);
		if (!StringUtil.isNullOrEmpty(message)) {
			final String string = "LDAP: error code ";
			int ldpx = message.indexOf(string);
			if (ldpx != -1) {
				String err = message.substring(ldpx + string.length());
				StringTokenizer t = new StringTokenizer(err);
				return Integer.parseInt(t.nextToken());
			}
		}
		return 0;
	}

	protected String getMessage(NamingException nme) {
		String message = nme.getExplanation();
		if (StringUtil.isNullOrEmpty(message)) {
			return StringUtil.isNullOrEmpty(nme.getMessage()) ? "No actual error message supplied." : nme.getMessage();
		}
		if (message.startsWith("[")) {
			message = message.substring(1);
		}
		if (message.endsWith("]")) {
			message = message.substring(0, message.length() - 1);
		}
		return message;
	}

	private ContextSource buildContextSource(DirectoryConfiguration configuration, String serviceAccountDn,
			String serviceAccountPassword) {

		/*
		 * NOTE
		 * 
		 * Check the LDAP hostname may be looked up by IP address. If this is
		 * not possible, LDAP queries will be very slow
		 */

		for (String controllerHost : configuration.getControllerHosts()) {
			String host = DirectoryConfiguration.getControllerHostWithoutPort(controllerHost);
			if (host.matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")) {
				try {
					InetAddress addr = InetAddress.getByName(host);
					if (addr.getHostName().equals(host)) {
						throw new UncategorizedLdapException("LDAP controller host address " + controllerHost
							+ " is not resolvable by a reverse DNS lookup. Please check your DNS configuration.");
					}
				} catch (UnknownHostException e) {
				}
			}
		}

		try {
			LdapContextSource contextSource = new LdapContextSource();
			// contextSource.setBase(configuration.getBaseDn().toString());
			contextSource.setPassword(serviceAccountPassword);
			contextSource.setUrls(configuration.getProviderURLList());
			contextSource.setUserDn(serviceAccountDn);
			contextSource.setBaseEnvironmentProperties(directoryConfiguration.getConnectorConfigurationParameters());
			contextSource.afterPropertiesSet();
			return contextSource;
		} catch (Exception expt) {
			throw new UncategorizedLdapException("Failed to build LdapTemplate", expt);
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
	public Iterator<BrowseNode> getBrowseableNodes(BrowseNode parent) {
		final SearchControls ctrls = new SearchControls();
		ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
		ctrls.setReturningObjFlag(true);
		ParameterizedContextMapper<List<BrowseNode>> s = new AbstractParameterizedContextMapper<List<BrowseNode>>() {
			@SuppressWarnings("serial")
			protected List<BrowseNode> doMapFromContext(DirContextOperations ctx) {
				List<BrowseNode> l = new ArrayList<BrowseNode>();
				final String[] stringAttributes = ctx.getStringAttributes("namingContexts");
				if (stringAttributes == null || stringAttributes.length == 0) {
					System.err.println("******* NO ROOT DSES");
				} else {
					for (final String string : stringAttributes) {
						l.add(new BrowseNode() {
							@Override
							public boolean isLeaf() {
								return false;
							}

							public String toString() {
								return string;
							}

						});
					}
				}
				return l;
			}
		};
		// Collection<? extends Name> dns = Collections.singleton(new
		// DistinguishedName(""));
		// Iterator<List<BrowseNode>> nodes = new
		// SearchResultsIterator<List<BrowseNode>>(s, dns, "(objectclass=*)",
		// ctrls);
		// return nodes.hasNext() ? nodes.next().iterator() : new
		// ArrayList<BrowseNode>().iterator();

		Iterator<List<BrowseNode>> nodes = ldapTemplate.search("", "(objectclass=*)", ctrls, s, null).iterator();
		return nodes.hasNext() ? nodes.next().iterator() : new ArrayList<BrowseNode>().iterator();

	}
}