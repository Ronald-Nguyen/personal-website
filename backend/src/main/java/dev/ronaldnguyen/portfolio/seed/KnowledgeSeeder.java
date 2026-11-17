package dev.ronaldnguyen.portfolio.seed;

import dev.ronaldnguyen.portfolio.github.GithubService;
import dev.ronaldnguyen.portfolio.rag.IndexService;
import dev.ronaldnguyen.portfolio.repository.ChunkRepository;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeds the knowledge base with the CV on startup (idempotent, by content
 * hash), builds the vector index and then kicks off the GitHub sync. Runs
 * in the background and retries until the database is reachable; the
 * readiness check reports DOWN until the first seed succeeded.
 */
@ApplicationScoped
public class KnowledgeSeeder {

    private static final Logger LOG = Logger.getLogger(KnowledgeSeeder.class.getName());
    private static final long RETRY_SECONDS = 5;

    @Inject
    ChunkRepository chunks;

    @Inject
    IndexService index;

    @Inject
    ProfileService profile;

    @Inject
    GithubService github;

    @Resource
    ManagedScheduledExecutorService scheduler;

    private volatile boolean ready;

    void onStart(@Observes @Initialized(ApplicationScoped.class) Object event) {
        scheduler.execute(this::seed);
    }

    private void seed() {
        try {
            boolean changed = chunks.sync(ProfileChunker.SOURCE_TYPE, ProfileChunker.chunks(profile.profile()));
            LOG.info(() -> "CV knowledge base seeded" + (changed ? " (updated)" : " (unchanged)"));
            index.reindex();
            ready = true;
            github.start();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Seeding failed, retrying in " + RETRY_SECONDS + "s: " + e.getMessage());
            scheduler.schedule(this::seed, RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    public boolean isReady() {
        return ready;
    }
}
