package com.pconk.procurement.infrastructure.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import java.io.IOException;

@Provider
public class HttpLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger("http.access");
    private static final String START_TIME = "start-time";
    private static final String REQUEST_ID_PROP = "request-id";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME, System.currentTimeMillis());
        
        // Rule #6: Tangkap X-Request-ID dari Gateway
        String requestId = requestContext.getHeaderString("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = java.util.UUID.randomUUID().toString(); // Fallback jika gateway tidak kirim
        }
        
        // Masukkan ke MDC agar muncul di log JSON (seperti slog di Go)
        MDC.put("request_id", requestId);
        // Simpan di property agar bisa diambil saat phase Response
        requestContext.setProperty(REQUEST_ID_PROP, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Long startTime = (Long) requestContext.getProperty(START_TIME);
        String requestId = (String) requestContext.getProperty(REQUEST_ID_PROP);

        if (requestId != null) {
            responseContext.getHeaders().add("X-Request-ID", requestId);
        }

        long duration = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getRequestUri().getPath();
        int status = responseContext.getStatus();

        // Tambahkan atribut HTTP ke MDC
        MDC.put("http_method", method);
        MDC.put("http_path", path);
        MDC.put("http_status", status);
        MDC.put("duration_ms", duration);

        // Log dengan pesan yang jelas agar bisa dibedakan dengan log interceptor
        LOG.infof("HTTP Request: %s %s - Status: %d in %dms", method, path, status, duration);

        // Penting: Bersihkan MDC setelah request selesai agar tidak bocor ke thread lain
        MDC.remove("http_method");
        MDC.remove("http_path");
        MDC.remove("http_status");
        MDC.remove("duration_ms");
        // Note: request_id jangan di-remove di sini jika masih ada log asinkron, 
        // tapi untuk request REST biasa, ini saat yang tepat.
        MDC.remove("request_id");
    }
}