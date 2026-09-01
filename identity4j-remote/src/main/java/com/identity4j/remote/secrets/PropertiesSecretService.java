package com.identity4j.remote.secrets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.identity4j.connector.exception.PrincipalAlreadyExistsException;
import com.identity4j.connector.exception.PrincipalNotFoundException;

/**
 * Simple file-backed secret store using Java properties.
 */
public class PropertiesSecretService implements SecretService {

    public static final String DEFAULT_FILE = "identity4j-remote-secrets.properties";

    private final Path file;
    private final Map<String, Map<String, String>> secrets = new LinkedHashMap<>();

    public PropertiesSecretService() {
        this(DEFAULT_FILE);
    }

    public PropertiesSecretService(String fileName) {
        this.file = Paths.get(fileName).toAbsolutePath();
        load();
    }

    @Override
    public synchronized void create(String alias, Map<String, String> values) {
        if (secrets.containsKey(alias)) {
            throw new PrincipalAlreadyExistsException("The secret " + alias + " already exists");
        }
        secrets.put(alias, new LinkedHashMap<>(values));
        store();
    }

    @Override
    public synchronized void patch(String alias, Map<String, String> values) {
        Map<String, String> existing = secrets.get(alias);
        if (existing == null) {
            throw new PrincipalNotFoundException("The secret " + alias + " does not exists");
        }
        existing.putAll(values);
        store();
    }

    @Override
    public synchronized boolean exists(String alias) {
        return secrets.containsKey(alias);
    }

    @Override
    public synchronized void delete(String alias) {
        if (secrets.remove(alias) == null) {
            throw new PrincipalNotFoundException("The secret " + alias + " does not exist");
        }
        store();
    }

    @Override
    public synchronized Set<String> aliases() {
        return new LinkedHashSet<>(secrets.keySet());
    }

    @Override
    public synchronized Map<String, String> get(String alias) {
        Map<String, String> values = secrets.get(alias);
        if (values == null) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(values);
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        } catch (IOException ioe) {
            throw new IllegalStateException("Failed to read secrets from " + file, ioe);
        }

        Map<String, Map<String, String>> reloaded = new LinkedHashMap<>();
        for (String key : p.stringPropertyNames()) {
            int idx = key.indexOf('.');
            if (idx < 1 || idx == key.length() - 1) {
                continue;
            }
            String alias = key.substring(0, idx);
            String property = key.substring(idx + 1);
            reloaded.computeIfAbsent(alias, k -> new LinkedHashMap<>()).put(property, p.getProperty(key));
        }

        secrets.clear();
        secrets.putAll(reloaded);
    }

    private void store() {
        Properties p = new Properties();
        for (Map.Entry<String, Map<String, String>> aliasEntry : secrets.entrySet()) {
            for (Map.Entry<String, String> valueEntry : aliasEntry.getValue().entrySet()) {
                p.setProperty(aliasEntry.getKey() + "." + valueEntry.getKey(), valueEntry.getValue());
            }
        }

        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Identity4J Remote Secrets");
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Failed to write secrets to " + file, ioe);
        }
    }
}
