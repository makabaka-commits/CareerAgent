package dev.careeragent.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class Models {
    private Models() {}

    public record User(long id, String username, String email, String passwordHash, Instant createdAt) {}
    public record Profile(long userId, String school, String major, String grade, String targetPosition,
                          String targetCity, String selfDescription) {}
    public record Skill(long id, String code, String name, String category, List<String> aliases) {}
    public record UserSkill(long id, long userId, long skillId, int level, String source, String evidence,
                            Long sourceRefId, boolean confirmed) {}
    public record Resume(long id, long userId, String fileName, String contentType, String fileKey, String rawText,
                         StructuredResume parsed, String status, boolean current, String errorMessage, Instant createdAt) {}
    public record StructuredResume(String name, String education, List<ResumeSkill> skills,
                                   List<String> projects, List<String> experiences) {}
    public record ResumeSkill(String name, int level, double confidence, String evidence) {}
    public record Job(long id, long userId, String companyName, String positionName, String rawContent,
                      List<JobRequirement> requirements, String status, Instant createdAt) {}
    public record JobRequirement(long skillId, String skillName, String importance, Integer requirementLevel,
                                 String originalText) {}
    public record SkillMatch(String skillName, String importance, int weight, double coefficient,
                             double earnedWeight, double evidenceScore, double proficiencyScore,
                             double relevanceScore, String status, String evidence, String originalText) {}
    public record MatchDimensions(double skillCoverage, double evidenceStrength,
                                  double proficiencyFit, double projectRelevance) {}
    public record SkillGapReport(long jobId, double score, double requiredRate, Double preferredRate,
                                 MatchDimensions dimensions, String confidence, String recommendation,
                                 List<SkillMatch> details, List<String> missingSkills, List<String> weakEvidenceSkills) {}
    public record Conversation(long id, long userId, String scene, String title, Long jobId, Instant createdAt) {}
    public record Message(long id, long conversationId, String role, String content, Map<String, Object> metadata, Instant createdAt) {}
    public record KnowledgeChunk(long id, String title, String source, String topic, String section, String content) {}
    public record RetrievedKnowledge(long chunkId, String title, String source, String section, String content, double score) {}
    public record Interview(long id, long userId, long jobId, String status, List<Long> questionIds,
                            Double totalScore, InterviewReport report, Instant startedAt, Instant finishedAt) {}
    public record InterviewQuestion(long id, long interviewId, int sequenceNo, String question, String skillName,
                                    String difficulty, List<String> referencePoints, String userAnswer,
                                    Double score, QuestionEvaluation evaluation, Long parentQuestionId) {}
    public record QuestionEvaluation(double score, List<String> coveredPoints, List<String> missingPoints,
                                     String feedback, boolean followUpNeeded) {}
    public record InterviewReport(double totalScore, Map<String, Double> skillScores, List<String> strengths,
                                  List<String> weaknesses, List<String> suggestions) {}
}
