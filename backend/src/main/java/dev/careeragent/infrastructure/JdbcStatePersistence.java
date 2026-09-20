package dev.careeragent.infrastructure;

import dev.careeragent.domain.Models.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/** Relational durable storage for the application's in-memory working set. */
@Component
@ConditionalOnProperty(name = "app.persistence.mode", havingValue = "jdbc")
public class JdbcStatePersistence implements StatePersistence {
    private final JdbcClient jdbc; private final ObjectMapper json; private final TransactionTemplate transactions;
    public JdbcStatePersistence(JdbcClient jdbc,ObjectMapper json,TransactionTemplate transactions){this.jdbc=jdbc;this.json=json;this.transactions=transactions;}

    @Override public Optional<StoreSnapshot> load(){
        Long lastId=jdbc.sql("SELECT last_value FROM app_sequence WHERE name='global'").query(Long.class).optional().orElse(null);
        if(lastId==null)return legacySnapshot();
        try{
            Map<Long,User> users=map(jdbc.sql("SELECT * FROM user_account").query((r,n)->new User(r.getLong("id"),r.getString("username"),r.getString("email"),r.getString("password_hash"),instant(r,"created_at"))).list(),User::id);
            Map<Long,Profile> profiles=map(jdbc.sql("SELECT * FROM career_profile").query((r,n)->new Profile(r.getLong("user_id"),r.getString("school"),r.getString("major"),r.getString("grade"),r.getString("target_position"),r.getString("target_city"),r.getString("self_description"))).list(),Profile::userId);
            Map<Long,Skill> skills=map(jdbc.sql("SELECT * FROM skill").query((r,n)->new Skill(r.getLong("id"),r.getString("code"),r.getString("name"),r.getString("category"),read(r.getString("aliases_json"),new TypeReference<List<String>>(){}))).list(),Skill::id);
            Map<Long,UserSkill> userSkills=map(jdbc.sql("SELECT * FROM user_skill").query((r,n)->new UserSkill(r.getLong("id"),r.getLong("user_id"),r.getLong("skill_id"),r.getInt("level"),r.getString("source"),r.getString("evidence"),nullableLong(r,"source_ref_id"),r.getBoolean("confirmed"))).list(),UserSkill::id);
            Map<Long,Resume> resumes=map(jdbc.sql("SELECT * FROM resume").query((r,n)->new Resume(r.getLong("id"),r.getLong("user_id"),r.getString("file_name"),r.getString("content_type"),r.getString("file_key"),r.getString("raw_text"),readNullable(r.getString("parsed_json"),StructuredResume.class),r.getString("status"),r.getBoolean("is_current"),r.getString("error_message"),instant(r,"created_at"))).list(),Resume::id);
            Map<Long,Job> jobs=map(jdbc.sql("SELECT * FROM job_description").query((r,n)->new Job(r.getLong("id"),r.getLong("user_id"),r.getString("company_name"),r.getString("position_name"),r.getString("raw_content"),read(r.getString("requirements_json"),new TypeReference<List<JobRequirement>>(){}),r.getString("status"),instant(r,"created_at"))).list(),Job::id);
            Map<Long,Conversation> conversations=map(jdbc.sql("SELECT * FROM conversation").query((r,n)->new Conversation(r.getLong("id"),r.getLong("user_id"),r.getString("scene"),r.getString("title"),nullableLong(r,"job_id"),instant(r,"created_at"))).list(),Conversation::id);
            Map<Long,Message> messages=map(jdbc.sql("SELECT * FROM message").query((r,n)->new Message(r.getLong("id"),r.getLong("conversation_id"),r.getString("role"),r.getString("content"),read(r.getString("metadata_json"),new TypeReference<Map<String,Object>>(){}),instant(r,"created_at"))).list(),Message::id);
            Map<Long,KnowledgeChunk> knowledge=map(jdbc.sql("SELECT * FROM knowledge_chunk").query((r,n)->new KnowledgeChunk(r.getLong("id"),r.getString("title"),r.getString("source"),r.getString("topic"),r.getString("section"),r.getString("content"))).list(),KnowledgeChunk::id);
            Map<Long,Interview> interviews=map(jdbc.sql("SELECT * FROM interview").query((r,n)->new Interview(r.getLong("id"),r.getLong("user_id"),r.getLong("job_id"),r.getString("status"),read(r.getString("question_ids_json"),new TypeReference<List<Long>>(){}),(Double)r.getObject("total_score"),readNullable(r.getString("report_json"),InterviewReport.class),instantNullable(r,"started_at"),instantNullable(r,"finished_at"))).list(),Interview::id);
            Map<Long,InterviewQuestion> questions=map(jdbc.sql("SELECT * FROM interview_question").query((r,n)->new InterviewQuestion(r.getLong("id"),r.getLong("interview_id"),r.getInt("sequence_no"),r.getString("question"),r.getString("skill_name"),r.getString("difficulty"),read(r.getString("reference_points_json"),new TypeReference<List<String>>(){}),r.getString("user_answer"),(Double)r.getObject("score"),readNullable(r.getString("evaluation_json"),QuestionEvaluation.class),nullableLong(r,"parent_question_id"))).list(),InterviewQuestion::id);
            return Optional.of(new StoreSnapshot(lastId,users,profiles,skills,userSkills,resumes,jobs,conversations,messages,knowledge,interviews,questions));
        }catch(Exception e){throw new IllegalStateException("无法读取关系型业务数据",e);}
    }

