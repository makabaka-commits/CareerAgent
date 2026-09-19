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
        double total = 0, earned = 0, reqTotal = 0, reqCovered = 0, prefTotal = 0, prefCovered = 0;
        double coverageTotal = 0, evidenceTotal = 0, proficiencyTotal = 0, relevanceTotal = 0;
        for (JobRequirement requirement : job.requirements()) {
            int weight = "REQUIRED".equals(requirement.importance()) ? 3 : 1;
            UserSkill own = strongest.get(requirement.skillId());
            double coverage = own == null ? 0 : 1;
            double evidence = own == null ? 0 : evidenceQuality(own);
            double proficiency = own == null ? 0 : Math.min(1, own.level() / (double)Math.max(1, Optional.ofNullable(requirement.requirementLevel()).orElse(2)));
            double relevance = own == null ? 0 : projectRelevance(own, requirement.skillName());
            double coefficient = .40 * coverage + .30 * evidence + .20 * proficiency + .10 * relevance;
            if (own != null && !own.confirmed()) coefficient *= .70;
            String status = coefficient == 0 ? "MISSING" : coefficient < .72 ? "WEAK" : "MATCHED";
            double itemEarned = weight * coefficient;
            details.add(new SkillMatch(requirement.skillName(), requirement.importance(), weight, round(coefficient), round(itemEarned),
                    round(evidence), round(proficiency), round(relevance), status,
                    own == null ? "未发现用户证据" : own.evidence(), requirement.originalText()));
            total += weight; earned += itemEarned;
            coverageTotal += weight * coverage; evidenceTotal += weight * evidence;
            proficiencyTotal += weight * proficiency; relevanceTotal += weight * relevance;
            if (weight == 3) { reqTotal += weight; reqCovered += weight * coverage; }
            else { prefTotal += weight; prefCovered += weight * coverage; }
        }
        double score = total == 0 ? 0 : earned / total * 100;
        double required = reqTotal == 0 ? 0 : reqCovered / reqTotal * 100;
        Double preferred = prefTotal == 0 ? null : round(prefCovered / prefTotal * 100);
        MatchDimensions dimensions = new MatchDimensions(
                total == 0 ? 0 : round(coverageTotal / total * 100),
                total == 0 ? 0 : round(evidenceTotal / total * 100),
                total == 0 ? 0 : round(proficiencyTotal / total * 100),
                total == 0 ? 0 : round(relevanceTotal / total * 100));
        String confidence = job.requirements().size() >= 5 && dimensions.evidenceStrength() >= 65 ? "HIGH"
                : job.requirements().size() >= 3 ? "MEDIUM" : "LOW";
        String recommendation = score >= 85 ? "高度匹配，可以投递；重点准备证据追问。"
                : score >= 70 ? "值得投递；先补强弱证据与关键技能差距。"
                : score >= 50 ? "可以尝试；建议完成针对性项目后再重点投递。"
                : "暂不建议盲投；优先补齐必需技能与可验证项目证据。";
        return new SkillGapReport(job.id(), round(score), round(required), preferred, dimensions, confidence, recommendation, details,
                details.stream().filter(d -> d.coefficient() == 0).map(SkillMatch::skillName).toList(),
                details.stream().filter(d -> d.coefficient() > 0 && d.coefficient() < .72).map(SkillMatch::skillName).toList());
    }

    private double evidenceQuality(UserSkill skill) {
        String evidence = Optional.ofNullable(skill.evidence()).orElse("").trim();
        if (evidence.isEmpty()) return .15;
        String lower = evidence.toLowerCase(Locale.ROOT);
        boolean quantified = lower.matches(".*(\\d+%|\\d+\\s*(ms|qps|万|次|人)|提升|降低|减少|测试通过|上线).*?");
        boolean action = containsAny(lower, "实现", "开发", "设计", "优化", "解决", "负责", "通过", "built", "implemented", "optimized");
        double score = quantified ? 1.0 : action ? .55 : evidence.length() >= 16 ? .35 : .20;
        return skill.confirmed() ? score : Math.min(.35, score);
    }

    private double projectRelevance(UserSkill skill, String skillName) {
        String evidence = Optional.ofNullable(skill.evidence()).orElse("").toLowerCase(Locale.ROOT);
        if (evidence.isBlank()) return 0;
        boolean namesSkill = evidence.contains(skillName.toLowerCase(Locale.ROOT));
        boolean project = containsAny(evidence, "项目", "系统", "平台", "接口", "服务", "缓存", "数据库", "project", "service", "api");
        return namesSkill && project ? .8 : project ? .6 : .35;
    }
    private double coefficient(UserSkill skill) { return skill.level() + (skill.confirmed() ? .5 : 0); }
    private boolean containsAny(String value, String... terms) { return Arrays.stream(terms).anyMatch(value::contains); }
    private String compact(String value) { return value.toLowerCase(Locale.ROOT).replaceAll("[\\s._+\\-]", ""); }
    private double round(double value) { return Math.round(value * 10) / 10.0; }
}
