package com.identity4j.connector.htpasswd;

import org.junit.Test;

import com.identity4j.connector.flatfile.FlatFileConnectorTest;


public class HTPasswdConnectorTest extends FlatFileConnectorTest {

    public HTPasswdConnectorTest() {
        super("/htpasswd-connector.properties");
    }
    
    @Test
    public void test() {        
    }
}
