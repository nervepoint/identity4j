package com.identity4j.remote.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final class RemoteApiClient {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final URI baseUri;

    RemoteApiClient(String baseUri) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build(), new ObjectMapper(), baseUri);
    }

    RemoteApiClient(HttpClient client, ObjectMapper objectMapper, String baseUri) {
        this.client = Objects.requireNonNull(client, "client");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.baseUri = normalizeBaseUri(baseUri);
    }

    Map<String, Object> get(String path, Map<String, String> query) {
        return request("GET", path, query, null);
    }

    Map<String, Object> post(String path, Map<String, String> body) {
        return request("POST", path, null, body);
    }

    Map<String, Object> patch(String path, Map<String, String> body) {
        return request("PATCH", path, null, body);
    }

    Map<String, Object> delete(String path) {
        return request("DELETE", path, null, null);
    }

    boolean headSuccess(String path) {
        HttpRequest request = HttpRequest.newBuilder(resolve(path, null))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            return status >= 200 && status < 300;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RemoteApiException(500, "Failed to call remote API", e);
        }
    }

    String path(String... segments) {
        StringBuilder sb = new StringBuilder();
        for (String segment : segments) {
            if (segment == null || segment.isEmpty()) {
                continue;
            }
            sb.append('/').append(segment);
        }
        return sb.length() == 0 ? "/" : sb.toString();
    }

    private Map<String, Object> request(String method, String path, Map<String, String> query, Map<String, String> body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(resolve(path, query))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json");

        if (body != null) {
            try {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RemoteApiException(500, "Failed to serialize request body", e);
            }
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RemoteApiException(response.statusCode(), response.body());
            }

            Map<String, Object> json;
            String payload = response.body();
            if (payload == null || payload.isBlank()) {
                json = new LinkedHashMap<>();
                json.put("success", true);
                json.put("code", response.statusCode());
            } else {
                json = objectMapper.readValue(payload, MAP_TYPE);
            }

            boolean success = Boolean.TRUE.equals(json.get("success"));
            int code = asInt(json.get("code"), response.statusCode());
            if (!success || code >= 400) {
                String message = asString(json.get("message"));
                throw new RemoteApiException(code, message == null ? "Remote API error" : message);
            }
            return json;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RemoteApiException(500, "Failed to call remote API", e);
        }
    }

    private URI resolve(String path, Map<String, String> query) {
        String p = path.startsWith("/") ? path.substring(1) : path;
        StringBuilder full = new StringBuilder(baseUri.toString());
        full.append(p);
        if (query != null && !query.isEmpty()) {
            StringJoiner joiner = new StringJoiner("&");
            for (Map.Entry<String, String> entry : query.entrySet()) {
                String key = url(entry.getKey());
                String value = entry.getValue() == null ? "" : url(entry.getValue());
                joiner.add(key + "=" + value);
            }
            if (full.indexOf("?") < 0) {
                full.append('?');
            } else {
                full.append('&');
            }
            full.append(joiner);
        }
        return URI.create(full.toString());
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private URI normalizeBaseUri(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Base URI may not be empty");
        }
        String normalized = value.endsWith("/") ? value : value + "/";
        return URI.create(normalized);
    }

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                // Use default
            }
        }
        return defaultValue;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
