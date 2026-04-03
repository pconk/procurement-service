package com.pconk.procurement.infrastructure.config;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.MDC;
import org.jboss.logging.Logger;

@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {

    private static final Logger LOG = Logger.getLogger(LoggingInterceptor.class);

    @AroundInvoke
    public Object logInvocation(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();
        String className = context.getMethod().getDeclaringClass().getSimpleName();
        
        long start = System.currentTimeMillis();

        try {
            Object result = context.proceed();
            long duration = System.currentTimeMillis() - start;
            
            // Menambahkan duration ke MDC agar muncul di field JSON tersendiri
            MDC.put("duration_ms", duration);
            LOG.infof("Executed %s.%s", className, methodName);
            return result;
        } finally {
            MDC.remove("duration_ms");
        }
    }
}