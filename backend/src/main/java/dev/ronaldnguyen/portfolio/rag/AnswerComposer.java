package dev.ronaldnguyen.portfolio.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Builds a grounded, extractive answer: the most relevant sentences of the
 * retrieved chunks, grouped by source and annotated with [n] citations.
 * Nothing is generated beyond a short lead-in, so the answer can only
 * contain facts that are present in the knowledge base.
 */
public final class AnswerComposer {

    public record Answer(String text, List<ScoredChunk> sources, boolean found) {
    }

    static final double MIN_CHUNK_SCORE = 0.08;
    static final double RELATIVE_CHUNK_SCORE = 0.55;
    static final double RELATIVE_SENTENCE_SCORE = 0.3;
    static final int MAX_SOURCES = 4;
    static final int MAX_CHUNKS_PER_SOURCE = 2;
    /** The CV is the primary source; GitHub READMEs are long and would otherwise dominate. */
    static final double CV_BOOST = 1.15;
    static final int MAX_SENTENCES = 6;
    static final int MAX_CHARS = 1100;

    private static final Pattern SENTENCE_SPLIT =
            Pattern.compile("(?<=[.!?])\\s+(?=[\\p{Lu}\\p{N}\"„(])|\\n+");

    private record Candidate(int sourceRank, int position, String text, double score) {
    }

    private AnswerComposer() {
    }

    public static Answer compose(Map<String, Double> queryFeatures, List<ScoredChunk> hits,
                                 LocalEmbedder model, String contactEmail) {
        List<ScoredChunk> ranked = hits.stream()
                .sorted(Comparator.comparingDouble(AnswerComposer::rankScore).reversed())
                .toList();
        if (ranked.isEmpty() || ranked.get(0).score() < MIN_CHUNK_SCORE) {
            return notFound(contactEmail);
        }
        double threshold = Math.max(MIN_CHUNK_SCORE, rankScore(ranked.get(0)) * RELATIVE_CHUNK_SCORE);
        Map<String, Integer> perSource = new HashMap<>();
        List<ScoredChunk> sources = new ArrayList<>();
        for (ScoredChunk hit : ranked) {
            if (sources.size() >= MAX_SOURCES || rankScore(hit) < threshold) {
                break;
            }
            int used = perSource.merge(hit.chunk().getSourceKey(), 1, Integer::sum);
            if (used <= MAX_CHUNKS_PER_SOURCE) {
                sources.add(hit);
            }
        }

        List<Candidate> candidates = new ArrayList<>();
        Map<Integer, List<String>> sentencesBySource = new LinkedHashMap<>();
        for (int rank = 0; rank < sources.size(); rank++) {
            ScoredChunk source = sources.get(rank);
            List<String> sentences = splitSentences(source.chunk().getContent());
            sentencesBySource.put(rank, sentences);
            for (int pos = 0; pos < sentences.size(); pos++) {
                double score = sentenceScore(sentences.get(pos), queryFeatures, model)
                        * (0.5 + source.score());
                candidates.add(new Candidate(rank, pos, sentences.get(pos), score));
            }
        }

        double best = candidates.stream().mapToDouble(Candidate::score).max().orElse(0);
        if (best <= 0) {
            // The vector match came only from hash collisions: nothing really overlaps.
            return notFound(contactEmail);
        }

        Map<Integer, TreeSet<Integer>> chosen = new LinkedHashMap<>();
        int count = 0;
        int chars = 0;
        List<Candidate> byScore = candidates.stream()
                .filter(c -> c.score() > 0 && c.score() >= best * RELATIVE_SENTENCE_SCORE)
                .sorted(Comparator.comparingDouble(Candidate::score).reversed())
                .toList();
        for (Candidate c : byScore) {
            if (count >= MAX_SENTENCES || chars + c.text().length() > MAX_CHARS && count > 0) {
                break;
            }
            if (chosen.computeIfAbsent(c.sourceRank(), k -> new TreeSet<>()).add(c.position())) {
                count++;
                chars += c.text().length();
            }
        }
        // The first sentence of a chunk carries its context (who / where / when).
        for (Map.Entry<Integer, TreeSet<Integer>> e : chosen.entrySet()) {
            e.getValue().add(0);
        }

        List<ScoredChunk> cited = new ArrayList<>();
        List<String> paragraphs = new ArrayList<>();
        for (int rank = 0; rank < sources.size(); rank++) {
            TreeSet<Integer> positions = chosen.get(rank);
            if (positions == null || positions.isEmpty()) {
                continue;
            }
            cited.add(sources.get(rank));
            List<String> sentences = sentencesBySource.get(rank);
            StringBuilder paragraph = new StringBuilder();
            for (int pos : positions) {
                if (paragraph.length() > 0) {
                    paragraph.append(' ');
                }
                paragraph.append(sentences.get(pos));
            }
            paragraph.append(" [").append(cited.size()).append(']');
            paragraphs.add(paragraph.toString());
        }

        boolean fromGithub = cited.stream().anyMatch(s -> "github".equals(s.chunk().getSourceType()));
        boolean fromCv = cited.stream().anyMatch(s -> !"github".equals(s.chunk().getSourceType()));
        String lead = fromGithub && fromCv
                ? "Here's what I found in Ronald's CV and GitHub projects:"
                : fromGithub ? "Here's what I found in Ronald's GitHub projects:"
                : "Here's what I found in Ronald's CV:";
        return new Answer(lead + "\n\n" + String.join("\n\n", paragraphs), cited, true);
    }

    static double rankScore(ScoredChunk hit) {
        return "github".equals(hit.chunk().getSourceType()) ? hit.score() : hit.score() * CV_BOOST;
    }

    static List<String> splitSentences(String content) {
        List<String> result = new ArrayList<>();
        for (String s : SENTENCE_SPLIT.split(content)) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private static double sentenceScore(String sentence, Map<String, Double> queryFeatures, LocalEmbedder model) {
        List<String> tokens = Tokenizer.tokenize(sentence);
        if (tokens.isEmpty()) {
            return 0;
        }
        double score = 0;
        for (String feature : LocalEmbedder.features(tokens).keySet()) {
            Double weight = queryFeatures.get(feature);
            if (weight != null) {
                score += Math.min(weight, 1.0) * model.idf(feature);
            }
        }
        return score / (1.0 + tokens.size() / 25.0);
    }

    private static Answer notFound(String contactEmail) {
        return new Answer("I couldn't find anything about that in Ronald's CV or GitHub projects. "
                + "Try asking about his education, work experience, skills, projects or availability"
                + " — or contact him directly at " + contactEmail + ".", List.of(), false);
    }
}
