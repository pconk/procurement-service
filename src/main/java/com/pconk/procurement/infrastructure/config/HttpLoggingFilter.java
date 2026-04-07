package com.pconk.procurement.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pconk.procurement.api.dto.WebResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Provider
public class HttpLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger("http.access");
    private static final String START_TIME = "start-time";
    private static final String REQUEST_ID_PROP = "request-id";

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "jwt.secret")
    String jwtSecret;

    @Inject
    UserContext userContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME, System.currentTimeMillis());
        
        // Rule #6: Tangkap X-Request-ID dari Gateway
        String requestId = requestContext.getHeaderString("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = java.util.UUID.randomUUID().toString(); // Fallback jika gateway tidak kirim
        }
        
        // Masukkan ke MDC agar muncul di log JSON (seperti slog di Go)
        MDC.put("trace.request_id", requestId);
        // Simpan di property agar bisa diambil saat phase Response
        requestContext.setProperty(REQUEST_ID_PROP, requestId);

        // Ekstraksi JWT Claims (seperti di Golang)
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                
                // Validasi Signature (HmacSHA256)
                if (!isSignatureValid(token, jwtSecret)) {
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity(WebResponse.error(403, "error", "Invalid JWT Signature")).build());
                    return;
                }

                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                    JsonNode claims = objectMapper.readTree(payload);

                    // Validasi Expiration (exp)
                    long currentTimeSeconds = System.currentTimeMillis() / 1000;
                    if (claims.has("exp") && claims.get("exp").asLong() < currentTimeSeconds) {
                        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                                .entity(WebResponse.error(401, "error", "JWT Token has expired")).build());
                        return;
                    }

                    String userId = claims.has("user_id") ? claims.get("user_id").asText() : null;
                    String username = claims.has("username") ? claims.get("username").asText() : null;
                    String role = claims.has("role") ? claims.get("role").asText() : null;

                    if (userId != null) MDC.put("trace.user_id", userId);
                    if (username != null) MDC.put("trace.username", username);
                    if (role != null) MDC.put("trace.role", role);

                    // Simpan ke UserContext agar bisa di-inject di Service
                    userContext.setUserId(userId);
                    userContext.setUsername(username);
                    userContext.setRole(role);
                    userContext.setToken(authHeader);

                    // Simpan ke context request agar bisa dipakai di layer service jika butuh
                    requestContext.setProperty("user_id", userId);
                    requestContext.setProperty("username", username);
                }
            } catch (Exception e) {
                LOG.warn("Failed to parse JWT claims for logging", e);
            }
        }
    }

    private boolean isSignatureValid(String token, String secret) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String data = parts[0] + "." + parts[1];
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return calculatedSignature.equals(parts[2]);
        } catch (Exception e) {
            return false;
        }
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
        String ip = requestContext.getUriInfo().getBaseUri().getHost(); // Simpel IP info

        // Tambahkan atribut HTTP ke MDC
        MDC.put("http.method", method);
        MDC.put("http.path", path);
        MDC.put("http.status", status);
        MDC.put("http.duration_ms", duration);
        MDC.put("http.ip", ip);

        // Pesan log statis agar mirip dengan Go slog
        LOG.info("HTTP Request");

        // Penting: Bersihkan MDC setelah request selesai agar tidak bocor ke thread lain
        MDC.clear(); // Bersihkan semua MDC di akhir request
    }
}