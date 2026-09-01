package com.identity4j.remote.secrets;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Pluggable store for secret property sheets used by remote connector requests.
 */
public interface SecretService extends Closeable {

    void create(String alias, Map<String, String> values);

    void patch(String alias, Map<String, String> values);

    boolean exists(String alias);

    void delete(String alias);

    Set<String> aliases();

    Map<String, String> get(String alias);

    @Override
    default void close() throws IOException {
        // Nothing by default.
    }
}
