package com.identity4j.util.http;

public interface HttpProvider {

HttpProviderClient getClient(String url, String username, char[] password, String realm);

HttpProviderClient getClient(String url);
}
