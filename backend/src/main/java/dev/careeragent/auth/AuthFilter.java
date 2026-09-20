package dev.careeragent.auth;

import dev.careeragent.common.ApiException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthFilter extends OncePerRequestFilter {
    public static final String USER_ID = "currentUserId";
    private final TokenService tokens;
    public AuthFilter(TokenService tokens) { this.tokens = tokens; }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/") || path.startsWith("/api/v1/auth/");
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "请先登录");
            long userId=tokens.verify(header.substring(7));
            request.setAttribute(USER_ID,userId);
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    userId,null,java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"))));
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
