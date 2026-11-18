package dev.ronaldnguyen.portfolio.rag;

import dev.ronaldnguyen.portfolio.dto.ChatResponse;
import dev.ronaldnguyen.portfolio.dto.SourceDto;
import dev.ronaldnguyen.portfolio.llm.LlmService;
import dev.ronaldnguyen.portfolio.repository.ChunkRepository;
import dev.ronaldnguyen.portfolio.seed.ProfileService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServiceUnavailableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Retrieval-augmented question answering over the local knowledge base. */
@ApplicationScoped
public class RagService {

    static final int TOP_K = 10;
    private static final int SNIPPET_LENGTH = 240;

    @Inject
    IndexService index;

    @Inject
    ChunkRepository chunks;

    @Inject
    ProfileService profile;

    @Inject
    LlmService llm;

    /**
     * Answers from the knowledge base. Only when retrieval found relevant
     * sources is the LLM asked to phrase the answer from exactly those
     * sources; otherwise (or if the LLM is unavailable) the extractive
     * answer is returned.
     */
    public ChatResponse ask(String question, String clientId) {
        LocalEmbedder model = index.model()
                .orElseThrow(() -> new ServiceUnavailableException("The knowledge base is still being indexed."));

        Map<String, Double> features = QueryExpander.expand(Tokenizer.tokenize(question));
        float[] vector = model.embed(features);
        List<ScoredChunk> hits = vector == null ? List.of() : chunks.search(vector, TOP_K);

        AnswerComposer.Answer answer = AnswerComposer.compose(features, hits, model, profile.email());
        List<SourceDto> sources = new ArrayList<>();
        for (int i = 0; i < answer.sources().size(); i++) {
            ScoredChunk hit = answer.sources().get(i);
            sources.add(new SourceDto(i + 1, hit.chunk().getSection(), hit.chunk().getTitle(),
                    snippet(hit.chunk().getContent()), Math.round(hit.score() * 1000) / 1000.0,
                    hit.chunk().getUrl()));
        }
        if (!answer.found()) {
            return new ChatResponse(answer.text(), sources, false, false);
        }
        return llm.answer(question, answer.sources(), clientId, profile.email())
                .map(text -> new ChatResponse(text, sources, true, true))
                .orElseGet(() -> new ChatResponse(answer.text(), sources, true, false));
    }

    private static String snippet(String content) {
        String flat = content.replaceAll("\\s+", " ").trim();
        if (flat.length() <= SNIPPET_LENGTH) {
            return flat;
        }
        int cut = flat.lastIndexOf(' ', SNIPPET_LENGTH);
        return flat.substring(0, cut > 0 ? cut : SNIPPET_LENGTH) + " …";
    }
}
