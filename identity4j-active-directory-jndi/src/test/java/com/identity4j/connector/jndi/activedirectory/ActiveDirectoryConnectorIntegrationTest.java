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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assume;
import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.Connector;
import com.identity4j.connector.ConnectorCapability;
import com.identity4j.connector.ResultIterator;
import com.identity4j.connector.principal.Identity;
import com.identity4j.connector.principal.IdentityImpl;
import com.identity4j.connector.principal.Role;
import com.identity4j.util.MultiMap;
import com.identity4j.util.TestUtils;

import junit.framework.Assert;

public class ActiveDirectoryConnectorIntegrationTest extends AbstractConnectorTest<ActiveDirectoryConfiguration> {

	public ActiveDirectoryConnectorIntegrationTest() {
		super("/active-directory-connector.properties");
		setReecreateTestUserOnSelectedTests(true);
	}

	@Override
	@Test
	public void changePassword() {
		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.passwordChange));

		try {
			String password = "aA" + TestUtils.randomValue() + "1$12123123######121221212";
			connector.changePassword(identityName, identityGuid, identityPassword.toCharArray(),
					password.toCharArray());
			assertPasswordChange(identityName, identityPassword, password);
		} finally {
			// reset to original password
			connector.setPassword(identityName, identityGuid, identityPassword.toCharArray(), false);
		}
	}

	@Test
	public void testOUFilters() throws IOException {
		doTestOUFilters(false);
	}

	@Test
	public void testOUFiltersAtNonDomainBaseDN() throws IOException {
		doTestOutFiltersAtNonDomainBaseDN(false);
	}

	@Test
	public void testOUFiltersAtNonDomainBaseDNWithDefaults() throws IOException {
		doTestOutFiltersAtNonDomainBaseDN(true);
	}

	@Test
	public void testOUFiltersWithDefaults() throws IOException {
		doTestOUFilters(true);
	}
	
	@Test
	public void testTag() {

		Assume.assumeTrue(connector.getCapabilities().contains(ConnectorCapability.tag));
		ResultIterator<Identity> ri = connector.allIdentities(null);
		
		/* Count first iterate */
		int i = 0;
		while(ri.hasNext()) {
			ri.next();
			i++;
		}
		
		String tag = ri.tag();
		
		ResultIterator<Identity> nri = connector.allIdentities(tag);
		Assert.assertTrue("Should return no results in iterator", !nri.hasNext());
		
		updateIdentity();

		tag = nri.tag();
		nri = connector.allIdentities(tag);
		i = 0;
		while(nri.hasNext()) {
			nri.next();
			i++;
		}
		Assert.assertEquals("Should return 1 results", 1, i);
		
	}

	@Override
	protected void assertUpdatedAttributes(Identity identity, Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {
		assertEquals("Attributes should be identical", "My Office",
				newIdentity.getAttribute("physicalDeliveryOfficeName"));
	}

	@Override
	protected void assertUpdatedRoleAttributes(Role identity, Map<String, String[]> attributes, Role newIdentity,
			Map<String, String[]> newAttributes) {
		assertEquals("Attributes should be identical", "My Role Description " + newIdentity.getPrincipalName(),
				newIdentity.getAttribute("description"));
	}

	protected void doTestOUFilters(boolean includeDefaults) throws IOException {
		List<Identity> created = new ArrayList<Identity>();
		try {
			String ou = "OU=Filtered," + connectorConfigurationParameters.getBaseDN();
			for (int i = 1; i <= 1; i++) {
				Identity newIdentity = new IdentityImpl(identityName + "OU" + i);
				String fullName = newIdentity.getPrincipalName() + "s full name";
				newIdentity.setFullName(fullName);
				newIdentity.setAttribute(ActiveDirectoryConnector.OU_ATTRIBUTE, ou);
				LOG.info("Creating " + newIdentity);
				created.add(connector.createIdentity(newIdentity, identityPassword.toCharArray()));
			}

			/*
			 * Start another connection with new configuration of the includes
			 */
			MultiMap prms = new MultiMap(configurationParameters);
			prms.set(ActiveDirectoryConfiguration.DIRECTORY_INCLUDES, "OU=Filtered");
			prms.remove(ActiveDirectoryConfiguration.DIRECTORY_EXCLUDES);
			prms.set(ActiveDirectoryConfiguration.ACTIVE_DIRECTORY_INCLUDE_DEFAULT_USERS, String.valueOf(includeDefaults));
			ActiveDirectoryConfiguration cfg = createConnectorConfigurationParameters(prms);
			Connector<?> subc = connectorBuilder.buildConnector(cfg);
			try {
				List<Identity> ids = new ArrayList<Identity>();
				for (Iterator<Identity> idIt = subc.allIdentities(); idIt.hasNext();) {
					ids.add(idIt.next());
				}

				Collections.sort(ids);
				Collections.sort(created);

				assertEquals("Identity List Should Be Identical", created, ids);

			} finally {
				subc.close();
			}

		} finally {
			for (Identity c : created) {
				LOG.info("Deleting " + c);
				connector.deleteIdentity(c.getPrincipalName());
			}
		}
	}

	protected void doTestOutFiltersAtNonDomainBaseDN(boolean includeDefaults) throws IOException {
		List<Identity> created = new ArrayList<Identity>();
		try {
			String ou = "OU=Filtered," + connectorConfigurationParameters.getBaseDN();
			for (int i = 1; i <= 3; i++) {
				Identity newIdentity = new IdentityImpl(identityName + "OU" + i);
				String fullName = newIdentity.getPrincipalName() + "s full name";
				newIdentity.setFullName(fullName);
				newIdentity.setAttribute(ActiveDirectoryConnector.OU_ATTRIBUTE, ou);
				LOG.info("Creating " + newIdentity);
				created.add(connector.createIdentity(newIdentity, identityPassword.toCharArray()));
			}

			/*
			 * Start another connection with new configuration of the includes
			 */
			MultiMap prms = new MultiMap(configurationParameters);
			prms.set(ActiveDirectoryConfiguration.DIRECTORY_BASE_DN, ou);
			List<String> inc = new ArrayList<String>();
			for (Identity i : created) {
				inc.add("CN=" + i.getAttribute(ActiveDirectoryConnector.COMMON_NAME_ATTRIBUTE));
			}
			prms.set(ActiveDirectoryConfiguration.DIRECTORY_INCLUDES, inc);
			prms.remove(ActiveDirectoryConfiguration.DIRECTORY_EXCLUDES);
			prms.set(ActiveDirectoryConfiguration.ACTIVE_DIRECTORY_INCLUDE_DEFAULT_USERS,
					String.valueOf(includeDefaults));
			ActiveDirectoryConfiguration cfg = createConnectorConfigurationParameters(prms);
			Connector<?> subc = connectorBuilder.buildConnector(cfg);
			try {
				List<Identity> ids = new ArrayList<Identity>();
				for (Iterator<Identity> idIt = subc.allIdentities(); idIt.hasNext();) {
					ids.add(idIt.next());
				}

				Collections.sort(ids);
				Collections.sort(created);

				assertEquals("Identity List Should Be Identical", created, ids);

			} finally {
				subc.close();
			}

		} finally {
			for (Identity c : created) {
				LOG.info("Deleting " + c);
				connector.deleteIdentity(c.getPrincipalName());
			}
		}
	}

	@Override
	protected void populateIdentityForCreation(Identity newIdentity) {
		newIdentity.setAttribute(ActiveDirectoryConnector.OU_ATTRIBUTE, connectorConfigurationParameters.getOU());
	}

	@Override
	protected void populateRoleForCreation(Role newRole) {
		newRole.setAttribute(ActiveDirectoryConnector.OU_ATTRIBUTE, connectorConfigurationParameters.getOU());
	}

	@Override
	protected void updateAttributes(Identity identity, Map<String, String[]> attributes) {
		attributes.put("physicalDeliveryOfficeName", new String[] { "My Office" });
	}

	@Override
	protected void updateRoleAttributes(Role role, Map<String, String[]> attributes) {
		attributes.put("description", new String[] { "My Role Description " + role.getPrincipalName() });
	}
}