    private Optional<StoreSnapshot> legacySnapshot(){
        try{return jdbc.sql("SELECT payload FROM career_state WHERE id=1").query(String.class).optional().map(value->{try{return json.readValue(value,StoreSnapshot.class);}catch(Exception e){throw new IllegalStateException("无法迁移旧版数据",e);}});}catch(Exception tableMissing){return Optional.empty();}
    }

    @Override public void save(StoreSnapshot s){transactions.executeWithoutResult(status->{try{
        for(String table:List.of("interview_question","interview","message","conversation","user_skill","resume","job_description","knowledge_chunk","career_profile","skill","user_account"))jdbc.sql("DELETE FROM "+table).update();
        for(User v:s.users().values())jdbc.sql("INSERT INTO user_account(id,username,email,password_hash,role,created_at) VALUES(:id,:u,:e,:p,'USER',:c)").param("id",v.id()).param("u",v.username()).param("e",v.email()).param("p",v.passwordHash()).param("c",Timestamp.from(v.createdAt())).update();
        for(Profile v:s.profiles().values())jdbc.sql("INSERT INTO career_profile VALUES(:id,:s,:m,:g,:p,:c,:d)").param("id",v.userId()).param("s",v.school()).param("m",v.major()).param("g",v.grade()).param("p",v.targetPosition()).param("c",v.targetCity()).param("d",v.selfDescription()).update();
        for(Skill v:s.skills().values())jdbc.sql("INSERT INTO skill VALUES(:id,:c,:n,:g,:a)").param("id",v.id()).param("c",v.code()).param("n",v.name()).param("g",v.category()).param("a",write(v.aliases())).update();
        for(UserSkill v:s.userSkills().values())jdbc.sql("INSERT INTO user_skill VALUES(:id,:u,:s,:l,:o,:e,:r,:c)").param("id",v.id()).param("u",v.userId()).param("s",v.skillId()).param("l",v.level()).param("o",v.source()).param("e",v.evidence()).param("r",v.sourceRefId()).param("c",v.confirmed()).update();
        for(Resume v:s.resumes().values())jdbc.sql("INSERT INTO resume VALUES(:id,:u,:n,:t,:k,:x,:p,:s,:c,:e,:a)").param("id",v.id()).param("u",v.userId()).param("n",v.fileName()).param("t",v.contentType()).param("k",v.fileKey()).param("x",v.rawText()).param("p",writeNullable(v.parsed())).param("s",v.status()).param("c",v.current()).param("e",v.errorMessage()).param("a",Timestamp.from(v.createdAt())).update();
        for(Job v:s.jobs().values())jdbc.sql("INSERT INTO job_description VALUES(:id,:u,:c,:p,:r,:j,:s,:a)").param("id",v.id()).param("u",v.userId()).param("c",v.companyName()).param("p",v.positionName()).param("r",v.rawContent()).param("j",write(v.requirements())).param("s",v.status()).param("a",Timestamp.from(v.createdAt())).update();
        for(Conversation v:s.conversations().values())jdbc.sql("INSERT INTO conversation VALUES(:id,:u,:s,:t,:j,:c)").param("id",v.id()).param("u",v.userId()).param("s",v.scene()).param("t",v.title()).param("j",v.jobId()).param("c",Timestamp.from(v.createdAt())).update();
        for(Message v:s.messages().values())jdbc.sql("INSERT INTO message VALUES(:id,:c,:r,:x,:m,:a)").param("id",v.id()).param("c",v.conversationId()).param("r",v.role()).param("x",v.content()).param("m",write(v.metadata())).param("a",Timestamp.from(v.createdAt())).update();
        for(KnowledgeChunk v:s.knowledge().values())jdbc.sql("INSERT INTO knowledge_chunk VALUES(:id,:t,:s,:o,:e,:c)").param("id",v.id()).param("t",v.title()).param("s",v.source()).param("o",v.topic()).param("e",v.section()).param("c",v.content()).update();
        for(Interview v:s.interviews().values())jdbc.sql("INSERT INTO interview VALUES(:id,:u,:j,:s,:q,:t,:r,:a,:f)").param("id",v.id()).param("u",v.userId()).param("j",v.jobId()).param("s",v.status()).param("q",write(v.questionIds())).param("t",v.totalScore()).param("r",writeNullable(v.report())).param("a",timestamp(v.startedAt())).param("f",timestamp(v.finishedAt())).update();
        for(InterviewQuestion v:s.questions().values())jdbc.sql("INSERT INTO interview_question VALUES(:id,:i,:n,:q,:s,:d,:r,:a,:c,:e,:p)").param("id",v.id()).param("i",v.interviewId()).param("n",v.sequenceNo()).param("q",v.question()).param("s",v.skillName()).param("d",v.difficulty()).param("r",write(v.referencePoints())).param("a",v.userAnswer()).param("c",v.score()).param("e",writeNullable(v.evaluation())).param("p",v.parentQuestionId()).update();
        int changed=jdbc.sql("UPDATE app_sequence SET last_value=:v WHERE name='global'").param("v",s.lastId()).update();if(changed==0)jdbc.sql("INSERT INTO app_sequence(name,last_value) VALUES('global',:v)").param("v",s.lastId()).update();
    }catch(Exception e){status.setRollbackOnly();throw new IllegalStateException("无法保存关系型业务数据",e);}});}

