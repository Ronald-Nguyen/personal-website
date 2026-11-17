package dev.ronaldnguyen.portfolio.dto;

import java.util.List;

public record ProjectDto(long id, String name, String description, String url, String homepage,
                         String language, int stars, int forks, List<String> topics, boolean fork,
                         String pushedAt) {
}
