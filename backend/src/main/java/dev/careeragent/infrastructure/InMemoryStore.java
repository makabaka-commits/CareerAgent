package dev.careeragent.infrastructure;

import dev.careeragent.domain.Models.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStore {
    private final AtomicLong ids = new AtomicLong(100);
    private StatePersistence persistence;
    public final Map<Long, User> users = new ConcurrentHashMap<>();
    public final Map<Long, Profile> profiles = new ConcurrentHashMap<>();
    public final Map<Long, Skill> skills = new ConcurrentHashMap<>();
    public final Map<Long, UserSkill> userSkills = new ConcurrentHashMap<>();
    public final Map<Long, Resume> resumes = new ConcurrentHashMap<>();
    public final Map<Long, Job> jobs = new ConcurrentHashMap<>();
    public final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();
    public final Map<Long, Message> messages = new ConcurrentHashMap<>();
    public final Map<Long, KnowledgeChunk> knowledge = new ConcurrentHashMap<>();
    public final Map<Long, Interview> interviews = new ConcurrentHashMap<>();
    public final Map<Long, InterviewQuestion> questions = new ConcurrentHashMap<>();

    public InMemoryStore() {}
    @Autowired public InMemoryStore(ObjectProvider<StatePersistence> persistence) { this.persistence = persistence.getIfAvailable(); }

    public long nextId() { return ids.incrementAndGet(); }

    @PostConstruct
    public void initialize() {
        if (persistence != null && persistence.load().map(this::restore).orElse(false)) return;
        seedSkills();
    }

    public void seedSkills() {
        if (!skills.isEmpty()) return;
        addSkill("JAVA", "Java", "LANGUAGE", "java17", "jdk");
        addSkill("SPRING_BOOT", "Spring Boot", "FRAMEWORK", "springboot", "spring boot", "spring");
        addSkill("MYSQL", "MySQL", "DATABASE", "mysql", "sql");
        addSkill("REDIS", "Redis", "MIDDLEWARE", "redis");
        addSkill("GIT", "Git", "OTHER", "git", "github");
        addSkill("DOCKER", "Docker", "SYSTEM", "docker", "container");
        addSkill("LINUX", "Linux", "SYSTEM", "linux");
        addSkill("VUE", "Vue 3", "FRAMEWORK", "vue", "vue3", "vue.js");
        addSkill("TYPESCRIPT", "TypeScript", "LANGUAGE", "typescript", "ts");
        addSkill("NETWORK", "计算机网络", "CS_FUNDAMENTAL", "计算机网络", "tcp", "http");
        addSkill("OS", "操作系统", "CS_FUNDAMENTAL", "操作系统", "os");
        addSkill("MQ", "消息队列", "MIDDLEWARE", "消息队列", "rabbitmq", "kafka", "rocketmq");
    }

    private void addSkill(String code, String name, String category, String... aliases) {
        long id = nextId();
        skills.put(id, new Skill(id, code, name, category, List.of(aliases)));
    }

    @Scheduled(fixedDelayString = "${app.persistence.flush-ms:1500}")
    public void flush() { if (persistence != null) persistence.save(snapshot()); }
    @PreDestroy public void shutdown() { flush(); }

    private StoreSnapshot snapshot() {
        return new StoreSnapshot(ids.get(), Map.copyOf(users), Map.copyOf(profiles), Map.copyOf(skills),
                Map.copyOf(userSkills), Map.copyOf(resumes), Map.copyOf(jobs), Map.copyOf(conversations),
                Map.copyOf(messages), Map.copyOf(knowledge), Map.copyOf(interviews), Map.copyOf(questions));
    }

    private boolean restore(StoreSnapshot state) {
        ids.set(Math.max(100, state.lastId()));
        users.putAll(state.users()); profiles.putAll(state.profiles()); skills.putAll(state.skills());
        userSkills.putAll(state.userSkills()); resumes.putAll(state.resumes()); jobs.putAll(state.jobs());
        conversations.putAll(state.conversations()); messages.putAll(state.messages()); knowledge.putAll(state.knowledge());
        interviews.putAll(state.interviews()); questions.putAll(state.questions());
        return true;
    }
}
