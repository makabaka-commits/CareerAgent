package dev.careeragent.job;

import dev.careeragent.common.ApiException;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import dev.careeragent.skill.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class JobService {
    private final InMemoryStore store; private final SkillService skills;
    public JobService(InMemoryStore store, SkillService skills) { this.store=store; this.skills=skills; }

    public Job create(long userId, String company, String position, String content) {
        if (content == null || content.trim().length() < 30) throw new ApiException(HttpStatus.BAD_REQUEST, "JD 内容至少需要 30 个字符");
        List<JobRequirement> requirements = new ArrayList<>();
        for (Skill skill : skills.detect(content)) {
            String evidence = Arrays.stream(content.split("[\\n。；;]"))
                    .map(String::trim).filter(line -> containsSkill(line, skill)).findFirst()
                    .orElse("JD 提及 " + skill.name());
            String normalized = evidence.toLowerCase(Locale.ROOT);
            boolean preferred = containsAny(normalized, "优先", "加分", "了解", "熟悉", "preferred", "nice to have");
            String importance = preferred ? "PREFERRED" : "REQUIRED";
            int level = containsAny(evidence, "精通", "深入", "expert") ? 4
                    : containsAny(evidence, "熟练", "掌握", "proficient") ? 3 : 2;
            requirements.add(new JobRequirement(skill.id(), skill.name(), importance, level, evidence));
        }
        if (requirements.isEmpty()) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "JD 中未识别到技能，请补充技术要求");
        long id=store.nextId(); Job job=new Job(id,userId,clean(company),clean(position),content.trim(),requirements,"PARSED", Instant.now());
        store.jobs.put(id,job); return job;
    }
    public Job owned(long userId,long id) { Job job=store.jobs.get(id); if(job==null||job.userId()!=userId) throw new ApiException(HttpStatus.NOT_FOUND,"岗位不存在"); return job; }
    private boolean containsSkill(String line, Skill skill) { String lower=line.toLowerCase(Locale.ROOT); return lower.contains(skill.name().toLowerCase(Locale.ROOT)) || skill.aliases().stream().anyMatch(a->lower.contains(a.toLowerCase(Locale.ROOT))); }
    private boolean containsAny(String value,String...terms){return Arrays.stream(terms).anyMatch(value::contains);}
    private String clean(String v){return v==null?"":v.trim();}
}
