package com.identity4j.remote.client;

import java.util.Map;
import java.util.Set;

/**
 * Client API for the remote secrets endpoints.
 */
public interface SecretServiceClient {

    void create(String alias, Map<String, String> values);

    void patch(String alias, Map<String, String> values);

    boolean exists(String alias);

    void delete(String alias);

    Set<String> aliases();
}
