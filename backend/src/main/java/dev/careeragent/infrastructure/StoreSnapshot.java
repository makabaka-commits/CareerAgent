package dev.careeragent.infrastructure;

import dev.careeragent.domain.Models.*;
import java.util.Map;

public record StoreSnapshot(
        long lastId,
        Map<Long, User> users,
        Map<Long, Profile> profiles,
        Map<Long, Skill> skills,
        Map<Long, UserSkill> userSkills,
        Map<Long, Resume> resumes,
        Map<Long, Job> jobs,
        Map<Long, Conversation> conversations,
        Map<Long, Message> messages,
        Map<Long, KnowledgeChunk> knowledge,
        Map<Long, Interview> interviews,
        Map<Long, InterviewQuestion> questions) {}
