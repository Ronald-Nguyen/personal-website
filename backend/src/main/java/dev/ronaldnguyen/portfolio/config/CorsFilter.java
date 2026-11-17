package dev.ronaldnguyen.portfolio.config;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

/**
 * CORS for local development (Vite dev server on :5173) and any other
 * configured origin. In Docker the frontend is served same-origin through
 * the Nginx proxy, so CORS is not needed there but does no harm.
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ORIGIN = "Origin";

    private final List<String> allowedOrigins = AppConfig.corsAllowedOrigins();

    private boolean isAllowed(String origin) {
        return origin != null && (allowedOrigins.contains("*") || allowedOrigins.contains(origin));
    }

    @Override
    public void filter(ContainerRequestContext request) {
        if (HttpMethod.OPTIONS.equalsIgnoreCase(request.getMethod())
                && request.getHeaderString("Access-Control-Request-Method") != null) {
            Response.ResponseBuilder builder = Response.noContent();
            String origin = request.getHeaderString(ORIGIN);
            if (isAllowed(origin)) {
                builder.header("Access-Control-Allow-Origin", origin)
                        .header("Vary", ORIGIN)
                        .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept")
                        .header("Access-Control-Max-Age", "86400");
            }
            request.abortWith(builder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        String origin = request.getHeaderString(ORIGIN);
        if (!isAllowed(origin)) {
            return;
        }
        MultivaluedMap<String, Object> headers = response.getHeaders();
        headers.putSingle("Access-Control-Allow-Origin", origin);
        headers.putSingle("Vary", ORIGIN);
        if (!headers.containsKey("Access-Control-Allow-Methods")) {
            headers.putSingle("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.putSingle("Access-Control-Allow-Headers", "Content-Type, Accept");
        }
    }
}
