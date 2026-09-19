# API 摘要

统一前缀 `/api/v1`，除注册登录和健康检查外均需 `Authorization: Bearer <token>`。

| 领域 | 主要接口 |
|---|---|
| Auth | `POST /auth/register`、`POST /auth/login`、`POST /auth/demo` |
| Profile | `GET/PUT /profiles/me`、`GET/POST /profiles/me/skills` |
| Resume | `POST /resumes`、`POST /resumes/{id}/parse`、`POST /resumes/{id}/confirm`、`DELETE /resumes/{id}` |
| Job | `POST /jobs`、`GET /jobs`、`POST /jobs/{id}/match` |
| Agent | `POST /conversations`、`POST /{id}/messages`、`GET /{id}/stream` |
| RAG | `POST /knowledge/search`、`POST /admin/knowledge/documents` |
| Interview | `POST /interviews`、`POST /{id}/start`、`POST /{id}/answers`、`POST /{id}/finish` |

SSE 事件为 `status`、`tool_call`、`content`、`citation`、`done`、`error`。示例请求见 `tests/api/demo.http`。

`POST /auth/demo` 无需参数，会创建一次性体验用户、岗位和有意保留差距的技能证据，返回 `token` 与 `jobId`。匹配响应包含四维分、置信度、投递建议和逐项证据。
