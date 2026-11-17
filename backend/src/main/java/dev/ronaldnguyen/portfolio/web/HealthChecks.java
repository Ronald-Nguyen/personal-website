package dev.ronaldnguyen.portfolio.web;

import dev.ronaldnguyen.portfolio.seed.KnowledgeSeeder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

/** MicroProfile Health: /health/live and /health/ready (used by Docker). */
@ApplicationScoped
public class HealthChecks {

    @Inject
    KnowledgeSeeder seeder;

    @Produces
    @Liveness
    HealthCheck alive() {
        return () -> HealthCheckResponse.up("portfolio-backend");
    }

    @Produces
    @Readiness
    HealthCheck knowledgeBase() {
        return () -> HealthCheckResponse.named("knowledge-base").status(seeder.isReady()).build();
    }
}
