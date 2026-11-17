package dev.ronaldnguyen.portfolio.web;

import dev.ronaldnguyen.portfolio.seed.ProfileService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** The structured CV data, for API consumers. */
@Path("/profile")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {

    @Inject
    ProfileService profile;

    @GET
    public JsonObject profile() {
        return profile.profile();
    }
}
