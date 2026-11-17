package dev.ronaldnguyen.portfolio.repository;

import dev.ronaldnguyen.portfolio.entity.KnowledgeChunk;
import dev.ronaldnguyen.portfolio.rag.ChunkDraft;
import dev.ronaldnguyen.portfolio.rag.ScoredChunk;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ChunkRepository {

    @PersistenceContext(unitName = "portfolio")
    EntityManager em;

    @Transactional
    public List<KnowledgeChunk> findAll() {
        return em.createQuery("SELECT c FROM KnowledgeChunk c ORDER BY c.id", KnowledgeChunk.class)
                .getResultList();
    }

    @Transactional
    public long count() {
        return em.createQuery("SELECT COUNT(c) FROM KnowledgeChunk c", Long.class).getSingleResult();
    }

    /**
     * Makes the stored chunks of one source type match {@code drafts}:
     * inserts new ones, updates changed ones (by content hash) and deletes
     * chunks that no longer exist.
     *
     * @return true if anything changed
     */
    @Transactional
    public boolean sync(String sourceType, List<ChunkDraft> drafts) {
        Map<String, KnowledgeChunk> existing = em.createQuery(
                        "SELECT c FROM KnowledgeChunk c WHERE c.sourceType = :type", KnowledgeChunk.class)
                .setParameter("type", sourceType)
                .getResultList().stream()
                .collect(Collectors.toMap(c -> c.getSourceKey() + "#" + c.getChunkIndex(),
                        Function.identity(), (a, b) -> a, LinkedHashMap::new));

        boolean changed = false;
        Instant now = Instant.now();
        for (ChunkDraft draft : drafts) {
            String hash = sha256(draft.section() + "\n" + draft.title() + "\n"
                    + draft.url() + "\n" + draft.content());
            KnowledgeChunk chunk = existing.remove(draft.key());
            if (chunk != null && chunk.getContentHash().equals(hash)) {
                continue;
            }
            if (chunk == null) {
                chunk = new KnowledgeChunk();
                chunk.setSourceType(sourceType);
                chunk.setSourceKey(draft.sourceKey());
                chunk.setChunkIndex(draft.chunkIndex());
            }
            chunk.setSection(draft.section());
            chunk.setTitle(draft.title());
            chunk.setUrl(draft.url());
            chunk.setContent(draft.content());
            chunk.setContentHash(hash);
            chunk.setUpdatedAt(now);
            if (chunk.getId() == null) {
                em.persist(chunk);
            }
            changed = true;
        }
        for (KnowledgeChunk stale : existing.values()) {
            em.remove(stale);
            changed = true;
        }
        return changed;
    }

    /** Writes embeddings with native SQL; a null vector clears the column. */
    @Transactional
    public void updateEmbeddings(Map<Long, float[]> embeddings) {
        for (Map.Entry<Long, float[]> entry : embeddings.entrySet()) {
            if (entry.getValue() == null) {
                em.createNativeQuery("UPDATE knowledge_chunk SET embedding = NULL WHERE id = ?1")
                        .setParameter(1, entry.getKey())
                        .executeUpdate();
            } else {
                em.createNativeQuery("UPDATE knowledge_chunk SET embedding = CAST(?1 AS vector) WHERE id = ?2")
                        .setParameter(1, toVectorLiteral(entry.getValue()))
                        .setParameter(2, entry.getKey())
                        .executeUpdate();
            }
        }
    }

    /**
     * Exact cosine-similarity search in pgvector. The dimension filter guards
     * against the short window in which a reindex changed the vocabulary size.
     */
    @Transactional
    public List<ScoredChunk> search(float[] query, int limit) {
        String literal = toVectorLiteral(query);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "SELECT id, 1 - (embedding <=> CAST(?1 AS vector)) AS score "
                                + "FROM knowledge_chunk "
                                + "WHERE embedding IS NOT NULL AND vector_dims(embedding) = ?3 "
                                + "ORDER BY embedding <=> CAST(?1 AS vector) LIMIT ?2")
                .setParameter(1, literal)
                .setParameter(2, limit)
                .setParameter(3, query.length)
                .getResultList();
        List<ScoredChunk> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            long id = ((Number) row[0]).longValue();
            double score = ((Number) row[1]).doubleValue();
            KnowledgeChunk chunk = em.find(KnowledgeChunk.class, id);
            if (chunk != null && !Double.isNaN(score)) {
                result.add(new ScoredChunk(chunk, score));
            }
        }
        return result;
    }

    static String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 8);
        sb.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            if (vector[i] == 0f) {
                sb.append('0');
            } else {
                sb.append(vector[i]);
            }
        }
        return sb.append(']').toString();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
