# 架构说明

CareerAgent 是 Spring Boot + Vue 3 的模块化单体。Controller 只处理 HTTP 与 DTO；领域服务负责简历、岗位、匹配、检索和面试规则；`CareerAgentTools` 是五个薄工具适配器。

```text
Vue 工作台 ── REST / SSE ── Spring Boot
                              ├─ Auth / Profile / Resume
                              ├─ Job / SkillGap (纯 Java 计算)
                              ├─ CareerAgent + 5 Tools
                              ├─ Knowledge Retrieval
                              └─ Interview State Machine
                                         │
                 local-demo: InMemoryStore（零配置演示）
                 production: MySQL + Redis + pgvector（deploy/）
```

关键边界：模型不计算匹配分；长期技能只在用户确认后写入；所有资源通过 `userId + resourceId` 校验；检索为空时明确不提供引用；GitHub MCP 是 M6 扩展，不是当前完成项。

## Agent 请求链路

1. 鉴权过滤器解析 Bearer Token，并把 userId 放入请求作用域。
2. 会话绑定可选 jobId；完整消息进入事实存储。
3. Spring AI 可用时，ChatClient 通过五个 `@Tool` 按需读取最小上下文。
4. 模型未配置时走确定性降级，同样调用匹配和检索领域服务。
5. 前端消费 SSE 公开状态、工具名、内容和引用；不展示私有推理。

