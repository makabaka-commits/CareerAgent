package dev.careeragent.skill;

import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkillService {
    private final InMemoryStore store;
    public SkillService(InMemoryStore store) { this.store = store; }

    public Optional<Skill> normalize(String raw) {
        String value = compact(raw);
        return store.skills.values().stream().filter(skill -> compact(skill.name()).equals(value)
                || compact(skill.code()).equals(value)
                || skill.aliases().stream().map(this::compact).anyMatch(value::equals)).findFirst();
    }

    public List<Skill> detect(String text) {
        String haystack = text.toLowerCase(Locale.ROOT);
        return store.skills.values().stream().filter(skill -> {
            List<String> terms = new ArrayList<>(skill.aliases()); terms.add(skill.name());
            return terms.stream().anyMatch(term -> haystack.contains(term.toLowerCase(Locale.ROOT)));
        }).sorted(Comparator.comparing(Skill::name)).toList();
    }

    public SkillGapReport calculate(long userId, Job job) {
        Map<Long, UserSkill> strongest = new HashMap<>();
        store.userSkills.values().stream().filter(s -> s.userId() == userId).forEach(s ->
                strongest.merge(s.skillId(), s, (a,b) -> coefficient(a) >= coefficient(b) ? a : b));
        List<SkillMatch> details = new ArrayList<>();
        double total = 0, earned = 0, reqTotal = 0, reqEarned = 0, prefTotal = 0, prefEarned = 0;
        for (JobRequirement requirement : job.requirements()) {
            int weight = "REQUIRED".equals(requirement.importance()) ? 3 : 1;
            UserSkill own = strongest.get(requirement.skillId());
            double coefficient = own == null ? 0 : coefficient(own);
            if (own != null && requirement.requirementLevel() != null && own.level() < requirement.requirementLevel() && coefficient > .5) coefficient = .5;
            String status = coefficient == 0 ? "MISSING" : coefficient < 1 ? "WEAK" : "MATCHED";
            double itemEarned = weight * coefficient;
            details.add(new SkillMatch(requirement.skillName(), requirement.importance(), weight, coefficient, itemEarned,
                    status, own == null ? "未发现用户证据" : own.evidence(), requirement.originalText()));
            total += weight; earned += itemEarned;
            if (weight == 3) { reqTotal += weight; reqEarned += itemEarned; } else { prefTotal += weight; prefEarned += itemEarned; }
        }
        double score = total == 0 ? 0 : earned / total * 100;
        double required = reqTotal == 0 ? 100 : reqEarned / reqTotal * 100;
        double preferred = prefTotal == 0 ? 100 : prefEarned / prefTotal * 100;
        return new SkillGapReport(job.id(), round(score), round(required), round(preferred), details,
                details.stream().filter(d -> d.coefficient() == 0).map(SkillMatch::skillName).toList(),
                details.stream().filter(d -> d.coefficient() > 0 && d.coefficient() < 1).map(SkillMatch::skillName).toList());
    }

    private double coefficient(UserSkill skill) {
        if (skill.confirmed() && skill.evidence() != null && skill.evidence().trim().length() >= 8) return 1;
        if (skill.evidence() != null && skill.evidence().trim().length() >= 8) return .5;
        return .25;
    }
    private String compact(String value) { return value.toLowerCase(Locale.ROOT).replaceAll("[\\s._+\\-]", ""); }
    private double round(double value) { return Math.round(value * 10) / 10.0; }
}

