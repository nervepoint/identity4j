package com.identity4j.connector.unix;

import java.util.Properties;

import org.junit.Test;

import com.identity4j.connector.flatfile.FlatFileConnectorTest;
import com.identity4j.connector.unix.UnixConfiguration;


public class UnixConnectorTest extends FlatFileConnectorTest {

    public UnixConnectorTest() {
        super("/unix-connector.properties");
    }
    
    @Test
    public void test() {        
    }
    
    protected void onLoadConfigurationProperties(Properties properties) {
        super.onLoadConfigurationProperties(properties);
        copyResourceToTemporaryFile(UnixConnectorTest.class, properties, UnixConfiguration.KEY_GROUP_FILE, "res:group");
        copyResourceToTemporaryFile(UnixConnectorTest.class, properties, UnixConfiguration.KEY_SHADOW_FILE, "res:shadow");
    }
}
