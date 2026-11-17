package dev.ronaldnguyen.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Cached GitHub repository, so projects survive restarts and API rate limits. */
@Entity
@Table(name = "github_repo")
public class GithubRepo {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "html_url", nullable = false)
    private String htmlUrl;

    private String homepage;
    private String language;
    private int stars;
    private int forks;

    /** Comma separated topic list. */
    private String topics;

    private boolean fork;

    @Column(name = "pushed_at")
    private Instant pushedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(columnDefinition = "text")
    private String readme;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }
    public String getHomepage() { return homepage; }
    public void setHomepage(String homepage) { this.homepage = homepage; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public int getForks() { return forks; }
    public void setForks(int forks) { this.forks = forks; }
    public String getTopics() { return topics; }
    public void setTopics(String topics) { this.topics = topics; }
    public boolean isFork() { return fork; }
    public void setFork(boolean fork) { this.fork = fork; }
    public Instant getPushedAt() { return pushedAt; }
    public void setPushedAt(Instant pushedAt) { this.pushedAt = pushedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getReadme() { return readme; }
    public void setReadme(String readme) { this.readme = readme; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }
}
