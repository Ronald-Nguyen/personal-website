package dev.ronaldnguyen.portfolio.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Tokenizer for English/German CV text that keeps technology names such as
 * C#, C++, ASP.NET or "full-stack" intact and removes stopwords.
 */
public final class Tokenizer {

    private record Replacement(Pattern pattern, String value) {
    }

    private static final List<Replacement> REPLACEMENTS = List.of(
            new Replacement(Pattern.compile("asp\\.net"), " aspnet "),
            new Replacement(Pattern.compile("\\.net\\b"), " dotnet "),
            new Replacement(Pattern.compile("c#"), " csharp "),
            new Replacement(Pattern.compile("f#"), " fsharp "),
            new Replacement(Pattern.compile("c\\+\\+"), " cplusplus "),
            new Replacement(Pattern.compile("node\\.js"), " nodejs "),
            new Replacement(Pattern.compile("ci/cd"), " cicd "),
            new Replacement(Pattern.compile("full[\\s-]?stack"), " fullstack "),
            new Replacement(Pattern.compile("front[\\s-]?end"), " frontend "),
            new Replacement(Pattern.compile("back[\\s-]?end"), " backend "));

    private static final Pattern SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+");

    /** Single-letter tokens worth keeping (programming languages). */
    private static final Set<String> SHORT_KEEP = Set.of("c", "r");

    static final Set<String> STOPWORDS = Set.copyOf(List.of(
            // English
            "a", "about", "above", "after", "again", "all", "also", "am", "an", "and", "any", "are", "as", "at",
            "be", "been", "being", "both", "but", "by", "can", "could", "did", "do", "does", "doing", "done",
            "during", "each", "either", "else", "ever", "for", "from", "further", "get", "got", "had", "has",
            "have", "having", "he", "her", "here", "hers", "him", "his", "how", "i", "if", "in", "into", "is",
            "it", "its", "just", "know", "like", "me", "more", "most", "much", "my", "no", "nor", "not", "of",
            "off", "on", "once", "only", "or", "other", "our", "out", "over", "own", "please", "same", "she",
            "should", "so", "some", "such", "tell", "than", "that", "the", "their", "them", "then", "there",
            "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "us", "very",
            "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will",
            "with", "would", "you", "your", "yours", "s", "t", "any", "anything", "something", "give",
            // German
            "aber", "als", "am", "an", "auch", "auf", "aus", "bei", "bin", "bis", "bist", "da", "das", "dass",
            "dem", "den", "der", "des", "die", "dies", "diese", "du", "durch", "ein", "eine", "einem", "einen",
            "einer", "er", "es", "für", "hat", "hatte", "ich", "ihr", "im", "in", "ist", "ja", "kann", "mit",
            "nach", "nicht", "noch", "oder", "sein", "seine", "seiner", "sich", "sie", "sind", "so", "über",
            "um", "und", "uns", "von", "vor", "war", "was", "welche", "wer", "wie", "wir", "wo", "zu", "zum",
            "zur", "hast", "habe", "haben", "wann", "welcher", "welches",
            // Always present in this corpus, carries no information
            "ronald", "nguyen"));

    private Tokenizer() {
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        for (Replacement r : REPLACEMENTS) {
            normalized = r.pattern().matcher(normalized).replaceAll(r.value());
        }
        List<String> tokens = new ArrayList<>();
        for (String raw : SPLIT.split(normalized)) {
            if (raw.isEmpty()) {
                continue;
            }
            if (raw.length() < 2 && !SHORT_KEEP.contains(raw)) {
                continue;
            }
            if (STOPWORDS.contains(raw)) {
                continue;
            }
            tokens.add(stem(raw));
        }
        return tokens;
    }

    /** Very light, conservative English plural stemming. */
    static String stem(String token) {
        if (token.length() <= 4 || !token.chars().allMatch(Character::isLetter)) {
            return token;
        }
        if (token.endsWith("ies")) {
            return token.substring(0, token.length() - 3) + "y";
        }
        if (token.endsWith("s") && !token.endsWith("ss") && !token.endsWith("us") && !token.endsWith("is")) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }
}
