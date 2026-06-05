package org.example.clinic.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class ClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ClientConfig.class);
    private static final String DEFAULT_RESOURCE = "application.properties";
    private static final String ENV_PREFIX = "APP_";

    private static volatile ClientConfig instance;

    private final Map<String, String> values = new LinkedHashMap<>();

    private ClientConfig() {
    }

    public static ClientConfig get() {
        ClientConfig local = instance;
        if (local == null) {
            throw new IllegalStateException(
                    "ClientConfig not initialized. Call ClientConfig.initialize(args) first.");
        }
        return local;
    }

    public static ClientConfig initialize(String[] args) {
        ClientConfig local = instance;
        if (local == null) {
            synchronized (ClientConfig.class) {
                local = instance;
                if (local == null) {
                    local = new ClientConfig();
                    local.load(args);
                    instance = local;
                }
            }
        }
        return local;
    }

    private void load(String[] args) {
        loadFromClasspath();
        loadFromEnvironment();
        loadFromArgs(args);
        log.info("Client configuration loaded: {} keys", values.size());
    }

    private void loadFromClasspath() {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                log.warn("Resource '{}' not found", DEFAULT_RESOURCE);
                return;
            }
            Properties p = new Properties();
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            p.forEach((k, v) -> values.put(k.toString(), v.toString()));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load config", e);
        }
    }

    private void loadFromEnvironment() {
        Map<String, String> env = System.getenv();
        for (String key : values.keySet()) {
            String envKey = ENV_PREFIX + key.replace('.', '_').replace('-', '_').toUpperCase();
            String envValue = env.get(envKey);
            if (envValue != null) {
                values.put(key, envValue);
            }
        }
    }

    private void loadFromArgs(String[] args) {
        if (args == null) return;
        for (String raw : args) {
            if (raw == null) continue;
            String arg = raw.startsWith("--") ? raw.substring(2) : raw;
            int eq = arg.indexOf('=');
            if (eq <= 0) continue;
            values.put(arg.substring(0, eq).trim(), arg.substring(eq + 1).trim());
        }
    }

    public String getString(String key) {
        String v = values.get(key);
        if (v == null) {
            throw new IllegalStateException("Missing config value for key: " + key);
        }
        return v;
    }

    public String getString(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String v = values.get(key);
        return v == null ? defaultValue : Integer.parseInt(v);
    }
}
