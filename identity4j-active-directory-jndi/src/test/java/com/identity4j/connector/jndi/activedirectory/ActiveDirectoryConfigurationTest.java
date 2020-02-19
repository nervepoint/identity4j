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

import javax.naming.Name;

import org.junit.Test;

import com.identity4j.connector.jndi.directory.AbstractDirectoryConfiguration;
import com.identity4j.connector.jndi.directory.DirectoryConfiguration;
import com.identity4j.util.MultiMap;

public class ActiveDirectoryConfigurationTest {
    private final MultiMap configurationParameters;

    public ActiveDirectoryConfigurationTest() {
        configurationParameters = new MultiMap();
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_HOSTNAME, "controller.root.nervepoint.com");
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_SERVICE_ACCOUNT_USERNAME, "admin");
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_BASE_DN, "ou=test,dc=root,dc=nervepoint,dc=com");
    }

    @Test
    public void emptyBaseDn() {
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_BASE_DN, "");
        AbstractDirectoryConfiguration configuration = new ActiveDirectoryConfiguration(configurationParameters);
        Name baseDn = configuration.getBaseDn();
        assertEquals(3, baseDn.size());
        assertEquals("DC=root", baseDn.get(2));
        assertEquals("DC=nervepoint", baseDn.get(1));
        assertEquals("DC=com", baseDn.get(0));
    }

    @Test
    public void commonNameWithoutBaseDn() {
        final String username = "cn=admin";
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_SERVICE_ACCOUNT_USERNAME, username);
        AbstractDirectoryConfiguration configuration = new ActiveDirectoryConfiguration(configurationParameters);
        String baseDn = configurationParameters.getString(DirectoryConfiguration.DIRECTORY_BASE_DN);
        assertEquals(username + "," + baseDn, configuration.getServiceAccountDn());
    }

    @Test
    public void commonNameWithoutBaseDnButComma() {
        final String username = "cn=admin,";
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_SERVICE_ACCOUNT_USERNAME, username);
        AbstractDirectoryConfiguration configuration = new ActiveDirectoryConfiguration(configurationParameters);
        String baseDn = configurationParameters.getString(DirectoryConfiguration.DIRECTORY_BASE_DN);
        assertEquals(username + baseDn, configuration.getServiceAccountDn());
    }

    @Test
    public void commonNameWithBaseDn() {
        String baseDn = configurationParameters.getString(DirectoryConfiguration.DIRECTORY_BASE_DN);
        final String username = "cn=admin" + "," + baseDn;
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_SERVICE_ACCOUNT_USERNAME, username);
        AbstractDirectoryConfiguration configuration = new ActiveDirectoryConfiguration(configurationParameters);
        assertEquals(username, configuration.getServiceAccountDn());
    }

    @Test
    public void username() {
        final String username = "admin";
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_SERVICE_ACCOUNT_USERNAME, username);
        AbstractDirectoryConfiguration configuration = new ActiveDirectoryConfiguration(configurationParameters);
        assertEquals(username + "@root.nervepoint.com", configuration.getServiceAccountDn());
    }

    @Test
    public void qualifiedUserPrincipalName() {
        final String username = "admin@root.nervepoint.com";
        configurationParameters.set(DirectoryConfiguration.DIRECTORY_SERVICE_ACCOUNT_USERNAME, username);
        AbstractDirectoryConfiguration configuration = new ActiveDirectoryConfiguration(configurationParameters);
        assertEquals(username, configuration.getServiceAccountDn());
    }
}