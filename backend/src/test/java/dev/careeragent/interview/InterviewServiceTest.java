package dev.careeragent.interview;

import dev.careeragent.domain.Models.*;import dev.careeragent.infrastructure.InMemoryStore;import dev.careeragent.job.JobService;import dev.careeragent.skill.SkillService;
import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.assertThat;

class InterviewServiceTest {
 @Test void supportsQuestionEvaluationAndReport(){InMemoryStore store=new InMemoryStore();store.seedSkills();SkillService skills=new SkillService(store);JobService jobs=new JobService(store,skills);Job job=jobs.create(1,"Acme","Java 实习","要求熟练 Java，掌握 Spring Boot 与 MySQL，能够完成后端系统开发和测试工作。");InterviewService service=new InterviewService(store,jobs);Interview i=service.create(1,job.id());InterviewQuestion q=service.start(1,i.id());var result=service.answer(1,i.id(),q.id(),"我在项目中处理 Java 并发，设计异常处理并使用测试验证结果，接口延迟下降 20%。");assertThat(result.evaluation().score()).isGreaterThan(70);Interview done=service.finish(1,i.id());assertThat(done.status()).isEqualTo("COMPLETED");assertThat(done.report()).isNotNull();}
}
