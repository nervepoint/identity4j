package com.identity4j.connector.jndi.directory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.PasswordChangeRequiredException;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public class LdapService {
	
	final static Log LOG = LogFactory.getLog(LdapService.class);
	/**
     */
	public static final String WILDCARD_SEARCH = "*";
	/**
     */
	public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

	private DirectoryConfiguration configuration; 

    private Hashtable<String,String> env = new Hashtable<String,String>();
    
    public void openConnection() throws NamingException, IOException{
    	checkLDAPHost();
    	env.put(Context.SECURITY_PRINCIPAL, configuration.getServiceAccountDn());  
	    env.put(Context.SECURITY_CREDENTIALS, configuration.getServiceAccountPassword());
    	env.putAll(configuration.getConnectorConfigurationParameters());
	    env.put(Context.PROVIDER_URL,
	    		configuration.buildProviderUrl(configuration.getSecurityProtocol().equalsIgnoreCase(DirectoryConfiguration.SSL), 
	    				configuration.getControllerHosts()));
    	lookupContext(configuration.getBaseDn());
    }
    
    public LdapContext getConnection() throws NamingException{
    	return new InitialLdapContext(env,null);
    }

    public DirContext getConnection(String account,String password) throws NamingException, IOException{
		Hashtable<String,String> env = new Hashtable<String, String>(configuration.getConnectorConfigurationParameters());
		env.put(Context.PROVIDER_URL,
	    		configuration.buildProviderUrl(configuration.getSecurityProtocol().equalsIgnoreCase(DirectoryConfiguration.SSL), 
	    				configuration.getControllerHosts()));
		
		env.put(Context.SECURITY_PRINCIPAL, account);  
	    env.put(Context.SECURITY_CREDENTIALS, password);
	    
		return new InitialDirContext(env);
		
    }
    
    public boolean authenticate(String account,String password) throws IOException{
    	try{
    		getConnection(account, password);
    	}catch (NamingException nme) {
			// http://stackoverflow.com/questions/2672125/what-does-sub-error-code-568-mean-for-ldap-error-49-with-active-directory
			DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
			if ("773".equals(dep.getData())) {
				throw new PasswordChangeRequiredException();
			}
			return false;
		}
    	return true;
    }
    
    public void setPassword(final String account,final char[] newPassword) throws NamingException,IOException{
		_(new Block<Void>() {

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
    
    public void setPassword(final String account,final byte[] encodedPassword) throws NamingException,IOException{
		_(new Block<Void>() {

			public Void apply(LdapContext context) throws NamingException {
		            ModificationItem[] mods = new ModificationItem[1];
					Attribute attribute = new BasicAttribute(configuration.getIdentityPasswordAttribute(), encodedPassword);
			    	mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
					context.modifyAttributes(account, mods);
					return null;
			}
		});
    }
    
    public void close(DirContext ctx) {
		if(ctx !=null)
			try {
				ctx.close();
			} catch (NamingException e) {
				throw new ConnectorException("Problem in closing " + e.getMessage(), e);
			}
	}
	
    public SearchControls getSearchControls(){
    	SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return searchControls;
    }
    
    LdapService(){}

	public void init(DirectoryConfiguration configuration){
		this.configuration = configuration;
	}
	
	public void rename(final LdapName currentDN, final LdapName newDN) throws NamingException, IOException {
		_(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException,IOException {
				context.rename(currentDN, newDN);
				return null;
			}
		});
	}
	
	public <T> Iterator<T> search(String filter, ResultMapper<T> resultMapper)throws NamingException,IOException{
		return search(configuration.getBaseDn(), filter, resultMapper);
	}
	
	public <T> Iterator<T> search(final Name baseDN, final String filter,final ResultMapper<T> resultMapper)throws NamingException,IOException{
		return _(new Block<Iterator<T>>() {

			public Iterator<T> apply(LdapContext context) throws IOException, NamingException {
				return new SearchResultIterator<T>(baseDN, context, filter, resultMapper);
			}
		});
	}
	
	class SearchResultIterator<T> implements Iterator<T> {

		NamingEnumeration<SearchResult> results = null;
		ResultMapper<T> resultMapper;
		T nextElement;
		byte[] cookie = null;
		LdapContext context;
		Name baseDN;
		String filter;
		
		SearchResultIterator(Name baseDN, LdapContext context, String filter, ResultMapper<T> resultMapper) throws NamingException, IOException {
			this.resultMapper = resultMapper;
			this.baseDN = baseDN;
			this.context = context;
			this.filter = filter;
			buildResults();
			nextElement = getNextElement();
		}
		
		private void buildResults() throws NamingException, IOException {
			if(cookie!=null) {
				context.setRequestControls(new Control[]{new PagedResultsControl(configuration.getMaxPageSize(), cookie, Control.CRITICAL) });
			} else {
				context.setRequestControls(new Control[]{new PagedResultsControl(configuration.getMaxPageSize(), Control.CRITICAL) });
			}
			results = context.search(baseDN, filter, getSearchControls());

		}
		
		T getNextElement() {
			if(results.hasMoreElements()) {
				try {
					return resultMapper.apply(results.next());
				} catch (NamingException e) {
					LOG.error("Failed to get results", e);
					throw new IllegalStateException(e);
				}
			}
			
			try {

				// Record page cookie for next set of results
				Control[] controls = context.getResponseControls();
				if (controls != null) {
					for (int i = 0; i < controls.length; i++) {
					    if (controls[i] instanceof PagedResultsResponseControl) {
							PagedResultsResponseControl pagedResultsResponseControl =
				                         	 (PagedResultsResponseControl)controls[i];
							cookie = pagedResultsResponseControl.getCookie();
					    }
					}
				}
				
				
				if(cookie==null) {
					close(context);
					return null;
				}
				
				buildResults();
				return getNextElement();
			} catch (NamingException e) {
				LOG.error("Failed to get results", e);
				throw new IllegalStateException(e);
			} catch (IOException e) {
				LOG.error("Failed to get results", e);
				throw new IllegalStateException(e);
			}
		}
		
		
		@Override
		public boolean hasNext() {
			return nextElement!=null;
		}

		@Override
		public T next() {
			
			if(nextElement==null) {
				throw new NoSuchElementException();
			}
			
			try {
				return nextElement;
			} finally {
				nextElement = getNextElement();
			}
		}

		@Override
		public void remove() {
		}
		
	}
	
	public void unbind(final Name name) throws NamingException, IOException{
		_(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException,IOException {
				context.unbind(name);
				return null;
			}
		});
	}
	
	
	public void update(final Name name,final ModificationItem...mods) throws NamingException, IOException{
		_(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException,IOException {
				context.modifyAttributes(name, mods);
				return null;
			}
		});
	}
	
	
	public void bind(final Name name,final Attribute...attrs) throws NamingException, IOException{
		_(new Block<Void>() {

			@Override
			public Void apply(LdapContext context) throws NamingException,IOException {
				Attributes attributes = new BasicAttributes();
				for (Attribute attribute : attrs) {
					attributes.put(attribute);
				}
				context.bind(name,null,attributes);
				return null;
			}
		});
	}

	public Attributes lookupContext(final Name dn) throws NamingException,IOException{
		return _(new Block<Attributes>() {

			public Attributes apply(LdapContext context) throws NamingException {
				return ((LdapContext) context.lookup(dn)).getAttributes("");
			}
		});
	}
	
	
	public final String buildObjectClassFilter(String objectClass, String principalNameFilterAttribute, String principalName) {
		return String.format("(&(objectClass=%s)(%s=%s))",objectClass,principalNameFilterAttribute,principalName);
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
	
	private <T> T _(Block<T> block) throws NamingException, IOException{
		LdapContext ctx = null;
		ctx = getConnection();
		return block.apply(ctx);
	}
	
	public interface ResultMapper<T>{
		public T apply(SearchResult result) throws NamingException;
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
						throw new IllegalStateException("LDAP controller host address " + controllerHost
							+ " is not resolvable by a reverse DNS lookup. Please check your DNS configuration.");
					}
				} catch (UnknownHostException e) {
				}
			}
		}
	}
	
}