    private String write(Object v){try{return json.writeValueAsString(v);}catch(Exception e){throw new IllegalStateException(e);}}
    private String writeNullable(Object v){return v==null?null:write(v);}
    private <T>T read(String v,TypeReference<T> t){try{return json.readValue(v,t);}catch(Exception e){throw new IllegalStateException(e);}}
    private <T>T readNullable(String v,Class<T> t){if(v==null)return null;try{return json.readValue(v,t);}catch(Exception e){throw new IllegalStateException(e);}}
    private static Instant instant(java.sql.ResultSet r,String c)throws java.sql.SQLException{return r.getTimestamp(c).toInstant();}
    private static Instant instantNullable(java.sql.ResultSet r,String c)throws java.sql.SQLException{Timestamp t=r.getTimestamp(c);return t==null?null:t.toInstant();}
    private static Long nullableLong(java.sql.ResultSet r,String c)throws java.sql.SQLException{long v=r.getLong(c);return r.wasNull()?null:v;}
    private static Timestamp timestamp(Instant v){return v==null?null:Timestamp.from(v);}
    private static <T>Map<Long,T> map(List<T> values,java.util.function.ToLongFunction<T> id){Map<Long,T> result=new HashMap<>();values.forEach(v->result.put(id.applyAsLong(v),v));return result;}
}
