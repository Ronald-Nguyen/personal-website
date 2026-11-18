package dev.ronaldnguyen.portfolio.rag;

import dev.ronaldnguyen.portfolio.dto.ChatResponse;
import dev.ronaldnguyen.portfolio.dto.SourceDto;
import dev.ronaldnguyen.portfolio.entity.KnowledgeChunk;
import dev.ronaldnguyen.portfolio.github.GithubService;
import dev.ronaldnguyen.portfolio.llm.LlmService;
import dev.ronaldnguyen.portfolio.llm.PromptBuilder;
import dev.ronaldnguyen.portfolio.repository.ChunkRepository;
import dev.ronaldnguyen.portfolio.seed.ProfileService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServiceUnavailableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Question answering over the website's knowledge base (CV + GitHub projects).
 *
 * <p>With an LLM configured, every question goes to the model together with
 * the complete CV, all project summaries and the README passages that
 * retrieval ranks highest for the question, so the model can answer freely
 * (combine, summarise, calculate) from the whole site. Without an LLM, or when
 * its budget is used up, the extractive answer from {@link AnswerComposer} is
 * returned instead.
 */
@ApplicationScoped
public class RagService {

    static final int TOP_K = 10;
    /** README passages added on top of the always-included CV and project summaries. */
    static final int MAX_README_PASSAGES = 5;
    static final double MIN_README_SCORE = 0.05;
    /** Upper bound for the context sent to the model (~6k tokens). */
    static final int MAX_CONTEXT_CHARS = 24_000;
    private static final int SNIPPET_LENGTH = 240;
    static final String LIMIT_NOTICE = "The AI assistant has reached its usage limit for the moment, "
            + "so this answer is quoted directly from the CV. Try again in a minute for a full answer.";
    /** Used when a question has no searchable words, e.g. "Who is Ronald?". */
    private static final String GENERIC_QUERY = "about master student full-stack AI software developer based specializing";

    @Inject
    IndexService index;

    @Inject
    ChunkRepository chunks;

    @Inject
    ProfileService profile;

    @Inject
    LlmService llm;

    public ChatResponse ask(String question, String clientId) {
        LocalEmbedder model = index.model()
                .orElseThrow(() -> new ServiceUnavailableException("The knowledge base is still being indexed."));

        Map<String, Double> features = queryFeatures(question);
        float[] vector = model.embed(features);
        List<ScoredChunk> hits = vector == null ? List.of() : chunks.search(vector, TOP_K);

        boolean limited = false;
        if (llm.enabled()) {
            List<ScoredChunk> context = buildContext(chunks.findAll(), hits);
            LlmService.Result result = llm.answer(question, context, clientId, profile.email());
            if (result.text().isPresent()) {
                return generatedResponse(result.text().get(), context);
            }
            limited = result.limited();
        }

        AnswerComposer.Answer answer = AnswerComposer.compose(features, hits, model, profile.email());
        List<SourceDto> sources = new ArrayList<>();
        for (int i = 0; i < answer.sources().size(); i++) {
            sources.add(toDto(i + 1, answer.sources().get(i)));
        }
        return new ChatResponse(answer.text(), sources, answer.found(), false, limited ? LIMIT_NOTICE : null);
    }

    static Map<String, Double> queryFeatures(String question) {
        List<String> tokens = Tokenizer.tokenize(question);
        return QueryExpander.expand(tokens.isEmpty() ? Tokenizer.tokenize(GENERIC_QUERY) : tokens);
    }

    private static ChatResponse generatedResponse(String text, List<ScoredChunk> context) {
        PromptBuilder.Renumbered renumbered = PromptBuilder.renumber(text);
        List<SourceDto> sources = new ArrayList<>();
        for (int original : renumbered.sourceIndexes()) {
            sources.add(toDto(sources.size() + 1, context.get(original - 1)));
        }
        return new ChatResponse(renumbered.text(), sources, !sources.isEmpty(), true, null);
    }

    /**
     * The whole CV and every project summary, followed by the best matching
     * README passages. CV first, so its source numbers are stable and low.
     */
    static List<ScoredChunk> buildContext(List<KnowledgeChunk> all, List<ScoredChunk> hits) {
        Map<Long, Double> scores = new HashMap<>();
        for (ScoredChunk hit : hits) {
            if (hit.chunk().getId() != null) {
                scores.put(hit.chunk().getId(), hit.score());
            }
        }
        Set<KnowledgeChunk> ordered = new LinkedHashSet<>();
        all.stream().filter(c -> !isGithub(c)).forEach(ordered::add);
        all.stream().filter(c -> isGithub(c) && c.getChunkIndex() == 0).forEach(ordered::add);
        hits.stream()
                .filter(h -> isGithub(h.chunk()) && h.chunk().getChunkIndex() > 0 && h.score() >= MIN_README_SCORE)
                .limit(MAX_README_PASSAGES)
                .forEach(h -> ordered.add(h.chunk()));

        List<ScoredChunk> context = new ArrayList<>();
        int chars = 0;
        for (KnowledgeChunk c : ordered) {
            int size = c.getContent().length() + 80;
            if (chars + size > MAX_CONTEXT_CHARS) {
                continue;
            }
            chars += size;
            context.add(new ScoredChunk(c, scores.getOrDefault(c.getId(), 0.0)));
        }
        return context;
    }

    private static boolean isGithub(KnowledgeChunk c) {
        return GithubService.SOURCE_TYPE.equals(c.getSourceType());
    }

    private static SourceDto toDto(int index, ScoredChunk hit) {
        return new SourceDto(index, hit.chunk().getSection(), hit.chunk().getTitle(),
                snippet(hit.chunk().getContent()), Math.round(hit.score() * 1000) / 1000.0, hit.chunk().getUrl());
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
