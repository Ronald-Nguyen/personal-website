package dev.ronaldnguyen.portfolio.dto;

/** A cited knowledge-base chunk; {@code index} matches the [n] marker in the answer. */
public record SourceDto(int index, String section, String title, String snippet, double score, String url) {
}
