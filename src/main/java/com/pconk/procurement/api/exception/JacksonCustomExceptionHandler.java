package com.pconk.procurement.api.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.pconk.procurement.api.dto.WebResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class JacksonCustomExceptionHandler implements ExceptionMapper<MismatchedInputException> {

    private static final Logger LOG = Logger.getLogger(JacksonCustomExceptionHandler.class);

    @Override
    public Response toResponse(MismatchedInputException exception) {
        String fieldName = exception.getPath().isEmpty() ? "unknown" : exception.getPath().get(0).getFieldName();
        String message = String.format("Format data salah pada field '%s'. Pastikan tipe data sesuai.", fieldName);

        if (exception instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) exception;
            message = String.format("Nilai '%s' tidak valid untuk field '%s'. Diharapkan tipe %s.", 
                ife.getValue(), fieldName, ife.getTargetType().getSimpleName());
        }

        LOG.errorf("Jackson Error: %s", exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(WebResponse.error(400, "error", message))
                .build();
    }
}