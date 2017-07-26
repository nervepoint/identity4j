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
import javax.net.SocketFactory;

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

	public static final String WILDCARD_SEARCH = "*";
	public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

	private DirectoryConfiguration directoryConfiguration;
	protected LdapService ldapService;
	protected SocketFactory socketFactory;
	

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
			ConnectorCapability.roleAttributes,
			ConnectorCapability.identityAttributes,
	}));
	
	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
		if(ldapService != null)
			ldapService.setSocketFactory(socketFactory);
	}

	@Override
	public Set<ConnectorCapability> getCapabilities() {
		return capabilities;
	}
	
	@Override
	public boolean isOpen() {
		return ldapService != null;
	}

	@Override
	protected void onClose() {
		ldapService = null;
	}

	@Override
	public boolean isReadOnly() {
		return directoryConfiguration.getSecurityProtocol().equals(DirectoryConfiguration.PLAIN);
	}

	protected final DirectoryConfiguration getConfiguration() {
		return directoryConfiguration;
	}

	
	
	@Override
	public boolean supportsOptimisedCheckCredentials() {
		return true;
	}

	@Override
	public boolean checkCredentialsOptimised(String remoteIdentifier, char[] password) throws ConnectorException {
		try {
			ldapService.authenticate(remoteIdentifier, new String(password));
			return true;
		} catch (IOException e) {
			return false;
		} catch (NamingException e) {
			checkNamingException(e);
			return false;
		}
	}

	protected void checkNamingException(NamingException e) {

	}
	
	@Override
	protected final boolean areCredentialsValid(Identity identity, char[] password) throws ConnectorException {

		DirectoryIdentity directoryIdentity = (DirectoryIdentity) identity;
		try {
			ldapService.authenticate(directoryIdentity.getDn().toString(), new String(password));
			return true;
		} catch (IOException e) {
			return false;
		} catch (NamingException e) {
			checkNamingException(e);
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
		}, ldapService.getSearchControls());
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
			}, configureSearchControls(ldapService.getSearchControls()));
		} catch (NamingException e) {
			processNamingException(e, "List Identities");
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
	}

	protected Identity mapIdentity(SearchResult result) throws NamingException {
		Attributes attributes = result.getAttributes();
		String guid = StringUtil.nonNull(attributes.get(directoryConfiguration.getIdentityGuidAttribute()).get().toString());
		String identityName = StringUtil.nonNull(attributes.get(directoryConfiguration.getIdentityNameAttribute()).get().toString());
		LdapName dn = new LdapName(result.getName().toString());
		Name base = directoryConfiguration.getBaseDn();
		for(int i = base.size() - 1 ; i >= 0; i--) {
			dn.add(0, base.get(i));	
		}
		
		NamingEnumeration<? extends Attribute> ne = attributes.getAll();
		DirectoryIdentity directoryIdentity = new DirectoryIdentity(guid, identityName,dn);
		directoryIdentity.setAttribute("dn", dn.toString());
		while(ne.hasMoreElements()) {
			Attribute a = ne.next();
			if(!a.getID().equals(directoryConfiguration.getIdentityGuidAttribute()) &&
					!a.getID().equals(directoryConfiguration.getIdentityNameAttribute())) {
				List<String> vals = new ArrayList<String>();
				NamingEnumeration<?> ane = a.getAll();
				while(ane.hasMoreElements()) {
					Object val = ane.next();					
					vals.add(val == null ? null : String.valueOf(val));
				}
				directoryIdentity.setAttribute(a.getID(), vals.toArray(new String[0]));
			}
		}
		
		String idRoleAttr = directoryConfiguration.getIdentityRoleGuidAttribute();
		if(!StringUtil.isNullOrEmpty(idRoleAttr)) {
			String roleObjectClass = directoryConfiguration.getRoleObjectClass();
			String roleNameAttribute = directoryConfiguration.getRoleGuidAttribute();
			String filter = ldapService.buildObjectClassFilter(roleObjectClass, roleNameAttribute, attributes.get(idRoleAttr).get().toString());
			directoryIdentity.addRole(getPrincipal(filter, getRoles(filter)));
		} else {
			idRoleAttr = directoryConfiguration.getIdentityRoleNameAttribute();
			if(!StringUtil.isNullOrEmpty(idRoleAttr)) {
				directoryIdentity.addRole(getRoleByName(idRoleAttr));				
			}	
		}
		
		return directoryIdentity;
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
			}, configureRoleSearchControls(ldapService.getSearchControls()));
			
		} catch (NamingException e) {
			processNamingException(e, "List Roles");
			throw new IllegalStateException("Unreachable code");
		} catch (IOException e) {
			throw new ConnectorException(e.getMessage(), e);
		}
		
	}

	protected Role mapRole(SearchResult result) throws NamingException {
		Attributes attributes = result.getAttributes();
		String guid = StringUtil.nonNull(attributes.get(directoryConfiguration.getRoleGuidAttribute()).get().toString());
		String identityName = StringUtil.nonNull(attributes.get(directoryConfiguration.getRoleNameAttribute()).get().toString());
		LdapName dn = new LdapName(result.getName().toString());
		NamingEnumeration<? extends Attribute> ne = attributes.getAll();
		DirectoryRole directoryRole = new DirectoryRole(guid, identityName,dn);
		while(ne.hasMoreElements()) {
			Attribute a = ne.next();
			if(!a.getID().equals(directoryConfiguration.getIdentityGuidAttribute()) &&
					!a.getID().equals(directoryConfiguration.getIdentityNameAttribute())) {
				List<String> vals = new ArrayList<String>();
				NamingEnumeration<?> ane = a.getAll();
				while(ane.hasMoreElements()) {
					Object val = ane.next();					
					vals.add(val == null ? null : String.valueOf(val));
				}
				directoryRole.setAttribute(a.getID(), vals.toArray(new String[0]));
			}
		}
		return directoryRole;
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
			ldapService.setSocketFactory(socketFactory);
			ldapService.init(directoryConfiguration);
			ldapService.openConnection();
			Name baseDn = directoryConfiguration.getBaseDn();
			LOG.info("Looking up " + baseDn);
			
		} catch(NamingException nme){
			ldapService = null;
			processNamingException(nme, "Open Connector");
		} catch (Exception e) {
			ldapService = null;
			throw new ConnectorException(e);
		}
	}

	protected String processNamingException(NamingException nme, String op) {
		DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
		String message = dep.getMessage();
		throw new ConnectorException(op + ":" + message, nme);
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
			}, ldapService.getSearchControls());
			 
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