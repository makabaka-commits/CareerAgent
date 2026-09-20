# Stepwise

> 让每一步，都有依据。

**在线体验：** [https://stepwise-app.onrender.com](https://stepwise-app.onrender.com)

面向计算机专业学生的可解释 AI 求职智能体。它把简历解析、岗位分析、证据化匹配、Agent 咨询和模拟面试串成一个完整产品闭环；评分由确定性规则完成，模型只负责工具选择与解释。

## 为什么值得放进作品集

- **正式产品界面**：Vue 3 + TypeScript 的落地页、体验入口与响应式工作台，不是单页表单 Demo。
- **可信匹配**：技能覆盖 40%、证据强度 30%、熟练度适配 20%、项目相关性 10%，逐项说明得分和原始 JD。
- **Agent 工程**：Spring AI 工具调用、SSE 状态流、短期会话上下文、可追溯 RAG 引用、超时自动降级。
- **生产能力**：H2 零配置本地运行，PostgreSQL 关系表 + Flyway 迁移、Redis 分布式限流、Spring Security、Actuator/Prometheus 指标。
- **可部署**：Docker Compose 一键启动前端、后端、PostgreSQL 与 Redis；GitHub Actions 自动测试、构建与镜像校验。

## 技术栈

`Java 17` · `Spring Boot 4` · `Spring AI 2` · `Vue 3` · `TypeScript` · `PostgreSQL/pgvector` · `Redis` · `H2` · `Docker` · `SSE`

## 5 分钟本地运行

需要 Java 17+、Node.js 20+。默认不需要数据库和模型密钥。项目已约定从 `D:\\DevTools\\Tesseract-OCR` 调用中英文 OCR；若本机未安装，可将 `OCR_ENABLED` 设为 `false`，普通文本简历仍可正常解析。

```powershell
# 终端 1：后端
& .\.tools\apache-maven-3.9.16\bin\mvn.cmd -pl backend spring-boot:run

# 终端 2：前端
pnpm --dir frontend install
pnpm --dir frontend dev
```

项目在本机成功启动后，访问 `http://localhost:5173`，点击“在线体验”即可生成一套非满分示例数据。API 文档位于 `http://localhost:8080/swagger-ui.html`。`localhost` 只代表当前电脑，并不是公网演示地址。

## 用 `http://localhost` 访问完整生产拓扑

安装 Docker Desktop 后：

```powershell
Copy-Item deploy/.env.example deploy/.env
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up --build
```

项目在本机成功启动后，打开 `http://localhost`。如需任何人都能访问的公网网址，需要把同一套容器部署到云服务器或 Railway/Render，再绑定域名并启用 HTTPS；详见 [部署说明](docs/deployment.md)。

## 匹配结果为什么不会轻易 100 分

岗位要求按 `REQUIRED=3`、`PREFERRED=1` 加权。即使简历提到某项技能，也还要检查项目证据、目标熟练度和场景相关性；未确认的技能再乘 0.7。只有每项技能都有充分且可验证的证据时，结果才可能接近 100。算法说明见 [评分设计](docs/scoring.md)。

## 验证

```powershell
& .\.tools\apache-maven-3.9.16\bin\mvn.cmd "-Dmaven.repo.local=D:\code\CareerAgent\.tools\m2-repository" -B test
pnpm --dir frontend test
pnpm --dir frontend build
```

## 一键公网部署

仓库根目录包含 `render.yaml` 和前后端一体化云端镜像。登录 Render 后点击下面按钮，即可创建 Web 服务和 PostgreSQL；首次构建完成后会得到一个 HTTPS 公网网址。

[Deploy to Render](https://render.com/deploy?repo=https://github.com/makabaka-commits/CareerAgent)

免费实例适合作品集演示，会休眠且上传文件是临时存储；长期使用时应升级实例并挂载持久磁盘。云端镜像已包含中英文 Tesseract OCR；模型默认关闭，未配置密钥时使用规则保障模式。

## 文档

- [系统架构](docs/architecture.md)
- [评分设计](docs/scoring.md)
- [API 摘要](docs/api.md)
- [部署说明](docs/deployment.md)
- [演示与面试讲解](docs/demo.md)

> 隐私提示：上传的简历默认保存在本机 `storage/`。请勿提交真实简历、密钥或生产环境变量。
