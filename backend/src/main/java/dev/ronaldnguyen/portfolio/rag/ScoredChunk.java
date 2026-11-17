package dev.ronaldnguyen.portfolio.rag;

import dev.ronaldnguyen.portfolio.entity.KnowledgeChunk;

/** A retrieved chunk together with its cosine similarity to the query. */
public record ScoredChunk(KnowledgeChunk chunk, double score) {
}
