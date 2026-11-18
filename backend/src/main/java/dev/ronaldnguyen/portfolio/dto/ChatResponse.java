package dev.ronaldnguyen.portfolio.dto;

import java.util.List;

/**
 * Answer of POST /api/chat; {@code found} is false when nothing relevant matched,
 * {@code generated} is true when the LLM phrased the answer from the sources.
 */
public record ChatResponse(String answer, List<SourceDto> sources, boolean found, boolean generated) {
}
