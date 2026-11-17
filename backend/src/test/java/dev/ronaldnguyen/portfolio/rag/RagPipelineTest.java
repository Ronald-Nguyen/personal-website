package dev.ronaldnguyen.portfolio.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ronaldnguyen.portfolio.entity.KnowledgeChunk;
import dev.ronaldnguyen.portfolio.github.ReadmeCleaner;
import dev.ronaldnguyen.portfolio.seed.ProfileChunker;
import dev.ronaldnguyen.portfolio.seed.ProfileService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Runs the full local RAG pipeline on the real CV data with an in-memory
 * cosine search that mirrors the pgvector query.
 */
class RagPipelineTest {

    private static List<KnowledgeChunk> chunks;
    private static List<float[]> vectors;
    private static LocalEmbedder model;

    @BeforeAll
    static void index() {
        chunks = new ArrayList<>();
        for (ChunkDraft d : ProfileChunker.chunks(ProfileService.load())) {
            KnowledgeChunk c = new KnowledgeChunk();
            c.setSourceType(d.sourceType());
            c.setSourceKey(d.sourceKey());
            c.setSection(d.section());
            c.setTitle(d.title());
            c.setUrl(d.url());
            c.setContent(d.content());
            chunks.add(c);
        }
        List<List<String>> tokens = chunks.stream().map(c -> Tokenizer.tokenize(IndexService.documentText(c))).toList();
        model = LocalEmbedder.fit(tokens);
        vectors = tokens.stream().map(model::embedDocument).toList();
    }

    private static AnswerComposer.Answer ask(String question) {
        Map<String, Double> features = QueryExpander.expand(Tokenizer.tokenize(question));
        float[] q = model.embed(features);
        List<ScoredChunk> hits = new ArrayList<>();
        if (q != null) {
            for (int i = 0; i < chunks.size(); i++) {
                if (vectors.get(i) != null) {
                    hits.add(new ScoredChunk(chunks.get(i), LocalEmbedder.cosine(q, vectors.get(i))));
                }
            }
        }
        hits.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        AnswerComposer.Answer answer = AnswerComposer.compose(features, hits.subList(0, Math.min(6, hits.size())),
                model, "ronaldnguyen314@gmail.com");
        System.out.println("Q: " + question);
        hits.stream().limit(3).forEach(h -> System.out.printf("   %.3f %s%n", h.score(), h.chunk().getSourceKey()));
        System.out.println(answer.text() + "\n");
        return answer;
    }

    private static String topKey(AnswerComposer.Answer answer) {
        return answer.sources().get(0).chunk().getSourceKey();
    }

    @Test
    void tokenizerKeepsTechTerms() {
        List<String> tokens = Tokenizer.tokenize("C#/ASP.NET, C++ and full-stack .NET with R and C");
        assertTrue(tokens.containsAll(List.of("csharp", "aspnet", "cplusplus", "fullstack", "dotnet", "r", "c")),
                tokens.toString());
    }

    @Test
    void bachelorThesis() {
        AnswerComposer.Answer a = ask("What was Ronald's Bachelor thesis about?");
        assertTrue(a.found());
        assertEquals("cv:thesis:bsc", topKey(a));
        assertTrue(a.text().contains("Empirical Evaluation of LLM-Generated Code Transformations"));
    }

    @Test
    void csharpFullStack() {
        AnswerComposer.Answer a = ask("Does he have experience with C# and full-stack development?");
        assertTrue(a.found());
        List<String> top2 = a.sources().stream().limit(2).map(s -> s.chunk().getSourceKey()).toList();
        assertTrue(top2.containsAll(List.of("cv:experience:gecko", "cv:experience:confovis")), top2.toString());
    }

    @Test
    void programmingLanguages() {
        AnswerComposer.Answer a = ask("Which programming languages does he know?");
        assertTrue(a.found());
        assertEquals("cv:skills:programming-languages", topKey(a));
    }

    @Test
    void moreQuestions() {
        assertEquals("cv:experience:confovis", topKey(ask("Where does he currently work?")));
        assertEquals("cv:languages", topKey(ask("Does he speak German?")));
        assertTrue(ask("What is his GPA in the master?").text().contains("1.9"));
        assertTrue(ask("How can I contact him?").text().contains("ronaldnguyen314@gmail.com"));
    }

    @Test
    void offTopic() {
        assertFalse(ask("What's the weather like in Paris tomorrow?").found());
        assertFalse(ask("asdfgh qwertz").found());
    }

    @Test
    void readmeCleaning() {
        String md = "# My App\n\n![badge](http://x/y.svg) A **cool** [tool](http://x) for `devs`.\n\n```java\ncode();\n```\n- item one\n";
        String clean = ReadmeCleaner.clean(md);
        assertFalse(clean.contains("code()"));
        assertFalse(clean.contains("badge"));
        assertTrue(clean.contains("A cool tool for devs."), clean);
        assertTrue(TextChunker.chunk("a ".repeat(1000), 700, 100).size() > 1);
    }
}
