package dev.ronaldnguyen.portfolio.github;

import java.util.regex.Pattern;

/** Reduces Markdown/HTML README content to plain prose for indexing. */
public final class ReadmeCleaner {

    private static final Pattern FENCED_CODE = Pattern.compile("(?s)```.*?```|~~~.*?~~~");
    private static final Pattern HTML_COMMENT = Pattern.compile("(?s)<!--.*?-->");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]]*]\\([^)]*\\)");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]*)]\\([^)]*\\)");
    private static final Pattern REF_LINK_DEF = Pattern.compile("(?m)^\\s*\\[[^\\]]+]:\\s*\\S+.*$");
    private static final Pattern HEADING = Pattern.compile("(?m)^\\s{0,3}#{1,6}\\s*(.*?)\\s*#*\\s*$");
    private static final Pattern LIST_MARKER = Pattern.compile("(?m)^\\s*(?:[-*+]|\\d+[.)])\\s+");
    private static final Pattern TABLE_RULE = Pattern.compile("(?m)^\\s*\\|?\\s*:?-{2,}.*$");
    private static final Pattern EMPHASIS = Pattern.compile("[*_`]{1,3}");
    private static final Pattern URL = Pattern.compile("https?://\\S+");

    private ReadmeCleaner() {
    }

    public static String clean(String markdown) {
        if (markdown == null) {
            return "";
        }
        String text = markdown.replace("\r\n", "\n");
        text = FENCED_CODE.matcher(text).replaceAll("\n");
        text = HTML_COMMENT.matcher(text).replaceAll("");
        text = IMAGE.matcher(text).replaceAll("");
        text = LINK.matcher(text).replaceAll("$1");
        text = REF_LINK_DEF.matcher(text).replaceAll("");
        text = HTML_TAG.matcher(text).replaceAll(" ");
        text = TABLE_RULE.matcher(text).replaceAll("");
        // Headings become their own sentence-terminated paragraph
        text = HEADING.matcher(text).replaceAll("\n$1.\n");
        text = LIST_MARKER.matcher(text).replaceAll("");
        text = text.replace('|', ' ');
        text = EMPHASIS.matcher(text).replaceAll("");
        text = URL.matcher(text).replaceAll("");
        text = text.replaceAll("(?m)^[ \\t]+|[ \\t]+$", "");
        text = text.replaceAll("\\.{2,}", ".");
        text = text.replaceAll("[ \\t]{2,}", " ");
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }
}
