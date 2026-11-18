package dev.ronaldnguyen.portfolio.github;

import dev.ronaldnguyen.portfolio.config.AppConfig;
import dev.ronaldnguyen.portfolio.dto.ProjectDto;
import dev.ronaldnguyen.portfolio.entity.GithubRepo;
import dev.ronaldnguyen.portfolio.rag.ChunkDraft;
import dev.ronaldnguyen.portfolio.rag.IndexService;
import dev.ronaldnguyen.portfolio.rag.TextChunker;
import dev.ronaldnguyen.portfolio.repository.ChunkRepository;
import dev.ronaldnguyen.portfolio.repository.GithubRepoRepository;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches public repositories from the GitHub REST API, caches them in
 * PostgreSQL and indexes descriptions and READMEs into the knowledge base.
 */
@ApplicationScoped
public class GithubService {

    public static final String SOURCE_TYPE = "github";

    private static final Logger LOG = Logger.getLogger(GithubService.class.getName());
    private static final int MAX_README_CHARS = 20_000;
    private static final int MAX_README_CHUNKS = 4;
    private static final int FORK_MIN_STARS = 3;
    private static final Duration FORK_MIN_OWN_ACTIVITY = Duration.ofDays(1);

    @Inject
    AppConfig config;

    @Inject
    GithubRepoRepository repos;

    @Inject
    ChunkRepository chunks;

    @Inject
    IndexService index;

    @Resource
    ManagedScheduledExecutorService scheduler;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private volatile boolean started;
    private volatile Instant lastSuccess;
    private volatile Instant lastAttempt;
    private volatile String lastError;

