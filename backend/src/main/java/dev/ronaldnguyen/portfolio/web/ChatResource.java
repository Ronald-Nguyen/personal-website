package dev.ronaldnguyen.portfolio.web;

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

    @Context
    HttpServletRequest servletRequest;

    @POST
    public Response ask(ChatRequest request) {
        String question = request == null || request.getQuestion() == null ? "" : request.getQuestion().strip();
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
        ChatResponse response = rag.ask(question);
        return Response.ok(response).build();
    }

    private String clientId() {
        String forwarded = servletRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorDto(status.getStatusCode(), message)).build();
    }
}
