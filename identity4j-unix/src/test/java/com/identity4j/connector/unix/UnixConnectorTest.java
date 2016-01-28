package com.identity4j.connector.unix;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.identity4j.connector.flatfile.FlatFileConnectorTest;
import com.identity4j.connector.principal.Identity;


public class UnixConnectorTest extends FlatFileConnectorTest {

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
