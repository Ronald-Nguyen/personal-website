package dev.ronaldnguyen.portfolio.dto;

import java.util.List;

/**
 * Answer of POST /api/chat; {@code found} is false when nothing relevant matched,
 * {@code generated} is true when the LLM wrote the answer, and {@code notice}
 * (may be null) explains to the visitor why it did not.
 */
public record ChatResponse(String answer, List<SourceDto> sources, boolean found, boolean generated,
                           String notice) {
}
