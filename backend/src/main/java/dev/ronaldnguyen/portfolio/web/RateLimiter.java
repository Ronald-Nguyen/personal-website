package dev.ronaldnguyen.portfolio.web;

import dev.ronaldnguyen.portfolio.config.AppConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;

/** Simple per-client token bucket (requests per minute) for the chat endpoint. */
@ApplicationScoped
public class RateLimiter {

    private static final int MAX_CLIENTS = 10_000;

    @Inject
    AppConfig config;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final class Bucket {
        double tokens;
        long lastRefill;

        Bucket(double tokens, long now) {
            this.tokens = tokens;
            this.lastRefill = now;
        }
    }

    public boolean tryAcquire(String client) {
        int perMinute = config.chatRateLimitPerMinute();
        long now = System.nanoTime();
        if (buckets.size() > MAX_CLIENTS) {
            buckets.clear();
        }
        Bucket bucket = buckets.computeIfAbsent(client, k -> new Bucket(perMinute, now));
        synchronized (bucket) {
            double elapsedMinutes = (now - bucket.lastRefill) / 60_000_000_000.0;
            bucket.tokens = Math.min(perMinute, bucket.tokens + elapsedMinutes * perMinute);
            bucket.lastRefill = now;
            if (bucket.tokens < 1) {
                return false;
            }
            bucket.tokens -= 1;
            return true;
        }
    }
}
