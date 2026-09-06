package dev.careeragent.auth;

import dev.careeragent.common.ApiException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthFilter extends OncePerRequestFilter {
    public static final String USER_ID = "currentUserId";
    private final TokenService tokens;
    public AuthFilter(TokenService tokens) { this.tokens = tokens; }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/") || path.startsWith("/actuator/") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "请先登录");
            request.setAttribute(USER_ID, tokens.verify(header.substring(7)));
            chain.doFilter(request, response);
        } catch (ApiException e) {
            response.setStatus(e.status().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String message = e.getMessage().replace("\\", "\\\\").replace("\"", "\\\"");
            response.getWriter().write("{\"success\":false,\"data\":null,\"message\":\"" + message + "\"}");
        }
    }
}
