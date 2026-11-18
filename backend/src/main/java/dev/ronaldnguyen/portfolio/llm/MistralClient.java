package dev.ronaldnguyen.portfolio.llm;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Minimal client for Mistral's chat completions endpoint. It never retries
 * (the quota is tiny) and never includes the API key in exceptions or logs.
 */
public class MistralClient {

    public record Message(String role, String content) {
    }

    public record Completion(String text, int totalTokens) {
    }

    /** Failure of a call; {@code status} is the HTTP status, or 0 for network errors and timeouts. */
    public static final class LlmException extends Exception {
        private final int status;
        private final long retryAfterSeconds;

        LlmException(String message, int status, long retryAfterSeconds) {
            super(message);
            this.status = status;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public int status() {
            return status;
        }

        public long retryAfterSeconds() {
            return retryAfterSeconds;
        }
    }

    private static final int MAX_RESPONSE_BYTES = 64 * 1024;

    private final HttpClient http;
    private final URI endpoint;
    private final String apiKey;
    private final String model;
    private final Duration timeout;

    public MistralClient(String apiBase, String apiKey, String model, Duration timeout) {
        this.endpoint = URI.create(apiBase.replaceAll("/+$", "") + "/v1/chat/completions");
        if (!"https".equals(endpoint.getScheme()) && !"localhost".equals(endpoint.getHost())) {
            throw new IllegalArgumentException("MISTRAL_API_BASE must use https");
        }
        this.apiKey = apiKey;
        this.model = model;
        this.timeout = timeout;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public String model() {
        return model;
    }

    public Completion complete(List<Message> messages, int maxTokens, double temperature) throws LlmException {
        var messageArray = Json.createArrayBuilder();
        for (Message m : messages) {
            messageArray.add(Json.createObjectBuilder().add("role", m.role()).add("content", m.content()));
        }
        String body = Json.createObjectBuilder()
                .add("model", model)
                .add("messages", messageArray)
                .add("max_tokens", maxTokens)
                .add("temperature", temperature)
                .add("safe_prompt", true)
                .build().toString();

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new LlmException("Mistral request failed: " + e.getClass().getSimpleName(), 0, 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmException("Mistral request interrupted", 0, 0);
        }

        int status = response.statusCode();
        if (status != 200) {
            long retryAfter = response.headers().firstValue("Retry-After")
                    .flatMap(MistralClient::parseLong).orElse(0L);
            throw new LlmException("Mistral returned HTTP " + status, status, retryAfter);
        }
        if (response.body().length() > MAX_RESPONSE_BYTES) {
            throw new LlmException("Mistral response too large", status, 0);
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            JsonObject json = reader.readObject();
            JsonArray choices = json.getJsonArray("choices");
            String text = choices.getJsonObject(0).getJsonObject("message").getString("content", "");
            int tokens = json.containsKey("usage")
                    ? json.getJsonObject("usage").getInt("total_tokens", maxTokens) : maxTokens;
            return new Completion(text, tokens);
        } catch (RuntimeException e) {
            throw new LlmException("Unexpected Mistral response format", status, 0);
        }
    }

    private static Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
