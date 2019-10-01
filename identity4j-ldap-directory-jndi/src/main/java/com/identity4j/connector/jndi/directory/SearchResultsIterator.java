package com.identity4j.connector.jndi.directory;

import java.io.IOException;

/*
 * #%L
 * Nervepoint Generic LDAP Connector
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * All Rights Reserved.
 * 
 *  This file is subject to the terms and conditions defined in
 *  file 'nervepoint-basics/src/license/logonbox/license.txt', which is part of this source code package.
 * #L%
 */

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.connector.jndi.directory.LdapService.ResultMapper;

public class SearchResultsIterator<T extends Object> implements Iterator<T> {

	final static Log LOG = LogFactory.getLog(LdapService.class);

	private ResultMapper<T> filteredMapper;
	private Name[] dns;
	private int dnIdx = 0;
	private T next;
	private Name dn;
	private String filter;
	private NamingEnumeration<SearchResult> listIterator;
	private LdapContext context;
	private SearchControls searchControls;
	private DirectoryConfiguration configuration;
	private byte[] cookie = null;

	public SearchResultsIterator(Collection<? extends Name> dns, String filter, SearchControls searchControls,
			DirectoryConfiguration configuration, ResultMapper<T> filteredMapper, LdapContext context) {
		this.context = context;
		this.configuration = configuration;
		this.searchControls = searchControls;
		this.filteredMapper = filteredMapper;
		this.dns = dns == null ? null : dns.toArray(new Name[0]);
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
		if (context != null) {
			try {
				context.close();
			} catch (NamingException e) {
			}
			context = null;
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
		boolean nullSearch = false;
		boolean nullSearchDone = false;
		Control[] prev = null;
		while (next == null) {
			if (dn == null) {
				if (dns == null && !nullSearchDone) {
					// Actually search null, i.e. globally
					nullSearch = true;
					try {
						dn = new LdapName("NULL");
					} catch (NamingException e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				} else {
					if (dnIdx >= dns.length) {
						// There are no more
						next = null;
						return;
					}
					dn = dns[dnIdx++];
				}
				cookie = null;
			}

			while (next == null && (nullSearch || dn != null)) {
				// If there is no list iterator, create the quest and get
				// the
				// results
				nullSearchDone = true;
				nullSearch = false;

				if (listIterator == null) {

					try {
						prev = context.getRequestControls();
						if (cookie != null) {
							context.setRequestControls(
									new Control[] { new PagedResultsControl(configuration.getMaxPageSize(), cookie,
											Control.CRITICAL) });
						} else {
							context.setRequestControls(new Control[] {
									new PagedResultsControl(configuration.getMaxPageSize(), Control.CRITICAL) });
						}
						listIterator = context.search(dn, filter, searchControls);

					} catch (PartialResultException e) {
						if (configuration.isFollowReferrals()) {
							LOG.error("Following referrals is on but partial result was received", e);
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Partial resluts ignored: " + e.getExplanation());
							}
						}

						// Break out of this loop to get the next DN
						dn = null;

					} catch (NamingException e) {
						LOG.error("Failed to get results", e);
						closeListIterator();
						throw new IllegalStateException(e.getMessage(), e);
					} catch (IOException e) {
						LOG.error("Failed to get results", e);
						closeListIterator();
						throw new IllegalStateException(e.getMessage(), e);
					} 

				} else {
					SearchResult result = null;
					try {
						// Get the next result, skipping nulls
						while (next == null && listIterator.hasMoreElements()) {
							result = listIterator.nextElement();
							

							
							if (filteredMapper.isApplyFilters()) {

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
							next = filteredMapper.apply(result);
						}

						if (next == null) {
							// There are no more results in the current list,
							// skip
							// to the page result


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

							} catch (NamingException e) {
								throw new IllegalStateException(e.getMessage(), e);
							} finally {
								if(prev != null) {
									context.setRequestControls(prev);
									prev = null;
								}
							}
							
							
							closeListIterator();

							if (cookie == null) {
								// Break out of this loop to get the next DN
								dn = null;
							}
						}

					} catch (PartialResultException e) {
						if (configuration.isFollowReferrals()) {
							LOG.error("Following referrals is on but partial result was received", e);
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Partial resuts ignored: " + e.getExplanation());
							}
						}
					} catch (NamingException e) {
						LOG.error("Failed to get results", e);
						closeListIterator();
						throw new IllegalStateException(e.getMessage(), e);
					} catch (IOException e) {
						LOG.error("Failed to get results", e);
						closeListIterator();
						throw new IllegalStateException(e.getMessage(), e);
					} finally {
						if (result != null && result.getObject() != null) {
							try {
								((Context) result.getObject()).close();
							} catch (NamingException e) {
								LOG.error("Failed to close object", e);
								throw new IllegalStateException(e.getMessage(), e);
							}
						}
					}

				}
			}
		}
	}

	protected void closeListIterator() {
		if (listIterator != null) {
			try {
				listIterator.close();
			} catch (NamingException e2) {
			} finally {
				listIterator = null;
			}
		}
	}

}