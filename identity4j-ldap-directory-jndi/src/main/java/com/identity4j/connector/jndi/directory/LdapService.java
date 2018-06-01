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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.net.SocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public class LdapService {

	private static final String LDAP_SOCKET_FACTORY = "java.naming.ldap.factory.socket";
	final static Log LOG = LogFactory.getLog(LdapService.class);

	public static final String WILDCARD_SEARCH = "*";

	public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

	private DirectoryConfiguration configuration;
	private SocketFactory socketFactory;
	private Hashtable<String, String> env = new Hashtable<String, String>();

	public void openConnection() throws NamingException, IOException {
		checkLDAPHost();
		env.put(Context.SECURITY_PRINCIPAL, configuration.getServiceAccountDn());
		env.put(Context.SECURITY_CREDENTIALS, configuration.getServiceAccountPassword());
		env.putAll(configuration.getConnectorConfigurationParameters());
		env.put(Context.PROVIDER_URL,
				configuration.buildProviderUrl(
						configuration.getSecurityProtocol().equalsIgnoreCase(DirectoryConfiguration.SSL),
						configuration.getControllerHosts()));
		configureSocket(env);
		lookupContext(configuration.getBaseDn());
	}

	private void configureSocket(Hashtable<String, String> env) {
		env.put("com.sun.jndi.ldap.connect.pool", "false");
		env.put("com.sun.jndi.ldap.connect.pool.debug", "all");
		env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		// env.put("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
	}

	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	public LdapContext getConnection(Control... controls) throws NamingException {
		if (socketFactory != null) {
			env.put(LDAP_SOCKET_FACTORY, ThreadLocalSocketFactory.class.getName());
			ThreadLocalSocketFactory.set(socketFactory);
		}
		configureSocket(env);
		try {
			InitialLdapContext ctx = new InitialLdapContext(env, null);
			ctx.setRequestControls(controls);
			return ctx;
		} finally {
			if (socketFactory != null) {
				ThreadLocalSocketFactory.remove();
			}
		}
	}

	public DirContext getConnection(String account, String password) throws NamingException, IOException {
		Hashtable<String, String> env = new Hashtable<String, String>(
				configuration.getConnectorConfigurationParameters());
		env.put(Context.PROVIDER_URL,
				configuration.buildProviderUrl(
						configuration.getSecurityProtocol().equalsIgnoreCase(DirectoryConfiguration.SSL),
						configuration.getControllerHosts()));

		env.put(Context.SECURITY_PRINCIPAL, account);
		env.put(Context.SECURITY_CREDENTIALS, password);
		if (socketFactory != null) {
			env.put(LDAP_SOCKET_FACTORY, ThreadLocalSocketFactory.class.getName());
			ThreadLocalSocketFactory.set(socketFactory);
		}
		configureSocket(env);
		try {
			return new InitialDirContext(env);
		} finally {
			if (socketFactory != null) {
				ThreadLocalSocketFactory.remove();
			}
		}

	}

	public void authenticate(String account, String password) throws IOException, NamingException {
		close(getConnection(account, password));
	}

	public void setPassword(final String account, final char[] newPassword) throws NamingException, IOException {
		processBlock(new Block<Void>() {

			public Void apply(LdapContext context) throws NamingException {
				ModificationItem[] mods = new ModificationItem[1];
				byte[] encodedPassword = DefaultEncoderManager.getInstance().encode(newPassword,
						configuration.getIdentityPasswordEncoding(), "UTF-8", null, null);
				Attribute attribute = new BasicAttribute(configuration.getIdentityPasswordAttribute(), encodedPassword);
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
				context.modifyAttributes(account, mods);
				return null;
			}
		});
	}

	public void setPassword(final String account, final byte[] encodedPassword, Control... controls)
			throws NamingException, IOException {
		processBlock(new Block<Void>() {

			public Void apply(LdapContext context) throws NamingException {
				ModificationItem[] mods = new ModificationItem[1];
				Attribute attribute = new BasicAttribute(configuration.getIdentityPasswordAttribute(), encodedPassword);
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
				context.modifyAttributes(account, mods);
				return null;
			}
		}, controls);
	}

	public void close() {
	}

	private void close(DirContext ctx) {
		if (ctx != null)
			try {
				ctx.close();
			} catch (NamingException e) {
			}
	}

	public SearchControls getSearchControls() {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		return searchControls;
	}

	LdapService() {
	}

	public void init(DirectoryConfiguration configuration) {
		this.configuration = configuration;
	}

	public void rename(final LdapName currentDN, final LdapName newDN) throws NamingException, IOException {
		processBlock(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException, IOException {
				context.rename(currentDN, newDN);
				return null;
			}
		});
	}

	public <T> Iterator<T> search(String filter, ResultMapper<T> resultMapper, SearchControls searchControls)
			throws NamingException, IOException {
		return search(configuration.getBaseDn(), filter, resultMapper, searchControls);
	}

	public <T> Iterator<T> search(final Name baseDN, final String filter, final ResultMapper<T> resultMapper,
			final SearchControls searchControls) throws NamingException, IOException {
		return processBlockNoClose(new Block<Iterator<T>>() {

			public Iterator<T> apply(LdapContext context) throws IOException, NamingException {
				if("true".equals(System.getProperty("identity4j.useNewIterator", "false")))
					return new SearchResultsIterator<T>(Arrays.asList(baseDN), filter, searchControls, configuration, resultMapper, context);
				else
					return new OldSearchResultIterator<T>(baseDN, context, filter, resultMapper, searchControls);
			}
		});
	}

	public void unbind(final Name name) throws NamingException, IOException {
		processBlock(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException, IOException {
				context.unbind(name);
				return null;
			}
		});
	}

	public void update(final Name name, final ModificationItem... mods) throws NamingException, IOException {
		processBlock(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException, IOException {
				context.modifyAttributes(name, mods);
				return null;
			}
		});
	}

	public void bind(final Name name, final Attribute... attrs) throws NamingException, IOException {
		processBlock(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException, IOException {
				Attributes attributes = new BasicAttributes();
				for (Attribute attribute : attrs) {
					attributes.put(attribute);
				}
				context.bind(name, null, attributes);
				return null;
			}
		});
	}

	public LdapContext lookupContext(final Name dn) throws NamingException, IOException {
		return processBlock(new Block<LdapContext>() {
			public LdapContext apply(LdapContext context) throws NamingException {
				return (LdapContext) context.lookup(dn);
			}
		});
	}

	public final String buildObjectClassFilter(String objectClass, String principalNameFilterAttribute,
			String principalName) {
		return String.format("(&(objectClass=%s)(%s=%s))", objectClass, principalNameFilterAttribute, principalName);
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

	private <T> T processBlock(Block<T> block, Control... controls) throws NamingException, IOException {
		LdapContext connection = getConnection(controls);
		try {
			return block.apply(connection);
		} finally {
			close(connection);
		}

	}

	private <T> T processBlockNoClose(Block<T> block, Control... controls) throws NamingException, IOException {
		return block.apply(getConnection(controls));
	}

	public interface ResultMapper<T> {
		public T apply(SearchResult result) throws NamingException, IOException;

		public boolean isApplyFilters();
	}

	public interface Block<T> {
		public T apply(LdapContext context) throws NamingException, IOException;
	}

	protected void checkLDAPHost() {
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
						throw new ConnectorException("IP " + controllerHost
								+ " is not resolvable by a reverse DNS. Check your DNS configuration. "
								+ "If this error persists try adding an entry for " + controllerHost
								+ " to your system HOSTS file.");
					}
				} catch (UnknownHostException e) {
				}
			}
		}
	}
	
	class OldSearchResultIterator<T> implements Iterator<T> {

		NamingEnumeration<SearchResult> results = null;
		ResultMapper<T> resultMapper;
		byte[] cookie = null;
		LdapContext context;
		SearchControls searchControls;
		Name baseDN;
		String filter;
		LinkedList<T> cached = new LinkedList<T>();
		
		OldSearchResultIterator(Name baseDN, LdapContext context, String filter, ResultMapper<T> resultMapper,
				SearchControls searchControls) throws NamingException, IOException {
			this.resultMapper = resultMapper;
			this.baseDN = baseDN;
			this.searchControls = searchControls;
			this.context = context;
			this.filter = filter;
			buildResults();
		}

		private void buildResults() {
			
			try {
				if (cookie != null) {
					context.setRequestControls(new Control[] {
							new PagedResultsControl(configuration.getMaxPageSize(), cookie, Control.CRITICAL) });
				} else {
					context.setRequestControls(
							new Control[] { new PagedResultsControl(configuration.getMaxPageSize(), Control.CRITICAL) });
				}
				
				results = context.search(baseDN, filter, searchControls);
				
				while(results.hasMore()) {
					
					SearchResult result = null;
					try {
						
						result = results.next();
	
						if (resultMapper.isApplyFilters()) {

							Name resultName = new LdapName(result.getNameInNamespace());
							boolean include = configuration.getIncludes().isEmpty();
							if (!include) {
								for (Name name : configuration.getIncludes()) {
									if (resultName.startsWith(name)) {
										include = true;
										break;
									}
								}
							}
		
							for (Name name : configuration.getExcludes()) {
								if (resultName.startsWith(name)) {
									include = false;
									break;
								}
							}
		
							if (!include) {
								continue;
							}
						}
	
						cached.addLast(resultMapper.apply(result));
	
					} catch (PartialResultException e) {
						if (configuration.isFollowReferrals()) {
							LOG.error("Following referrals is on but partial result was received", e);
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Partial resluts ignored: " + e.getExplanation());
							}
						}
					} catch (NamingException e) {
						LOG.error("Failed to get results", e);
						throw new IllegalStateException(e.getMessage(), e);
					} catch (IOException e) {
						LOG.error("Failed to get results", e);
						throw new IllegalStateException(e.getMessage(), e);
					} finally {
						if(result!=null && result.getObject()!=null) {
							((Context)result.getObject()).close();
						}
					}
				}
			} catch (PartialResultException e) {
				if (configuration.isFollowReferrals()) {
					LOG.error("Following referrals is on but partial result was received", e);
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Partial resluts ignored: " + e.getExplanation());
					}
				}
			} catch (NamingException e) {
				LOG.error("Failed to get results", e);
				throw new IllegalStateException(e.getMessage(), e);
			} catch (IOException e) {
				LOG.error("Failed to get results", e);
				throw new IllegalStateException(e.getMessage(), e);
			} finally {
				if(results!=null) {
					try {
						results.close();
					} catch (NamingException e) {
					}
				}
			}
			
			try {

				// Record page cookie for next set of results
				Control[] controls = context.getResponseControls();
				if (controls != null) {
					for (int i = 0; i < controls.length; i++) {
						if (controls[i] instanceof PagedResultsResponseControl) {
							PagedResultsResponseControl pagedResultsResponseControl = (PagedResultsResponseControl) controls[i];
							cookie = pagedResultsResponseControl.getCookie();
						}
					}
				}

				if (cookie == null) {
					close(context);
					return;
				}

			} catch (NamingException e) {
				LOG.error("Failed to get results", e);
				throw new IllegalStateException(e.getMessage(), e);
			}
		}

		@Override
		public boolean hasNext() {
			return !cached.isEmpty();
		}

		@Override
		public T next() {

			if(cached.isEmpty()) {
				throw new NoSuchElementException();
			}
			
			T next = cached.removeFirst();

			if(cached.isEmpty()) {
				if(cookie!=null) {
					buildResults();
				}
			}

			return next;
		}

		@Override
		public void remove() {
		}

	}
}
