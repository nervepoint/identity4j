package com.identity4j.connector.unix;

/*
 * #%L
 * Identity4J Unix
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

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.identity4j.connector.flatfile.FlatFileConnectorTest;
import com.identity4j.connector.principal.Identity;


public class UnixConnectorTest extends FlatFileConnectorTest<UnixConfiguration> {

    public UnixConnectorTest() {
        super("/unix-connector.properties");
    }
    
    @Test
    public void test() {        
    }
    
    @Override
	protected void updateAttributes(Identity identity, Map<String, String[]> attributes) {
    	attributes.put(UnixConnector.ATTR_HOME, new String[] { "/home/xxxx" });
	}

	protected void assertUpdatedAttributes(Identity identity,
			Map<String, String[]> attributes, Identity newIdentity,
			Map<String, String[]> newAttributes) {

		assertEquals("Attributes should be identical", "/home/xxxx",
				newIdentity.getAttribute(UnixConnector.ATTR_HOME));
	}

	protected void onLoadConfigurationProperties(Properties properties) {
        super.onLoadConfigurationProperties(properties);
        copyResourceToTemporaryFile(UnixConnectorTest.class, properties, UnixConfiguration.KEY_GROUP_FILE, "res:group");
        copyResourceToTemporaryFile(UnixConnectorTest.class, properties, UnixConfiguration.KEY_SHADOW_FILE, "res:shadow");
    }
}
