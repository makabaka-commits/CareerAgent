package dev.careeragent.auth;

import dev.careeragent.common.*;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import dev.careeragent.job.JobService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final InMemoryStore store;
    private final TokenService tokens;
    private final JobService jobs;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    public AuthController(InMemoryStore store, TokenService tokens, JobService jobs) { this.store = store; this.tokens = tokens; this.jobs = jobs; }

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

    @PostMapping("/demo")
    public ApiResponse<Map<String,Object>> demo() {
        long id = store.nextId();
        String username = "guest_" + id;
        store.users.put(id, new User(id, username, username + "@careeragent.local", passwords.encode(UUID.randomUUID().toString()), Instant.now()));
        store.profiles.put(id, new Profile(id, "示例大学", "计算机科学与技术", "应届生", "Java 后端实习", "上海",
                "关注可解释 AI Agent、Java 后端工程和可验证项目证据。"));
        Map<String,Integer> levels = Map.of("Java",4,"Spring Boot",3,"MySQL",3,"Redis",2,"Docker",2,"Git",3,"Linux",2,"计算机网络",3);
        levels.forEach((name, level) -> store.skills.values().stream().filter(s -> s.name().equals(name)).findFirst().ifPresent(skill -> {
            long skillId = store.nextId();
            String evidence = switch (name) {
                case "Java" -> "在 CareerAgent 项目中使用 Java 17 实现 REST API，并通过单元测试验证核心评分逻辑。";
                case "Spring Boot" -> "使用 Spring Boot 构建鉴权、简历、岗位与面试模块，完成统一异常处理和健康检查。";
                case "MySQL" -> "设计用户、简历、岗位、会话与面试等业务表，但尚缺少线上查询性能数据。";
                case "Redis" -> "了解 Redis TTL、穿透与一致性方案，当前项目仅完成可切换限流适配。";
                default -> "在 CareerAgent 项目开发和部署过程中使用 " + name + "，具备可演示的基础实践。";
            };
            store.userSkills.put(skillId, new UserSkill(skillId, id, skill.id(), level, "DEMO", evidence, null, true));
        }));
        Job job = jobs.create(id, "未来科技", "Java 后端实习生",
                "熟练掌握 Java 和 Spring Boot，掌握 MySQL；了解 Redis、Linux、Docker 和消息队列。具备 Git 协作经验者优先。");
        return ApiResponse.ok(Map.of("token", tokens.create(id), "jobId", job.id(),
                "user", Map.of("id", id, "username", username, "email", username + "@careeragent.local")));
    }
}
