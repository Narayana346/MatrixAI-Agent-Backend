package com.workbuddy.matrixai.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

// Access-log style request/response logging for every route through the gateway.
// Deliberately does not buffer request/response bodies: several routes (e.g. the
// chat SSE stream) rely on the body being forwarded chunk-by-chunk, and buffering
// it to log would defeat streaming and blow up memory on large responses.
@Component
@Slf4j
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant start = Instant.now();
        String remoteAddr = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.info("--> {} {} from {}", request.getMethod(), request.getURI().getPath(), remoteAddr);

        return chain.filter(exchange)
                .doOnSuccess(v -> logResponse(exchange, start, null))
                .doOnError(err -> logResponse(exchange, start, err));
    }

    private void logResponse(ServerWebExchange exchange, Instant start, Throwable error) {
        ServerHttpRequest request = exchange.getRequest();
        long durationMs = Duration.between(start, Instant.now()).toMillis();

        if (error != null) {
            log.error("<-- {} {} failed after {}ms: {}", request.getMethod(), request.getURI().getPath(), durationMs, error.getMessage());
            return;
        }

        var status = exchange.getResponse().getStatusCode();
        log.info("<-- {} {} {} in {}ms", request.getMethod(), request.getURI().getPath(),
                status != null ? status.value() : "UNKNOWN", durationMs);
    }

    // Run before GatewayJwtAuthFilter (order -1) so unauthenticated/rejected requests get logged too.
    @Override
    public int getOrder() {
        return -2;
    }
}
