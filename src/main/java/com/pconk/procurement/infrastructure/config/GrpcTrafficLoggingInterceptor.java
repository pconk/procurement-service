package com.pconk.procurement.infrastructure.config;

import io.grpc.*;
import io.quarkus.grpc.GlobalInterceptor;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
@GlobalInterceptor
public class GrpcTrafficLoggingInterceptor implements ServerInterceptor {

    private static final Logger LOG = Logger.getLogger("grpc.traffic");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        long startTime = System.currentTimeMillis();
        String fullMethod = call.getMethodDescriptor().getFullMethodName();

        // Hapus try-catch di sini karena tidak akan menangkap error dari @Blocking service.
        // Error akan ditangkap di method close() pada LoggingServerCall di bawah.
        return next.startCall(new LoggingServerCall<>(call, startTime, fullMethod), headers);
    }

    private void logFinalTraffic(String method, Status status, long duration) {
        MDC.put("grpc.method", method);
        MDC.put("grpc.status", status.getCode().name());
        MDC.put("duration_ms", duration);

        if (status.isOk()) {
            LOG.infof("gRPC OK: %s (%dms)", method, duration);
        } else {
            LOG.errorf("gRPC %s: %s (%dms) - %s", status.getCode(), method, duration, status.getDescription());
        }
    }

    private void cleanupMdc() {
        MDC.remove("grpc.method");
        MDC.remove("grpc.status");
        MDC.remove("duration_ms");
        // Di sini kita hapus request_id karena ini adalah titik akhir request gRPC
        MDC.remove("request_id");
    }

    private class LoggingServerCall<ReqT, RespT> extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {
        private final long startTime;
        private final String fullMethod;

        LoggingServerCall(ServerCall<ReqT, RespT> delegate, long startTime, String fullMethod) {
            super(delegate);
            this.startTime = startTime;
            this.fullMethod = fullMethod;
        }

        // Kita membungkus ServerCall untuk menangkap event 'close' (saat request selesai)
        @Override
        public void close(Status status, Metadata trailers) {
            long duration = System.currentTimeMillis() - startTime;
            
            logFinalTraffic(fullMethod, status, duration);
            super.close(status, trailers);
            
            // Bersihkan semua termasuk request_id
            cleanupMdc();
        }
    }
}