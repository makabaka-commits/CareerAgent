# CareerAgent — AI 求职智能体平台

CareerAgent 面向计算机专业学生与初级开发者，把“简历 → JD → 可解释匹配 → Agent 咨询 → 模拟面试”串成一个可演示闭环。匹配分由 Java 规则计算，模型只负责解释；技能结论始终保留来源和证据。

## 已实现

- HMAC Bearer Token 注册登录、BCrypt 密码散列和用户资源隔离；
- PDF/DOCX/TXT/Markdown 简历文本提取、结构化技能识别、人工确认后写入长期画像；
- JD 技能归一化与确定性匹配：`REQUIRED=3`、`PREFERRED=1`，逐项展示原文和证据；
- Spring AI 2.0 的五个 `@Tool`：画像、简历、岗位、技能差距、知识检索；
- SSE 事件流：`status/tool_call/content/citation/done/error`；
- 本地知识库检索与可回溯 chunk 引用；
- 多题模拟面试、一次追问、覆盖点评分和分技能报告；
- Vue 3 + TypeScript 的完整演示工作台和 OpenAPI 文档。

默认 `local-demo` 运行态使用进程内存储，不需要先安装数据库或配置模型，因此可从空环境直接演示。`deploy/` 和 `db/migration/` 提供 MySQL、Redis、pgvector 的生产拓扑与 11 张业务表；接入真实基础设施时应替换 `InMemoryStore` 适配器。未配置 `AI_API_KEY` 时 Agent 会显式使用确定性降级，不伪造模型输出。

## 本地启动

要求 Java 17+ 与 Node.js 20+。仓库内下载的 Maven 位于 `.tools/`，该目录不会提交。

```powershell
# 后端
& .\.tools\apache-maven-3.9.16\bin\mvn.cmd -pl backend spring-boot:run

# 新开终端，前端
& 'C:\Users\19171\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback\pnpm.cmd' --dir frontend install
& 'C:\Users\19171\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback\pnpm.cmd' --dir frontend dev
```

访问 `http://localhost:5173`；API 文档为 `http://localhost:8080/swagger-ui.html`，健康检查为 `/actuator/health`。

演示顺序：注册 → 完善画像 → 上传并确认 `tests/fixtures/resume-demo.md` → 创建示例 JD → 查看匹配 → Agent 提问 → 完成模拟面试。

## 启用模型

```powershell
$env:AI_PROVIDER='openai'
$env:AI_BASE_URL='https://your-openai-compatible-host'
$env:AI_API_KEY='your-key'
$env:AI_CHAT_MODEL='your-model'
```

密钥仅通过环境变量提供。外部简历/JD/知识文档均被视为数据，不能覆盖 Agent 系统约束。

## 验证

```powershell
& .\.tools\apache-maven-3.9.16\bin\mvn.cmd -B test
pnpm --dir frontend build
```

更详细的架构、API 和演示说明见 [`docs/`](docs/architecture.md)。

