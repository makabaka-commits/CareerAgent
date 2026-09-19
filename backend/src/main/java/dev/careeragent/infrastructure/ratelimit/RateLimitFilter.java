package dev.careeragent.infrastructure.ratelimit;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {
    private final RequestRateLimiter limiter;
    public RateLimitFilter(RequestRateLimiter limiter) { this.limiter = limiter; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return !request.getRequestURI().startsWith("/api/"); }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        String key = token == null ? request.getRemoteAddr() : Integer.toHexString(token.hashCode());
        if (!limiter.allow(key)) {
            response.setStatus(429); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"success\":false,\"data\":null,\"message\":\"请求过于频繁，请稍后重试\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
