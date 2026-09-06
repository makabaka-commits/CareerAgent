# API 摘要

统一前缀 `/api/v1`，除注册登录和健康检查外均需 `Authorization: Bearer <token>`。

| 领域 | 主要接口 |
|---|---|
| Auth | `POST /auth/register`、`POST /auth/login` |
| Profile | `GET/PUT /profiles/me`、`GET/POST /profiles/me/skills` |
| Resume | `POST /resumes`、`POST /resumes/{id}/parse`、`POST /resumes/{id}/confirm` |
| Job | `POST /jobs`、`GET /jobs`、`POST /jobs/{id}/match` |
| Agent | `POST /conversations`、`POST /{id}/messages`、`GET /{id}/stream` |
| RAG | `POST /knowledge/search`、`POST /admin/knowledge/documents` |
| Interview | `POST /interviews`、`POST /{id}/start`、`POST /{id}/answers`、`POST /{id}/finish` |

SSE 事件为 `status`、`tool_call`、`content`、`citation`、`done`、`error`。示例请求见 `tests/api/demo.http`。

