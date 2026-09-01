package com.identity4j.remote.client;

import java.net.http.HttpClient;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default secrets API client for identity4j-remote.
 */
public class RemoteSecretServiceClient implements SecretServiceClient {

    private final RemoteApiClient api;

    public RemoteSecretServiceClient(String baseUri) {
        this.api = new RemoteApiClient(baseUri);
    }

    public RemoteSecretServiceClient(HttpClient client, String baseUri) {
        this.api = new RemoteApiClient(client, new com.fasterxml.jackson.databind.ObjectMapper(), baseUri);
    }

    @Override
    public void create(String alias, Map<String, String> values) {
        api.post(api.path("secrets", alias), values);
    }

    @Override
    public void patch(String alias, Map<String, String> values) {
        api.patch(api.path("secrets", alias), values);
    }

    @Override
    public boolean exists(String alias) {
        return aliases().contains(alias);
    }

    @Override
    public void delete(String alias) {
        api.delete(api.path("secrets", alias));
    }

    @Override
    public Set<String> aliases() {
        Map<String, Object> response = api.get(api.path("secrets"), null);
        Object secrets = response.get("secrets");
        Set<String> aliases = new LinkedHashSet<>();
        if (secrets instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    aliases.add(String.valueOf(o));
                }
            }
        }
        return aliases;
    }
}
