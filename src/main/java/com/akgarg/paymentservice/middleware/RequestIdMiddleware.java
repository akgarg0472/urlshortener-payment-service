package com.akgarg.paymentservice.middleware;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.akgarg.paymentservice.utils.PaymentServiceUtils.REQUEST_ID_HEADER;
import static com.akgarg.paymentservice.utils.PaymentServiceUtils.REQUEST_ID_THREAD_CONTEXT_KEY;


@Component
public class RequestIdMiddleware extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response,
            @Nonnull final FilterChain filterChain) throws ServletException, IOException {
        final String requestId;

        if (request.getHeader(REQUEST_ID_HEADER) == null) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        } else {
            requestId = request.getHeader(REQUEST_ID_HEADER);
        }

        ThreadContext.put(REQUEST_ID_THREAD_CONTEXT_KEY, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            ThreadContext.remove(REQUEST_ID_THREAD_CONTEXT_KEY);
        }
    }

}