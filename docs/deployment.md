# 部署说明

## 本地开发

后端默认使用磁盘 H2 和本地文件存储，前端开发服务器代理 `/api`。访问地址是 `http://localhost:5173`。

## Docker Compose

```powershell
Copy-Item deploy/.env.example deploy/.env
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up --build
```

访问 `http://localhost`。Nginx 提供前端并反向代理 `/api`，因此浏览器只访问一个入口；PostgreSQL、Redis 和文件均使用持久卷。

## 公网域名

项目可以部署成 `https://career.example.com`：

1. 在云服务器安装 Docker，把仓库拉取到服务器。
2. 修改 `deploy/.env`，至少替换数据库密码与 `APP_TOKEN_SECRET`。
3. 启动 Compose，并在云平台安全组开放 80/443。
4. 将域名 A 记录指向服务器 IP，用 Caddy、Nginx Proxy Manager 或云负载均衡配置 HTTPS。
5. 把 `CORS_ORIGIN` 设置为最终 HTTPS 域名。

如果使用 Railway/Render，可将前后端建为两个服务，并连接托管 PostgreSQL/Redis。不要把 `.env` 或 API Key 提交到 GitHub。

## Render 一键部署（推荐用于简历演示）

项目已经提供根目录 `render.yaml` 和 `deploy/Dockerfile.cloud`。云端镜像会把 Vue 构建产物打进 Spring Boot，因此前端、API 和 SSE 共用一个 HTTPS 域名，不需要额外处理跨域。

1. 打开 `https://render.com/deploy?repo=https://github.com/makabaka-commits/CareerAgent`；
2. 使用 GitHub 登录并允许读取该仓库；
3. 确认创建 `stepwise-app` 和 `stepwise-db`；
4. 等待健康检查通过，访问 Render 提供的 `onrender.com` 地址；
5. 如需真实模型，在服务环境变量中添加 `AI_PROVIDER=openai` 和 `AI_API_KEY`。

免费 Web 服务可能休眠，首次访问需要等待启动；免费实例没有持久磁盘，所以演示上传文件可能在重新部署后消失，结构化业务数据仍保存在 PostgreSQL。正式使用应挂载 `/app/storage` 持久磁盘。

## 生产检查清单

- 使用至少 32 字节随机 Token Secret；
- 仅通过环境变量提供模型密钥；
- 为数据库和简历卷配置备份；
- 监控 `/actuator/health` 与 `/actuator/prometheus`；
- 在公网入口启用 HTTPS、请求体限制和访问日志；
- 使用真实用户前补充隐私政策和删除账户能力。
