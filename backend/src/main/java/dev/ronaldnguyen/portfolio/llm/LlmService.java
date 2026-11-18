package dev.ronaldnguyen.portfolio.llm;

import dev.ronaldnguyen.portfolio.config.AppConfig;
import dev.ronaldnguyen.portfolio.rag.ScoredChunk;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Optional generative layer on top of retrieval. Every call is guarded by
 * {@link LlmBudget}, a concurrency cap and an answer cache; any failure or
 * exhausted budget returns empty so the caller keeps the extractive answer.
 */
@ApplicationScoped
public class LlmService {

    private static final Logger LOG = Logger.getLogger(LlmService.class.getName());
    private static final int MAX_TOKENS = 500;
    private static final double TEMPERATURE = 0.2;
    private static final int MAX_CONCURRENT = 3;
    private static final int CACHE_SIZE = 500;
    private static final long CACHE_TTL_MS = Duration.ofHours(6).toMillis();
    private static final long PAUSE_RATE_LIMITED_MS = Duration.ofSeconds(60).toMillis();
    private static final long PAUSE_SERVER_ERROR_MS = Duration.ofMinutes(2).toMillis();
    private static final long PAUSE_AUTH_ERROR_MS = Duration.ofHours(1).toMillis();

    @Inject
    AppConfig config;

    private MistralClient client;
    private List<String> models = List.of();
    private LlmBudget budget;
    private final Semaphore inFlight = new Semaphore(MAX_CONCURRENT);
    private final Map<String, CachedAnswer> cache = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedAnswer> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    private record CachedAnswer(String text, long expiresAt) {
    }

    @PostConstruct
    void init() {
        budget = new LlmBudget(config.llmRequestsPerMinute(), config.llmRequestsPerDay(),
                config.llmTokensPerDay(), config.llmRequestsPerClientHour(), Clock.systemUTC());
        Optional<String> key = config.mistralApiKey();
        if (key.isEmpty()) {
            LOG.info("MISTRAL_API_KEY not set: chat answers are extractive only.");
            return;
        }
        try {
            models = config.mistralModels();
            if (models.isEmpty()) {
                LOG.warning("LLM disabled: MISTRAL_MODELS is empty.");
                return;
            }
            client = new MistralClient(config.mistralApiBase(), key.get(), Duration.ofSeconds(15));
            LOG.info("LLM answers enabled with models " + models);
        } catch (IllegalArgumentException e) {
            LOG.warning("LLM disabled: " + e.getMessage());
        }
    }

    public boolean enabled() {
        return client != null;
    }

    public List<String> models() {
        return models;
    }

    public boolean paused() {
        return budget.paused(models);
    }

    /**
     * Outcome of {@link #answer}: {@code text} is empty when no usable answer came
     * back; {@code limited} says that was because of a usage limit or outage
     * (so the visitor can be told to retry), not because the answer was rejected.
     */
    public record Result(Optional<String> text, boolean limited) {
        static Result of(String text) {
            return new Result(Optional.of(text), false);
        }

        static Result none(boolean limited) {
            return new Result(Optional.empty(), limited);
        }
    }

    /**
     * An answer grounded in {@code sources}, citing them as [1]..[n] (indexes into
     * the list).
     */
    public Result answer(String question, List<ScoredChunk> sources, String clientId,
                                   String contactEmail) {
        if (client == null || sources.isEmpty()) {
            return Result.none(false);
        }
        String cacheKey = cacheKey(question, sources);
        Optional<String> cached = fromCache(cacheKey);
        if (cached.isPresent()) {
            return Result.of(cached.get());
        }
        if (!inFlight.tryAcquire()) {
            return Result.none(true);
        }
        List<MistralClient.Message> messages = List.of(
                new MistralClient.Message("system", PromptBuilder.systemPrompt(contactEmail)),
                new MistralClient.Message("user", PromptBuilder.userPrompt(question, sources)));
        try {
            // A model that is rate limited upstream is paused and the next one tried;
            // the budget bounds the attempts (each one counts against every limit).
            for (int attempt = 0; attempt < models.size(); attempt++) {
                Optional<String> model = budget.tryAcquire(clientId, models);
                if (model.isEmpty()) {
                    return Result.none(true);
                }
                try {
                    MistralClient.Completion completion = client.complete(model.get(), messages,
                            MAX_TOKENS, TEMPERATURE);
                    budget.recordTokens(completion.totalTokens());
                    Optional<String> text = PromptBuilder.sanitize(completion.text(), sources.size(),
                            allowedUrls(sources));
                    if (text.isEmpty()) {
                        LOG.info("LLM answer rejected by validation; using extractive answer.");
                    }
                    text.ifPresent(t -> putCache(cacheKey, t));
                    return new Result(text, false);
                } catch (MistralClient.LlmException e) {
                    if (!handleFailure(model.get(), e)) {
                        return Result.none(true);
                    }
                }
            }
            return Result.none(true);
        } finally {
            inFlight.release();
        }
    }

    /** Pauses what failed; true when another model may still be tried. */
    private boolean handleFailure(String model, MistralClient.LlmException e) {
        int status = e.status();
        if (status == 401) {
            budget.pause(PAUSE_AUTH_ERROR_MS);
            LOG.severe("Mistral rejected the API key (HTTP 401); LLM paused for 1 hour.");
            return false;
        }
        if (status == 403) {
            budget.pauseModel(model, PAUSE_AUTH_ERROR_MS);
            LOG.warning("Model " + model + " not available in this plan (HTTP 403); skipped for 1 hour.");
            return true;
        }
        if (status == 429) {
            budget.pauseModel(model, Math.max(PAUSE_RATE_LIMITED_MS, e.retryAfterSeconds() * 1000));
            LOG.info("Model " + model + " rate limited; trying the next one.");
            return true;
        }
        budget.pauseModel(model, PAUSE_SERVER_ERROR_MS);
        LOG.warning(e.getMessage() + " (" + model + "); model paused for 2 minutes.");
        return false;
    }

    private static String cacheKey(String question, List<ScoredChunk> sources) {
        String normalized = question.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").strip();
        String ids = sources.stream().map(s -> String.valueOf(s.chunk().getId())).collect(Collectors.joining(","));
        return normalized + "|" + ids;
    }

    private synchronized Optional<String> fromCache(String key) {
        CachedAnswer hit = cache.get(key);
        if (hit == null) {
            return Optional.empty();
        }
        if (hit.expiresAt() < System.currentTimeMillis()) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(hit.text());
    }

    private synchronized void putCache(String key, String text) {
        cache.put(key, new CachedAnswer(text, System.currentTimeMillis() + CACHE_TTL_MS));
    }

    private static Set<String> allowedUrls(List<ScoredChunk> sources) {
        Set<String> urls = new HashSet<>();
        for (ScoredChunk s : sources) {
            if (s.chunk().getUrl() != null) {
                urls.add(s.chunk().getUrl());
            }
        }
        return urls;
    }
}
