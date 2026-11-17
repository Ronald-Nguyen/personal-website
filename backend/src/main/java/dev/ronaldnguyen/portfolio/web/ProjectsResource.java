package dev.ronaldnguyen.portfolio.web;

import dev.ronaldnguyen.portfolio.dto.ErrorDto;
import dev.ronaldnguyen.portfolio.dto.ProjectDto;
import dev.ronaldnguyen.portfolio.github.GithubService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/projects")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class ProjectsResource {

    @Inject
    GithubService github;

    @GET
    public Response projects() {
        List<ProjectDto> projects = github.projects();
        if (projects.isEmpty() && github.lastSuccess() == null && github.lastError() != null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new ErrorDto(503, "GitHub is currently unreachable. Please try again later."))
                    .build();
        }
        CacheControl cache = new CacheControl();
        cache.setMaxAge(300);
        return Response.ok(projects).cacheControl(cache).build();
    }
}
