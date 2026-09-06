package dev.careeragent.skill;

import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import org.junit.jupiter.api.*;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class SkillServiceTest {
    private InMemoryStore store; private SkillService service; private Skill java; private Skill redis;
    @BeforeEach void setUp(){store=new InMemoryStore();store.seedSkills();service=new SkillService(store);java=service.normalize("java").orElseThrow();redis=service.normalize("Redis").orElseThrow();}
    @Test void normalizesAliases(){assertThat(service.normalize("SpringBoot")).get().extracting(Skill::code).isEqualTo("SPRING_BOOT");}
    @Test void calculatesDeterministicWeightedScore(){
        long uid=1;store.userSkills.put(1L,new UserSkill(1,uid,java.id(),4,"RESUME","项目中使用 Java 17 开发 REST API",1L,true));
        Job job=new Job(2,uid,"Acme","Java 实习", "", List.of(
                new JobRequirement(java.id(),java.name(),"REQUIRED",3,"熟练 Java"),
                new JobRequirement(redis.id(),redis.name(),"PREFERRED",2,"了解 Redis")),"PARSED", Instant.now());
        SkillGapReport report=service.calculate(uid,job);
        assertThat(report.score()).isEqualTo(75.0); // (3*1 + 1*0) / 4
        assertThat(report.requiredRate()).isEqualTo(100.0);
        assertThat(report.missingSkills()).containsExactly("Redis");
    }
}