    /** Starts the periodic refresh (first run immediately). Idempotent. */
    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        long hours = config.githubRefreshHours();
        scheduler.scheduleWithFixedDelay(this::refreshQuietly, 0, hours, TimeUnit.HOURS);
    }

    public List<ProjectDto> projects() {
        List<GithubRepo> cached = repos.findAllOrdered();
        if (cached.isEmpty() && lastSuccess == null
                && (lastAttempt == null || lastAttempt.isBefore(Instant.now().minusSeconds(60)))) {
            // First request before the background sync finished: fetch now (throttled).
            refreshQuietly();
            cached = repos.findAllOrdered();
        }
        return cached.stream().map(GithubService::toDto).toList();
    }

    public String lastError() {
        return lastError;
    }

    public Instant lastSuccess() {
        return lastSuccess;
    }

    private void refreshQuietly() {
        lastAttempt = Instant.now();
        try {
            refresh();
        } catch (Exception e) {
            lastError = e.getMessage();
            LOG.log(Level.WARNING, "GitHub refresh failed (serving cached projects): " + e.getMessage());
        }
    }

    public synchronized void refresh() throws IOException, InterruptedException {
        String user = config.githubUsername();
        String url = config.githubApiBase() + "/users/" + URLEncoder.encode(user, StandardCharsets.UTF_8)
                + "/repos?sort=updated&per_page=" + config.githubRepoLimit();
        HttpResponse<String> response = send(url, "application/vnd.github+json");
        if (response.statusCode() != 200) {
            throw new IOException("GitHub API returned HTTP " + response.statusCode() + " for " + url);
        }

        JsonArray array;
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            array = reader.readArray();
        }

        Instant now = Instant.now();
        List<String> excluded = config.githubExcludedRepos();
        Map<String, String> demoOverrides = config.githubDemoOverrides();
        List<GithubRepo> result = new ArrayList<>();
        for (JsonValue value : array) {
            JsonObject json = value.asJsonObject();
            if (isExcluded(json, excluded) || !include(json)) {
                continue;
            }
            GithubRepo repo = new GithubRepo();
            repo.setId(json.getJsonNumber("id").longValue());
            repo.setName(json.getString("name"));
            repo.setFullName(json.getString("full_name"));
            repo.setDescription(json.getString("description", null));
            repo.setHtmlUrl(json.getString("html_url"));
            repo.setHomepage(demoOverrides.getOrDefault(repo.getName().toLowerCase(Locale.ROOT),
                    emptyToNull(json.getString("homepage", null))));
            repo.setLanguage(json.getString("language", null));
            repo.setStars(json.getInt("stargazers_count", 0));
            repo.setForks(json.getInt("forks_count", 0));
            repo.setFork(json.getBoolean("fork", false));
            repo.setTopics(String.join(",", topics(json)));
            repo.setPushedAt(instant(json.getString("pushed_at", null)));
            repo.setUpdatedAt(instant(json.getString("updated_at", null)));
            repo.setReadme(fetchReadme(repo.getFullName()));
            repo.setSortOrder(result.size());
            repo.setFetchedAt(now);
            result.add(repo);
        }

        repos.replaceAll(result);
        boolean changed = chunks.sync(SOURCE_TYPE, toChunks(result));
        if (changed) {
            index.reindex();
        }
        lastSuccess = now;
        lastError = null;
        LOG.info(() -> "GitHub sync done: " + result.size() + " repositories" + (changed ? ", knowledge base updated" : ""));
    }

    static boolean isExcluded(JsonObject json, List<String> excluded) {
        String name = json.getString("name", "");
        return excluded.stream().anyMatch(name::equalsIgnoreCase);
    }

    /** Non-forks always; forks only with notable stars or own commits after forking. */
    static boolean include(JsonObject json) {
        if (json.getBoolean("archived", false) && json.getString("description", null) == null) {
            return false;
        }
        if (!json.getBoolean("fork", false)) {
            return true;
        }
        if (json.getInt("stargazers_count", 0) >= FORK_MIN_STARS) {
            return true;
        }
        Instant created = instant(json.getString("created_at", null));
        Instant pushed = instant(json.getString("pushed_at", null));
        return created != null && pushed != null && pushed.isAfter(created.plus(FORK_MIN_OWN_ACTIVITY));
    }

    private String fetchReadme(String fullName) {
        try {
            HttpResponse<String> response = send(config.githubApiBase() + "/repos/" + fullName + "/readme",
                    "application/vnd.github.raw+json");
            if (response.statusCode() != 200) {
                return null;
            }
            String body = response.body();
            return body.length() > MAX_README_CHARS ? body.substring(0, MAX_README_CHARS) : body;
        } catch (IOException e) {
            LOG.fine(() -> "README for " + fullName + " unavailable: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private HttpResponse<String> send(String url, String accept) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", accept)
                .header("User-Agent", "portfolio-backend")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET();
        config.githubToken().ifPresent(token -> builder.header("Authorization", "Bearer " + token));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    static List<ChunkDraft> toChunks(List<GithubRepo> repos) {
        List<ChunkDraft> drafts = new ArrayList<>();
        for (GithubRepo repo : repos) {
            String key = "github:" + repo.getFullName();
            String title = "GitHub project: " + repo.getName();
            StringBuilder summary = new StringBuilder();
            summary.append("Ronald's GitHub project \"").append(repo.getName()).append("\"");
            if (repo.getDescription() != null && !repo.getDescription().isBlank()) {
                summary.append(" is described as: ").append(repo.getDescription().trim());
                if (!repo.getDescription().trim().matches(".*[.!?]$")) {
                    summary.append('.');
                }
            } else {
                summary.append(" is a public repository on GitHub.");
            }
            if (repo.getLanguage() != null) {
                summary.append(" Its primary programming language is ").append(repo.getLanguage()).append('.');
            }
            if (repo.getTopics() != null && !repo.getTopics().isBlank()) {
                summary.append(" Topics: ").append(repo.getTopics().replace(",", ", ")).append('.');
            }
            summary.append(" The repository has ").append(repo.getStars())
                    .append(repo.getStars() == 1 ? " star." : " stars.");
            drafts.add(new ChunkDraft(SOURCE_TYPE, key, "Projects", title, repo.getHtmlUrl(), 0, summary.toString()));

            List<String> readmeChunks = TextChunker.chunk(ReadmeCleaner.clean(repo.getReadme()), 700, 100);
            for (int i = 0; i < Math.min(MAX_README_CHUNKS, readmeChunks.size()); i++) {
                String content = "From the README of " + repo.getName() + ". " + readmeChunks.get(i);
                drafts.add(new ChunkDraft(SOURCE_TYPE, key, "Projects", title + " (README)",
                        repo.getHtmlUrl(), i + 1, content));
            }
        }
        return drafts;
    }

    static ProjectDto toDto(GithubRepo r) {
        List<String> topics = r.getTopics() == null || r.getTopics().isBlank()
                ? List.of() : Arrays.asList(r.getTopics().split(","));
        return new ProjectDto(r.getId(), r.getName(), r.getDescription(), r.getHtmlUrl(), r.getHomepage(),
                r.getLanguage(), r.getStars(), r.getForks(), topics, r.isFork(),
                r.getPushedAt() == null ? null : r.getPushedAt().toString());
    }

    private static List<String> topics(JsonObject json) {
        JsonArray topics = json.getJsonArray("topics");
        if (topics == null) {
            return List.of();
        }
        return topics.getValuesAs(JsonString.class).stream().map(JsonString::getString).toList();
    }

    private static Instant instant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
