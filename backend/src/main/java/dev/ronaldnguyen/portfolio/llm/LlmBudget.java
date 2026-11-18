package dev.ronaldnguyen.portfolio.llm;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Server-side spending guard for the LLM. All limits are global or per
 * client and enforced before a call is made, so neither a single visitor
 * nor a crowd can burn through the API quota:
 * <ul>
 *   <li>global calls per minute (sliding window) and per UTC day,</li>
 *   <li>global tokens per UTC day,</li>
 *   <li>calls per client per hour,</li>
 *   <li>a cool-down after 429/5xx and a long pause after 401/403.</li>
 * </ul>
 * When any limit is hit the caller falls back to the extractive answer.
 */
public class LlmBudget {

    private static final int MAX_TRACKED_CLIENTS = 20_000;
    private static final long MINUTE_MS = 60_000;
    private static final long HOUR_MS = 60 * MINUTE_MS;

    private final int perMinute;
    private final int perDay;
    private final int tokensPerDay;
    private final int perClientHour;
    private final Clock clock;

    private final Deque<Long> recentCalls = new ArrayDeque<>();
    private final Map<String, Deque<Long>> clientCalls = new HashMap<>();
    private LocalDate day;
    private int callsToday;
    private long tokensToday;
    private long pausedUntil;

    public LlmBudget(int perMinute, int perDay, int tokensPerDay, int perClientHour, Clock clock) {
        this.perMinute = perMinute;
        this.perDay = perDay;
        this.tokensPerDay = tokensPerDay;
        this.perClientHour = perClientHour;
        this.clock = clock;
        this.day = LocalDate.now(clock.withZone(ZoneOffset.UTC));
    }

    /** Reserves one call for {@code client}; false means: do not call the LLM now. */
    public synchronized boolean tryAcquire(String client) {
        long now = clock.millis();
        rollDay();
        if (now < pausedUntil || callsToday >= perDay || tokensToday >= tokensPerDay) {
            return false;
        }
        prune(recentCalls, now - MINUTE_MS);
        if (recentCalls.size() >= perMinute) {
            return false;
        }
        if (clientCalls.size() > MAX_TRACKED_CLIENTS) {
            clientCalls.values().removeIf(q -> {
                prune(q, now - HOUR_MS);
                return q.isEmpty();
            });
            if (clientCalls.size() > MAX_TRACKED_CLIENTS) {
                return false; // under a flood of distinct clients, stop spending instead of forgetting limits
            }
        }
        Deque<Long> mine = clientCalls.computeIfAbsent(client, k -> new ArrayDeque<>());
        prune(mine, now - HOUR_MS);
        if (mine.size() >= perClientHour) {
            return false;
        }
        recentCalls.addLast(now);
        mine.addLast(now);
        callsToday++;
        return true;
    }

    public synchronized void recordTokens(int tokens) {
        rollDay();
        tokensToday += Math.max(0, tokens);
    }

    /** Stops all LLM calls for the given duration (rate limited upstream, bad key, outage). */
    public synchronized void pause(long millis) {
        pausedUntil = Math.max(pausedUntil, clock.millis() + millis);
    }

    public synchronized boolean paused() {
        return clock.millis() < pausedUntil;
    }

    public synchronized int callsToday() {
        rollDay();
        return callsToday;
    }

    public synchronized long tokensToday() {
        rollDay();
        return tokensToday;
    }

    private void rollDay() {
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        if (!today.equals(day)) {
            day = today;
            callsToday = 0;
            tokensToday = 0;
        }
    }

    private static void prune(Deque<Long> calls, long cutoff) {
        while (!calls.isEmpty() && calls.peekFirst() <= cutoff) {
            calls.pollFirst();
        }
    }
}
