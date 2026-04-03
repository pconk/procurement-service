package com.pconk.procurement.api.exception;

import com.pconk.procurement.api.dto.WebResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        int status = 500;
        String message = "An internal server error occurred";

        if (exception instanceof NotFoundException) {
            status = 404;
            message = exception.getMessage();
        } else if (exception instanceof RuntimeException) {
            message = exception.getMessage();
            status = 400; // Bad Request untuk logika bisnis yang gagal
        }

        LOG.error("API Error: " + message, exception);
        WebResponse<Object> errorBody = WebResponse.error(status, "error", message);
        return Response.status(status).entity(errorBody).build();
    }
}   