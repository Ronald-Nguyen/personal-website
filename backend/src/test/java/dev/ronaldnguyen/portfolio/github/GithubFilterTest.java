package dev.ronaldnguyen.portfolio.github;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.util.List;
import org.junit.jupiter.api.Test;

class GithubFilterTest {

    private static JsonObject repo(String name) {
        return Json.createObjectBuilder().add("name", name).add("fork", false).build();
    }

    @Test
    void excludesProfileRepoCaseInsensitively() {
        List<String> excluded = List.of("Ronald-Nguyen");
        assertTrue(GithubService.isExcluded(repo("Ronald-Nguyen"), excluded));
        assertTrue(GithubService.isExcluded(repo("ronald-nguyen"), excluded));
    }

    @Test
    void keepsOtherRepos() {
        List<String> excluded = List.of("Ronald-Nguyen");
        assertFalse(GithubService.isExcluded(repo("personal-website"), excluded));
        assertFalse(GithubService.isExcluded(repo("Ronald-Nguyen-old"), excluded));
        assertFalse(GithubService.isExcluded(repo("personal-website"), List.of()));
    }
}
