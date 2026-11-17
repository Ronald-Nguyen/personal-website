package dev.ronaldnguyen.portfolio.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Splits long text (READMEs) into overlapping, paragraph-aware chunks. */
public final class TextChunker {

    private static final Pattern PARAGRAPHS = Pattern.compile("\\n\\s*\\n");
    private static final Pattern SENTENCES = Pattern.compile("(?<=[.!?])\\s+");

    private TextChunker() {
    }

    public static List<String> chunk(String text, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        List<String> pieces = new ArrayList<>();
        for (String paragraph : PARAGRAPHS.split(text.trim())) {
            String p = paragraph.replaceAll("\\s+", " ").trim();
            if (p.isEmpty()) {
                continue;
            }
            if (p.length() <= maxChars) {
                pieces.add(p);
            } else {
                for (String sentence : SENTENCES.split(p)) {
                    for (int i = 0; i < sentence.length(); i += maxChars) {
                        pieces.add(sentence.substring(i, Math.min(sentence.length(), i + maxChars)));
                    }
                }
            }
        }

        StringBuilder current = new StringBuilder();
        for (String piece : pieces) {
            if (current.length() > 0 && current.length() + piece.length() + 1 > maxChars) {
                chunks.add(current.toString());
                current = new StringBuilder(tail(current.toString(), overlapChars));
            }
            if (current.length() > 0) {
                current.append(current.charAt(current.length() - 1) == ' ' ? "" : " ");
            }
            current.append(piece);
        }
        if (!current.toString().isBlank()) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    /** The last {@code n} characters, starting at a word boundary. */
    private static String tail(String text, int n) {
        if (n <= 0 || text.length() <= n) {
            return n <= 0 ? "" : text;
        }
        String tail = text.substring(text.length() - n);
        int space = tail.indexOf(' ');
        return space >= 0 ? tail.substring(space + 1) : tail;
    }
}
