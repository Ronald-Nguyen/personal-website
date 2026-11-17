package dev.ronaldnguyen.portfolio.rag;

/** A knowledge chunk before it is persisted. */
public record ChunkDraft(String sourceType, String sourceKey, String section, String title,
                         String url, int chunkIndex, String content) {

    public String key() {
        return sourceKey + "#" + chunkIndex;
    }
}
