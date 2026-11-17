package dev.ronaldnguyen.portfolio.rag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Local, dependency-free text embedding: TF-IDF over word unigrams and
 * bigrams. Every vocabulary term of the corpus gets its own dimension (no
 * hashing, so no collisions); vectors are L2-normalised and stored in
 * pgvector, where they are compared with cosine distance.
 *
 * <p>An instance is immutable; it captures the vocabulary and IDF statistics
 * of the corpus it was fitted on. The vector length equals the vocabulary
 * size, capped at {@link #MAX_DIMENSIONS} (pgvector's limit is 16000).
 */
public final class LocalEmbedder {

    public static final int MAX_DIMENSIONS = 16_000;

    private final Map<String, Integer> documentFrequency;
    private final Map<String, Integer> vocabulary;
    private final int documentCount;

    private LocalEmbedder(Map<String, Integer> documentFrequency, Map<String, Integer> vocabulary,
                          int documentCount) {
        this.documentFrequency = documentFrequency;
        this.vocabulary = vocabulary;
        this.documentCount = documentCount;
    }

    /** Builds vocabulary and IDF statistics from tokenized documents. */
    public static LocalEmbedder fit(Collection<List<String>> documents) {
        Map<String, Integer> df = new HashMap<>();
        for (List<String> tokens : documents) {
            Set<String> unique = new HashSet<>(features(tokens).keySet());
            for (String feature : unique) {
                df.merge(feature, 1, Integer::sum);
            }
        }
        // Unigrams first, then bigrams; within each group frequent terms first.
        List<String> terms = new ArrayList<>(df.keySet());
        terms.sort(Comparator.<String>comparingInt(t -> t.indexOf(' ') >= 0 ? 1 : 0)
                .thenComparing(t -> -df.get(t))
                .thenComparing(Comparator.naturalOrder()));
        Map<String, Integer> vocabulary = new HashMap<>();
        for (String term : terms) {
            if (vocabulary.size() >= MAX_DIMENSIONS) {
                break;
            }
            vocabulary.put(term, vocabulary.size());
        }
        df.keySet().retainAll(vocabulary.keySet());
        return new LocalEmbedder(Map.copyOf(df), Map.copyOf(vocabulary), documents.size());
    }

    /** Term counts of unigrams and adjacent bigrams. */
    public static Map<String, Double> features(List<String> tokens) {
        Map<String, Double> counts = new LinkedHashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            counts.merge(tokens.get(i), 1.0, Double::sum);
            if (i + 1 < tokens.size()) {
                counts.merge(tokens.get(i) + " " + tokens.get(i + 1), 1.0, Double::sum);
            }
        }
        return counts;
    }

    public int dimensions() {
        return vocabulary.size();
    }

    public boolean knows(String feature) {
        return vocabulary.containsKey(feature);
    }

    public double idf(String feature) {
        int df = documentFrequency.getOrDefault(feature, 0);
        return Math.log((1.0 + documentCount) / (1.0 + df)) + 1.0;
    }

    /**
     * Embeds weighted features. Weights {@code >= 1} are treated as raw term
     * counts (sublinear tf), smaller weights are used as-is (query expansion).
     * Features outside the vocabulary are ignored.
     *
     * @return the normalised vector, or {@code null} if no feature contributed
     */
    public float[] embed(Map<String, Double> weightedFeatures) {
        if (vocabulary.isEmpty()) {
            return null;
        }
        double[] vector = new double[vocabulary.size()];
        double norm = 0;
        for (Map.Entry<String, Double> e : weightedFeatures.entrySet()) {
            Integer index = vocabulary.get(e.getKey());
            double count = e.getValue();
            if (index == null || count <= 0) {
                continue;
            }
            double tf = count >= 1 ? 1.0 + Math.log(count) : count;
            vector[index] += tf * idf(e.getKey());
        }
        for (double v : vector) {
            norm += v * v;
        }
        if (norm == 0) {
            return null;
        }
        norm = Math.sqrt(norm);
        float[] result = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = (float) (vector[i] / norm);
        }
        return result;
    }

    public float[] embedDocument(List<String> tokens) {
        return embed(features(tokens));
    }

    public static double cosine(float[] a, float[] b) {
        double dot = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot; // both vectors are L2-normalised
    }
}
