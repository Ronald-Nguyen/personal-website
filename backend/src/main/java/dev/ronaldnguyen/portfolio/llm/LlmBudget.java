package dev.ronaldnguyen.portfolio.llm;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Server-side spending guard for the LLM. All limits are enforced before a
 * call is made, so neither a single visitor nor a crowd can burn through
 * the API quota:
 * <ul>
 *   <li>calls per minute per model (Mistral's free tier limits each model separately),</li>
 *   <li>global calls and tokens per UTC day,</li>
 *   <li>calls per client per hour,</li>
 *   <li>a per-model cool-down after 429/5xx and a global pause after 401.</li>
 * </ul>
 * When no model is available the caller falls back to the extractive answer.
 */
public class LlmBudget {

    private static final int MAX_TRACKED_CLIENTS = 20_000;
    private static final long MINUTE_MS = 60_000;
    private static final long HOUR_MS = 60 * MINUTE_MS;

    private final int perMinutePerModel;
    private final int perDay;
    private final int tokensPerDay;
    private final int perClientHour;
    private final Clock clock;

    private final Map<String, Deque<Long>> modelCalls = new HashMap<>();
    private final Map<String, Long> modelPausedUntil = new HashMap<>();
    private final Map<String, Deque<Long>> clientCalls = new HashMap<>();
    private LocalDate day;
    private int callsToday;
    private long tokensToday;
    private long pausedUntil;

    public LlmBudget(int perMinutePerModel, int perDay, int tokensPerDay, int perClientHour, Clock clock) {
        this.perMinutePerModel = perMinutePerModel;
        this.perDay = perDay;
        this.tokensPerDay = tokensPerDay;
        this.perClientHour = perClientHour;
        this.clock = clock;
        this.day = LocalDate.now(clock.withZone(ZoneOffset.UTC));
    }

    /**
     * Reserves one call for {@code client} on the first model in {@code models}
     * (in order of preference) that still has capacity. Empty means: do not call now.
     */
    public synchronized Optional<String> tryAcquire(String client, List<String> models) {
        long now = clock.millis();
        rollDay();
        if (now < pausedUntil || callsToday >= perDay || tokensToday >= tokensPerDay) {
            return Optional.empty();
        }
        if (clientCalls.size() > MAX_TRACKED_CLIENTS) {
            clientCalls.values().removeIf(q -> {
                prune(q, now - HOUR_MS);
                return q.isEmpty();
            });
            if (clientCalls.size() > MAX_TRACKED_CLIENTS) {
                return Optional.empty(); // under a flood of distinct clients, stop spending instead of forgetting limits
            }
        }
        Deque<Long> mine = clientCalls.computeIfAbsent(client, k -> new ArrayDeque<>());
        prune(mine, now - HOUR_MS);
        if (mine.size() >= perClientHour) {
            return Optional.empty();
        }
        for (String model : models) {
            if (now < modelPausedUntil.getOrDefault(model, 0L)) {
                continue;
            }
            Deque<Long> calls = modelCalls.computeIfAbsent(model, k -> new ArrayDeque<>());
            prune(calls, now - MINUTE_MS);
            if (calls.size() < perMinutePerModel) {
                calls.addLast(now);
                mine.addLast(now);
                callsToday++;
                return Optional.of(model);
            }
        }
        return Optional.empty();
    }

    public synchronized void recordTokens(int tokens) {
        rollDay();
        tokensToday += Math.max(0, tokens);
    }

    /** Stops calls to one model (rate limited upstream, model not in plan, outage). */
    public synchronized void pauseModel(String model, long millis) {
        modelPausedUntil.merge(model, clock.millis() + millis, Math::max);
    }

    /** Stops all LLM calls (e.g. the API key was rejected). */
    public synchronized void pause(long millis) {
        pausedUntil = Math.max(pausedUntil, clock.millis() + millis);
    }

    /** True when no model can be called right now because of pauses. */
    public synchronized boolean paused(List<String> models) {
        long now = clock.millis();
        return now < pausedUntil || models.stream().allMatch(m -> now < modelPausedUntil.getOrDefault(m, 0L));
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
