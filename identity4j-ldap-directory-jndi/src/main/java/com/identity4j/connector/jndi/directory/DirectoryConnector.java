/* HEADER */
package com.identity4j.connector.jndi.directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.AbstractConnector;
import com.identity4j.connector.BrowseNode;
import com.identity4j.connector.BrowseableConnector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ConnectorConfigurationParameters;
import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PrincipalNotFoundException;
import com.identity4j.connector.jndi.directory.LdapService.ResultMapper;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.CollectionUtil;
import com.identity4j.util.StringUtil;

public class DirectoryConnector extends AbstractConnector implements BrowseableConnector {

	protected static final Iterator<Identity> IDENTITY_ITERATOR = CollectionUtil.emptyIterator(Identity.class);
	protected static final Iterator<Role> ROLE_ITERATOR = CollectionUtil.emptyIterator(Role.class);
	
	final static Log LOG = LogFactory.getLog(DirectoryConnector.class);
	/**
     */
	public static final String WILDCARD_SEARCH = "*";
	/**
     */
	public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

	private DirectoryConfiguration directoryConfiguration;
	protected LdapService ldapService;
	

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
			ldapService = new LdapService();
			ldapService.init(directoryConfiguration);
			ldapService.openConnection();
			return true;
		} catch(ConnectorException ex) { 
			throw ex;
		} catch (Exception nme) {
			LOG.error("Problen in open connection check.", nme);
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

	

	@Override
	protected final boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {

		DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
		try {
			return ldapService.authenticate(directoryIdentity.getDn().toString(), new String(password));
		} catch (IOException e) {
			return false;
		}
	}

	

	@Override
	protected void changePassword(Identity identity, char[] oldPassword, char[] password) {
		DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
		try {
			ldapService.setPassword(directoryIdentity.getDn().toString(), password);
		} catch (NamingException e) {
			LOG.error("Problem in changing password.", e);
		} catch (IOException e) {
			LOG.error("Problem in changing password.", e);
		}
	}

	@Override
	protected void setPassword(Identity identity, char[] password, boolean forcePasswordChangeAtLogon, PasswordResetType type) throws ConnectorException {
		DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
		try {
			ldapService.setPassword(directoryIdentity.getDn().toString(), password);
		} catch (NamingException e) {
			LOG.error("Problem in getting identities.", e);
		} catch (IOException e) {
			LOG.error("Problem in getting identities.", e);
		}
	}

	public Iterator<DirectoryOU> getOrganizationalUnits() throws NamingException, IOException {
		return ldapService.search(ldapService.buildObjectClassFilter(
				"organizationalUnit", "ou", WILDCARD_SEARCH), new ResultMapper<DirectoryOU>() {

			@Override
			public DirectoryOU apply(SearchResult result) throws NamingException {
				return new DirectoryOU((String)result.getAttributes().get("distinguishedName").get(),
						(String)result.getAttributes().get("ou").get());
			}
			public boolean isApplyFilters() {
				return true;
			}
		});
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
		return ldapService.buildObjectClassFilter(identityObjectClass, identityNameAttribute, identityName);
	}

	protected Iterator<Identity> getIdentities(String filter) {
		try {
			return ldapService.search(filter, new ResultMapper<Identity>() {

				public Identity apply(SearchResult result) throws NamingException {
					return mapIdentity(result);
				}
				public boolean isApplyFilters() {
					return true;
				}
			});
		} catch (NamingException e) {
			LOG.error("Problem in getting identities.", e);
		} catch (IOException e) {
			LOG.error("Problem in getting identities.", e);
		}
		
		return IDENTITY_ITERATOR;
	}

	protected Identity mapIdentity(SearchResult result) throws NamingException {
		String guid = StringUtil.nonNull(result.getAttributes().get(directoryConfiguration.getIdentityGuidAttribute()).get().toString());
		String identityName = StringUtil.nonNull(result.getAttributes().get(directoryConfiguration.getIdentityNameAttribute()).get().toString());
		LdapName dn = new LdapName(StringUtil.nonNull(result.getAttributes().get("distinguishedName").get().toString()));
		return new DirectoryIdentity(guid, identityName,dn);
	}

	@Override
	public final Role getRoleByName(String roleName) throws PrincipalNotFoundException, ConnectorException {
		if (!getConfiguration().isEnableRoles()) {
			throw new PrincipalNotFoundException("Roles are not enabled");
		}
		String roleNameFilter = buildRoleFilter(roleName,false);
		return getPrincipal(roleNameFilter, getRoles(roleNameFilter));
	}

	public final Iterator<Role> allRoles() throws ConnectorException {
		if (!getConfiguration().isEnableRoles()) {
			return ROLE_ITERATOR;
		}
		return getRoles(buildRoleFilter(WILDCARD_SEARCH,true));
	}

	private String buildRoleFilter(String roleName,boolean isWildcard) {
		String roleObjectClass = directoryConfiguration.getRoleObjectClass();
		String roleNameAttribute = directoryConfiguration.getRoleNameAttribute();
		return ldapService.buildObjectClassFilter(roleObjectClass, roleNameAttribute, roleName);
	}
	
	protected Iterator<Role> getRoles() {
		return getRoles(buildRoleFilter(WILDCARD_SEARCH,true));
	}
	
	protected Iterator<Role> getRoles(String filter) {
		try {
			return ldapService.search(filter, new ResultMapper<Role>() {

				public Role apply(SearchResult result) throws NamingException {
					return mapRole(result);
				}
				
				public boolean isApplyFilters() {
					return true;
				}
			});
		} catch (NamingException e) {
			LOG.error("Problem in getting roles.", e);
		} catch (IOException e) {
			LOG.error("Problem in getting roles.", e);
		}
		return ROLE_ITERATOR;
	}

	protected Role mapRole(SearchResult result) throws NamingException {
		String guid = StringUtil.nonNull(result.getAttributes().get(directoryConfiguration.getRoleGuidAttribute()).get().toString());
		String identityName = StringUtil.nonNull(result.getAttributes().get(directoryConfiguration.getRoleNameAttribute()).get().toString());
		LdapName dn = new LdapName(StringUtil.nonNull(result.getAttributes().get("distinguishedName").get().toString()));
		return new DirectoryRole(guid, identityName,dn);
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

	

	

	protected final String getAttributeValue(Name dn, String attributeName) {
		try {
			Attributes attributes = ldapService.lookupContext(dn);
			return attributes.get(attributeName) != null ?  attributes.get(attributeName).get().toString() : null;
		} catch (NamingException e) {
			LOG.error("Problem in getting attribute value.", e);
		} catch (IOException e) {
			LOG.error("Problem in getting attribute value.", e);
		}
		return null;
	}

	protected final String getByteValue(String attributeName, Attributes attributes) {
		try {
			byte[] objectGuid = (byte[]) attributes.get(attributeName).get();
			if (objectGuid == null) {
				throw new IllegalArgumentException(attributeName + " cannot be null");
			}
			return StringUtil.convertByteToString(objectGuid);
		} catch (NamingException e) {
			throw new ConnectorException(e);
		}
	}

	@Override
	protected void onOpen(ConnectorConfigurationParameters parameters) {
		directoryConfiguration = (DirectoryConfiguration) parameters;
		
		try {
			ldapService = new LdapService();
			ldapService.init(directoryConfiguration);
			ldapService.openConnection();
			Name baseDn = directoryConfiguration.getBaseDn();
			LOG.info("Looking up " + baseDn);
			
		} catch(NamingException nme){
			processNamingException(nme);
		} catch (Exception e) {
			LOG.error("Problem in opening connector.", e);
			throw new ConnectorException(e);
		}
	}

	protected String processNamingException(NamingException nme) {
		DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
		String message = dep.getMessage();
		LOG.error(
			"Connected OK, but an error occurred retrieving information from the directory server (operationsErrror). "
				+ message, nme);
		throw new ConnectorException(message);

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

	@Override
	public Iterator<BrowseNode> getBrowseableNodes(BrowseNode parent) {
		final SearchControls ctrls = new SearchControls();
		ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
		ctrls.setReturningObjFlag(true);
		
		try {
			 Iterator<List<BrowseNode>> nodes = ldapService.search(new LdapName(parent.toString()),"(objectclass=*)", new ResultMapper<List<BrowseNode>>() {

				@SuppressWarnings("serial")
				public List<BrowseNode> apply(SearchResult result) throws NamingException {
					
					Attribute namingContexts = result.getAttributes().get("namingContexts");
					if(namingContexts == null) return Collections.emptyList();
					
			        @SuppressWarnings("rawtypes")
					final NamingEnumeration enumeration = namingContexts.getAll();
					
			        List<BrowseNode> l = new ArrayList<BrowseNode>();
			        while(enumeration.hasMore()) { 
			        	final String node = (String) enumeration.next();
			            l.add(new BrowseNode() {
							@Override
							public boolean isLeaf() {
								return false;
							}

							public String toString() {
								return node;
							}

						});
			        }
			        return l;
					
				}
				
				public boolean isApplyFilters() {
					return true;
				}
			});
			 
			 return nodes.hasNext() ? nodes.next().iterator() : new ArrayList<BrowseNode>().iterator(); 
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
		
		
		// Collection<? extends Name> dns = Collections.singleton(new
		// DistinguishedName(""));
		// Iterator<List<BrowseNode>> nodes = new
		// SearchResultsIterator<List<BrowseNode>>(s, dns, "(objectclass=*)",
		// ctrls);
		// return nodes.hasNext() ? nodes.next().iterator() : new
		// ArrayList<BrowseNode>().iterator();

		//Iterator<List<BrowseNode>> nodes = ldapTemplate.search("", "(objectclass=*)", ctrls, s, null).iterator();
		//return nodes.hasNext() ? nodes.next().iterator() : new ArrayList<BrowseNode>().iterator();
		//return null;

	}

}