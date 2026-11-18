package dev.ronaldnguyen.portfolio.config;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Central access to runtime configuration. Values come from environment
 * variables (or any other MicroProfile Config source) and always have a
 * working default so the application starts without a .env file.
 */
@ApplicationScoped
public class AppConfig {

    private static Config config() {
        return ConfigProvider.getConfig();
    }

    private static String string(String key, String defaultValue) {
        return config().getOptionalValue(key, String.class)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(defaultValue);
    }

    private static int integer(String key, int defaultValue) {
        try {
            return Integer.parseInt(string(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String githubUsername() {
        return string("GITHUB_USERNAME", "Ronald-Nguyen");
    }

    public Optional<String> githubToken() {
        return Optional.ofNullable(string("GITHUB_TOKEN", null));
    }

    public int githubRepoLimit() {
        return Math.max(1, Math.min(100, integer("GITHUB_REPO_LIMIT", 10)));
    }

    public int githubRefreshHours() {
        return Math.max(1, integer("GITHUB_REFRESH_HOURS", 6));
    }

    public String githubApiBase() {
        return string("GITHUB_API_BASE", "https://api.github.com");
    }

    public int chatRateLimitPerMinute() {
        return Math.max(1, integer("CHAT_RATE_LIMIT_PER_MINUTE", 20));
    }

    /** Mistral API key; when absent the chat answers extractively without an LLM. */
    public Optional<String> mistralApiKey() {
        return Optional.ofNullable(string("MISTRAL_API_KEY", null));
    }

    public String mistralModel() {
        return string("MISTRAL_MODEL", "ministral-8b-latest");
    }

    public String mistralApiBase() {
        return string("MISTRAL_API_BASE", "https://api.mistral.ai");
    }

    /** Global (all visitors together) LLM calls per minute; matches the free tier by default. */
    public int llmRequestsPerMinute() {
        return Math.max(1, integer("LLM_REQUESTS_PER_MINUTE", 2));
    }

    /** Global LLM calls per UTC day; afterwards the chat silently falls back to extractive answers. */
    public int llmRequestsPerDay() {
        return Math.max(1, integer("LLM_REQUESTS_PER_DAY", 300));
    }

    /** Global LLM tokens (prompt + completion) per UTC day. */
    public int llmTokensPerDay() {
        return Math.max(1_000, integer("LLM_TOKENS_PER_DAY", 400_000));
    }

    /** LLM calls per visitor per hour, on top of the general chat rate limit. */
    public int llmRequestsPerClientHour() {
        return Math.max(1, integer("LLM_REQUESTS_PER_CLIENT_HOUR", 15));
    }

    /**
     * Whether X-Forwarded-For is trusted for client identification. Only safe
     * when the backend is reachable exclusively through the Nginx proxy.
     */
    public boolean trustForwardedFor() {
        return Boolean.parseBoolean(string("TRUST_FORWARDED_FOR", "true"));
    }

    public static List<String> corsAllowedOrigins() {
        return Arrays.stream(string("CORS_ALLOWED_ORIGINS",
                        "http://localhost:5173,http://localhost:3000").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
