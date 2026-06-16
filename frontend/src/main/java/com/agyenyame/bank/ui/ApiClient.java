package com.agyenyame.bank.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Thin HTTP client for the banking backend. Holds the JWT in memory after login/signup.
 */
public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    public static ApiClient get() { return INSTANCE; }

    private final String baseUrl = System.getenv().getOrDefault("BANK_API_BASE_URL", "http://localhost:8080");
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token;

    public void setToken(String token) { this.token = token; }
    public boolean isAuthenticated() { return token != null; }
    public void logout() { this.token = null; }

    /** Performs a POST with a JSON body and returns the parsed response, throwing ApiError on failure. */
    public JsonNode post(String path, Object body, boolean auth) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
            if (auth && token != null) b.header("Authorization", "Bearer " + token);
            return send(b.build());
        } catch (ApiError e) {
            throw e;
        } catch (Exception e) {
            throw new ApiError("Network error: " + e.getMessage());
        }
    }

    public JsonNode get(String path) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path)).GET();
            if (token != null) b.header("Authorization", "Bearer " + token);
            return send(b.build());
        } catch (ApiError e) {
            throw e;
        } catch (Exception e) {
            throw new ApiError("Network error: " + e.getMessage());
        }
    }

    private JsonNode send(HttpRequest request) throws Exception {
        HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode node = resp.body() == null || resp.body().isBlank()
                ? mapper.createObjectNode() : mapper.readTree(resp.body());
        if (resp.statusCode() >= 400) {
            String msg = node.has("message") ? node.get("message").asText() : "Request failed (" + resp.statusCode() + ")";
            throw new ApiError(msg);
        }
        return node;
    }

    public static class ApiError extends RuntimeException {
        public ApiError(String message) { super(message); }
    }
}
