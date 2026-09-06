package dev.careeragent.agent;

import dev.careeragent.common.ApiException;
import dev.careeragent.domain.Models.*;
import dev.careeragent.infrastructure.InMemoryStore;
import dev.careeragent.job.JobService;
import dev.careeragent.rag.KnowledgeService;
import dev.careeragent.resume.ResumeService;
import dev.careeragent.skill.SkillService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CareerAgentTools {
    private final InMemoryStore store; private final ResumeService resumes; private final JobService jobs; private final SkillService skills; private final KnowledgeService knowledge;
    private final ThreadLocal<Long> currentUser=new ThreadLocal<>();
    public CareerAgentTools(InMemoryStore store,ResumeService resumes,JobService jobs,SkillService skills,KnowledgeService knowledge){this.store=store;this.resumes=resumes;this.jobs=jobs;this.skills=skills;this.knowledge=knowledge;}
    public <T> T asUser(long userId,java.util.function.Supplier<T> action){currentUser.set(userId);try{return action.get();}finally{currentUser.remove();}}
    @Tool(name="getCareerProfile",description="获取当前用户经确认的长期职业画像") public Profile getCareerProfile(){return store.profiles.get(uid());}
    @Tool(name="getResume",description="获取属于当前用户的已解析简历") public StructuredResume getResume(long resumeId){Resume r=resumes.owned(uid(),resumeId);if(r.parsed()==null)throw new ApiException(HttpStatus.CONFLICT,"简历尚未解析");return r.parsed();}
    @Tool(name="analyzeJob",description="获取属于当前用户的结构化岗位要求") public Job analyzeJob(long jobId){return jobs.owned(uid(),jobId);}
    @Tool(name="calculateSkillGap",description="使用确定性规则计算当前用户与岗位的技能差距") public SkillGapReport calculateSkillGap(long jobId){return skills.calculate(uid(),jobs.owned(uid(),jobId));}
    @Tool(name="searchKnowledge",description="检索求职知识库并返回可追溯引用") public List<RetrievedKnowledge> searchKnowledge(String query){return knowledge.search(query,null,5);}
    private long uid(){Long value=currentUser.get();if(value==null)throw new ApiException(HttpStatus.UNAUTHORIZED,"缺少工具调用用户上下文");return value;}
}
