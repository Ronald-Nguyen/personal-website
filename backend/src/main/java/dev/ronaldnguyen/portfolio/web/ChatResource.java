package dev.ronaldnguyen.portfolio.web;

import dev.ronaldnguyen.portfolio.config.AppConfig;
import dev.ronaldnguyen.portfolio.dto.ChatRequest;
import dev.ronaldnguyen.portfolio.dto.ChatResponse;
import dev.ronaldnguyen.portfolio.dto.ErrorDto;
import dev.ronaldnguyen.portfolio.rag.RagService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/chat")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatResource {

    static final int MAX_QUESTION_LENGTH = 500;

    @Inject
    RagService rag;

    @Inject
    RateLimiter rateLimiter;

    @Inject
    AppConfig config;

    @Context
    HttpServletRequest servletRequest;

    @POST
    public Response ask(ChatRequest request) {
        String question = request == null || request.getQuestion() == null ? "" : request.getQuestion()
                .replaceAll("[\\p{Cc}&&[^\\n\\t]]", "").strip();
        if (question.isEmpty()) {
            return error(Response.Status.BAD_REQUEST, "Please enter a question.");
        }
        if (question.length() > MAX_QUESTION_LENGTH) {
            return error(Response.Status.BAD_REQUEST,
                    "Questions are limited to " + MAX_QUESTION_LENGTH + " characters.");
        }
        if (!rateLimiter.tryAcquire(clientId())) {
            return error(Response.Status.TOO_MANY_REQUESTS, "Too many questions — please wait a moment.");
        }
        ChatResponse response = rag.ask(question, clientId());
        return Response.ok(response).build();
    }

    /**
     * Nginx overwrites X-Forwarded-For with the real peer address, so the header
     * is only trusted when configured (the backend port must then not be
     * reachable from outside, see docker-compose.yml).
     */
    private String clientId() {
        if (config.trustForwardedFor()) {
            String forwarded = servletRequest.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String last = forwarded.substring(forwarded.lastIndexOf(',') + 1).trim();
                if (!last.isEmpty() && last.length() <= 64) {
                    return last;
                }
            }
        }
        return servletRequest.getRemoteAddr();
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorDto(status.getStatusCode(), message)).build();
    }
}
