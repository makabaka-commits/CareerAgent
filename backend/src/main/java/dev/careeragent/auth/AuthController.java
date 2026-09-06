package dev.careeragent.auth;

import dev.careeragent.common.*;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final InMemoryStore store;
    private final TokenService tokens;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    public AuthController(InMemoryStore store, TokenService tokens) { this.store = store; this.tokens = tokens; }

    public record RegisterRequest(@NotBlank String username, @Email @NotBlank String email, @Size(min=6,max=72) String password) {}
    public record LoginRequest(@NotBlank String account, @NotBlank String password) {}

    @PostMapping("/register")
    public ApiResponse<Map<String,Object>> register(@Valid @RequestBody RegisterRequest request) {
        boolean exists = store.users.values().stream().anyMatch(u -> u.username().equalsIgnoreCase(request.username()) || u.email().equalsIgnoreCase(request.email()));
        if (exists) throw new ApiException(HttpStatus.CONFLICT, "用户名或邮箱已存在");
        long id = store.nextId();
        store.users.put(id, new User(id, request.username(), request.email(), passwords.encode(request.password()), Instant.now()));
        store.profiles.put(id, new Profile(id, "", "计算机科学与技术", "应届生", "Java 后端实习", "", ""));
        return ApiResponse.ok(Map.of("token", tokens.create(id), "user", Map.of("id", id, "username", request.username(), "email", request.email())));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String,Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = store.users.values().stream().filter(u -> u.username().equalsIgnoreCase(request.account()) || u.email().equalsIgnoreCase(request.account())).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "账号或密码错误"));
        if (!passwords.matches(request.password(), user.passwordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        return ApiResponse.ok(Map.of("token", tokens.create(user.id()), "user", Map.of("id", user.id(), "username", user.username(), "email", user.email())));
    }
}
