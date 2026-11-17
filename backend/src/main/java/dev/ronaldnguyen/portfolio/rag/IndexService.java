package dev.ronaldnguyen.portfolio.rag;

import dev.ronaldnguyen.portfolio.entity.KnowledgeChunk;
import dev.ronaldnguyen.portfolio.repository.ChunkRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Keeps the pgvector embeddings in sync with the knowledge base. Whenever
 * chunks change, IDF statistics are recomputed and every chunk is
 * re-embedded (cheap for a few hundred chunks).
 */
@ApplicationScoped
public class IndexService {

    private static final Logger LOG = Logger.getLogger(IndexService.class.getName());

    @Inject
    ChunkRepository chunks;

    private volatile LocalEmbedder model;

    public static String documentText(KnowledgeChunk chunk) {
        return chunk.getSection() + " " + chunk.getTitle() + " " + chunk.getContent();
    }

    public synchronized void reindex() {
        List<KnowledgeChunk> all = chunks.findAll();
        Map<Long, List<String>> tokens = new LinkedHashMap<>();
        for (KnowledgeChunk chunk : all) {
            tokens.put(chunk.getId(), Tokenizer.tokenize(documentText(chunk)));
        }
        LocalEmbedder fitted = LocalEmbedder.fit(tokens.values());
        Map<Long, float[]> embeddings = new LinkedHashMap<>();
        tokens.forEach((id, t) -> embeddings.put(id, fitted.embedDocument(t)));
        chunks.updateEmbeddings(embeddings);
        model = fitted;
        LOG.info(() -> "Knowledge base indexed: " + all.size() + " chunks");
    }

    public Optional<LocalEmbedder> model() {
        return Optional.ofNullable(model);
    }
}
