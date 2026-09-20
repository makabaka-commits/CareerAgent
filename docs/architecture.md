# 系统架构

Stepwise 采用模块化单体：保持毕业项目可读性，同时用端口与适配器隔离文件、状态、缓存和模型，便于后续拆分。

```mermaid
flowchart LR
  U[Vue 3 工作台] -->|REST + SSE| B[Spring Boot API]
  B --> A[Stepwise 顾问]
  A --> T[5 个领域工具]
  T --> P[画像 / 简历 / 岗位]
  T --> S[确定性匹配引擎]
  T --> R[知识检索]
  B --> I[模拟面试状态机]
  B --> DB[(H2 / PostgreSQL)]
  B --> FS[(简历文件卷)]
  B --> C[(Redis 限流)]
```

## Agent 请求链路

```mermaid
sequenceDiagram
  participant UI as Web
  participant API as Agent API
  participant LLM as Chat Model
  participant Tool as Domain Tools
  UI->>API: message + conversationId
  API-->>UI: status
  API->>LLM: system policy + recent context
  LLM->>Tool: profile/job/gap/knowledge
  Tool-->>LLM: owned and cited facts
  LLM-->>API: grounded answer
  API-->>UI: content/citation/done
  Note over API: timeout or model failure -> deterministic fallback
```

## 关键设计决策

1. 模型不直接打分，避免同一输入得到漂移结果；Java 服务是唯一评分源。
2. 外部简历、JD 和知识文档均视为不可信数据；高风险提示注入文本不会进入检索结果。
3. 所有业务资源按 `userId + resourceId` 校验，工具调用通过线程作用域绑定当前用户。
4. H2 与 PostgreSQL 共用同一套关系表和 Flyway 迁移；进程内工作集采用事务性写回，Redis 负责跨实例限流。
5. Spring Security 负责无状态 API 安全上下文，访问令牌支持过期和主动注销，密码使用 BCrypt 保存。
6. 模型未配置或超时时明确降级，不伪造“AI 已分析”；固定评测集与 Micrometer 指标持续验证路由和延迟。

## 可继续演进

当前 RAG 是适合小型作品集的词法混合检索。数据规模增长后，可把 `KnowledgeService` 适配为 pgvector 向量召回 + BM25 重排；持久层也可以从事务性关系表写回进一步演进为逐聚合 Repository，而无需改动 Controller 或前端协议。
