package dev.ronaldnguyen.portfolio.web;

import dev.ronaldnguyen.portfolio.github.GithubService;
import dev.ronaldnguyen.portfolio.llm.LlmService;
import dev.ronaldnguyen.portfolio.repository.ChunkRepository;
import dev.ronaldnguyen.portfolio.seed.KnowledgeSeeder;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Lightweight status for humans: GET /api/status. */
@Path("/status")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class StatusResource {

    @Inject
    KnowledgeSeeder seeder;

    @Inject
    ChunkRepository chunks;

    @Inject
    GithubService github;

    @Inject
    LlmService llm;

    @GET
    public JsonObject status() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("ready", seeder.isReady())
                .add("knowledgeChunks", chunks.count())
                .add("retrieval", "local TF-IDF embeddings, cosine search in pgvector");
        // Never expose the key; only whether generation is active.
        builder.add("llm", llm.enabled() ? (llm.budget().paused() ? "paused" : "enabled") : "disabled");
        llm.model().ifPresent(m -> builder.add("llmModel", m));
        if (github.lastSuccess() != null) {
            builder.add("githubLastSync", github.lastSuccess().toString());
        }
        if (github.lastError() != null) {
            builder.add("githubLastError", github.lastError());
        }
        return builder.build();
    }
}
