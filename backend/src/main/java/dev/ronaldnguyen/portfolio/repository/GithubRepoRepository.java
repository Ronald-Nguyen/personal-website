package dev.ronaldnguyen.portfolio.repository;

import dev.ronaldnguyen.portfolio.entity.GithubRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class GithubRepoRepository {

    @PersistenceContext(unitName = "portfolio")
    EntityManager em;

    @Transactional
    public List<GithubRepo> findAllOrdered() {
        return em.createQuery("SELECT r FROM GithubRepo r ORDER BY r.sortOrder, r.name", GithubRepo.class)
                .getResultList();
    }

    /** Replaces the cached repositories with a freshly fetched list. */
    @Transactional
    public void replaceAll(List<GithubRepo> repos) {
        em.createQuery("DELETE FROM GithubRepo").executeUpdate();
        for (GithubRepo repo : repos) {
            em.persist(repo);
        }
    }
}
