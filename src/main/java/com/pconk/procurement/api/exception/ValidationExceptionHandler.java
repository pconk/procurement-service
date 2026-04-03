package com.pconk.procurement.api.exception;

import com.pconk.procurement.api.dto.WebResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionHandler implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = Logger.getLogger(ValidationExceptionHandler.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Mengambil semua pesan validasi dan menggabungkannya dengan koma
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        LOG.errorf("Validation Error: %s", message);

        // Menyesuaikan dengan format WebResponse.error di GlobalExceptionHandler
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(WebResponse.error(400, "error", message))
                .build();
    }
}