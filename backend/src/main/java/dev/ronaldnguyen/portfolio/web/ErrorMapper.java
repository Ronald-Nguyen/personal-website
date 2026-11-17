package dev.ronaldnguyen.portfolio.web;

import dev.ronaldnguyen.portfolio.dto.ErrorDto;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Every error leaves the API as JSON; internals are logged, never returned. */
@Provider
public class ErrorMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ErrorMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        int status;
        String message;
        if (exception instanceof WebApplicationException wae) {
            status = wae.getResponse().getStatus();
            message = status >= 500 && wae.getMessage() == null ? "Service unavailable" : wae.getMessage();
            if (status == 404) {
                message = "Not found";
            } else if (status == 400 || status == 415) {
                message = "Invalid request";
            }
        } else if (isBadInput(exception)) {
            status = 400;
            message = "Invalid request";
        } else {
            LOG.log(Level.SEVERE, "Unhandled error", exception);
            status = 500;
            message = "Something went wrong. Please try again later.";
        }
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorDto(status, message)).build();
    }

    /** Malformed request bodies surface as JSON-B / JSON-P / JAX-RS processing errors. */
    private static boolean isBadInput(Throwable exception) {
        for (Throwable t = exception; t != null; t = t.getCause()) {
            if (t instanceof jakarta.json.bind.JsonbException
                    || t instanceof jakarta.json.JsonException
                    || t instanceof jakarta.ws.rs.ProcessingException) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }
}
