package com.pconk.procurement.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.*;
import org.jboss.logging.MDC;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.grpc.GlobalInterceptor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApplicationScoped
public class GrpcHeaderInterceptor {

    private static final Metadata.Key<String> REQUEST_ID_KEY = 
            Metadata.Key.of("X-Request-ID", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> AUTH_KEY = 
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    @ApplicationScoped
    @GlobalInterceptor
    public static class InboundInterceptor implements ServerInterceptor {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @ConfigProperty(name = "jwt.secret")
        String jwtSecret;

        @Inject
        UserContext userContext;

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            String requestId = headers.get(REQUEST_ID_KEY);
            // Jika X-Request-ID tidak ada di metadata, buat UUID baru (Fallback)
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }
            // Gunakan prefix trace. agar sesuai dengan format log Go (slog.Group)
            MDC.put("trace.request_id", requestId);

            // Ekstraksi JWT dari Metadata gRPC
            String authHeader = headers.get(AUTH_KEY);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    
                    // Validasi Signature
                    if (!isSignatureValid(token, jwtSecret)) {
                        call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT Signature"), new Metadata());
                        return new ServerCall.Listener<ReqT>() {};
                    }

                    String[] parts = token.split("\\.");
                    if (parts.length == 3) {
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                        JsonNode claims = objectMapper.readTree(payload);
                        
                        // Validasi Expiration (exp)
                        long currentTimeSeconds = System.currentTimeMillis() / 1000;
                        if (claims.has("exp") && claims.get("exp").asLong() < currentTimeSeconds) {
                            call.close(Status.UNAUTHENTICATED.withDescription("JWT Token has expired"), new Metadata());
                            return new ServerCall.Listener<ReqT>() {};
                        }

                        String userId = claims.has("user_id") ? claims.get("user_id").asText() : null;
                        String username = claims.has("username") ? claims.get("username").asText() : null;
                        String role = claims.has("role") ? claims.get("role").asText() : null;

                        if (userId != null) MDC.put("trace.user_id", userId);
                        if (username != null) MDC.put("trace.username", username);
                        if (role != null) MDC.put("trace.role", role);

                        userContext.setUserId(userId);
                        userContext.setUsername(username);
                        userContext.setToken(authHeader);
                        userContext.setRole(role);
                    }
                } catch (Exception e) {
                    // Silent failure untuk logging, jangan hentikan request
                }
            }

            return next.startCall(call, headers);
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
    }

    @ApplicationScoped
    @GlobalInterceptor
    public static class OutboundInterceptor implements ClientInterceptor {
        @Inject
        UserContext userContext;

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    // Ambil dari key trace.request_id
                    String requestId = (String) MDC.get("trace.request_id");
                    if (requestId != null) headers.put(REQUEST_ID_KEY, requestId);

                    // Teruskan JWT Token jika ada
                    if (userContext.getToken() != null) {
                        headers.put(AUTH_KEY, userContext.getToken());
                    }

                    super.start(responseListener, headers);
                }
            };
        }
    }
}