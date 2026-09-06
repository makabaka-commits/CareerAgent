package dev.careeragent.profile;

import dev.careeragent.auth.AuthFilter;
import dev.careeragent.common.*;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import dev.careeragent.skill.SkillService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/profiles/me")
public class ProfileController {
    private final InMemoryStore store;
    private final SkillService skills;
    public ProfileController(InMemoryStore store, SkillService skills) { this.store = store; this.skills = skills; }

    public record ProfileRequest(@Size(max=100) String school, @Size(max=100) String major, @Size(max=30) String grade,
                                 @Size(max=100) String targetPosition, @Size(max=100) String targetCity,
                                 @Size(max=1000) String selfDescription) {}
    public record SkillRequest(@NotBlank String name, @Min(1) @Max(5) int level, @NotBlank @Size(max=500) String evidence) {}

    @GetMapping public ApiResponse<Profile> me(HttpServletRequest request) { return ApiResponse.ok(store.profiles.get(userId(request))); }
    @PutMapping public ApiResponse<Profile> update(HttpServletRequest req, @Valid @RequestBody ProfileRequest body) {
        long userId = userId(req);
        Profile profile = new Profile(userId, clean(body.school()), clean(body.major()), clean(body.grade()), clean(body.targetPosition()), clean(body.targetCity()), clean(body.selfDescription()));
        store.profiles.put(userId, profile); return ApiResponse.ok(profile);
    }
    @GetMapping("/skills") public ApiResponse<List<Map<String,Object>>> listSkills(HttpServletRequest request) {
        long userId = userId(request);
        return ApiResponse.ok(store.userSkills.values().stream().filter(it -> it.userId() == userId).map(it -> {
            Skill s = store.skills.get(it.skillId());
            Map<String,Object> result = new LinkedHashMap<>();
            result.put("id", it.id()); result.put("name", s.name()); result.put("category", s.category()); result.put("level", it.level());
            result.put("source", it.source()); result.put("evidence", it.evidence()); result.put("confirmed", it.confirmed()); return result;
        }).toList());
    }
    @PostMapping("/skills") public ApiResponse<UserSkill> addSkill(HttpServletRequest request, @Valid @RequestBody SkillRequest body) {
        long userId = userId(request);
        Skill skill = skills.normalize(body.name()).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "技能不在当前词典中"));
        long id = store.nextId();
        UserSkill value = new UserSkill(id, userId, skill.id(), body.level(), "USER_CONFIRMED", body.evidence(), null, true);
        store.userSkills.put(id, value); return ApiResponse.ok(value);
    }
    @DeleteMapping("/skills/{id}") public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable long id) {
        UserSkill value = store.userSkills.get(id);
        if (value == null || value.userId() != userId(request)) throw new ApiException(HttpStatus.NOT_FOUND, "技能证据不存在");
        store.userSkills.remove(id); return ApiResponse.ok();
    }
    private long userId(HttpServletRequest r) { return (long) r.getAttribute(AuthFilter.USER_ID); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}

