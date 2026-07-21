# WorkMate AI Docker Compose 部署说明

本文档说明本地或云服务器上的 Docker Compose 部署流程。

## 服务组成

- `mysql`：MySQL 8.4，使用命名卷 `workmate_mysql_data` 持久化数据。
- `redis`：Redis 7.4，使用命名卷 `workmate_redis_data` 持久化数据。
- `backend`：Spring Boot 后端，Docker 默认使用 Mock LLM。
- `frontend`：Nginx 托管 Vue 构建产物，并将 `/api` 代理到 `backend:8080`。

## 启动

```bash
cp .env.example .env
./scripts/docker-up.sh
```

默认访问：

```text
http://localhost
```

如果 `.env` 中使用了临时端口，例如：

```dotenv
FRONTEND_HOST_PORT=8088
BACKEND_HOST_PORT=8081
```

则访问：

```text
http://localhost:8088
```

后端健康检查：

```bash
curl http://localhost:8080/api/health
```

如果后端使用临时端口：

```bash
curl http://localhost:8081/api/health
```

期望返回：

```json
{"code":200,"message":"success","data":{"application":"UP"}}
```

## 构建策略

`./scripts/docker-up.sh` 会先构建本地产物，再构建 Docker 镜像：

- 后端：本地执行 `mvn -B -DskipTests package`，Docker 只复制 `backend/target/*.jar`。
- 前端：本地执行 `npm run build`，Docker 只复制 `frontend/dist`。

这样可以避免 Docker build 阶段在容器内反复下载 Maven/npm 依赖，降低网络失败概率。

## LLM 模式

Docker 部署默认使用 Mock：

```dotenv
DOCKER_WORKMATE_LLM_PROVIDER=mock
DOCKER_WORKMATE_LLM_MODEL=mock-llm
DOCKER_DEEPSEEK_API_KEY=
```

本地后端的 `WORKMATE_LLM_PROVIDER=deepseek` 不会影响 Docker。Docker 只读取 `DOCKER_` 前缀的 LLM 变量。

如果需要 Docker 也使用 DeepSeek，只在本机 `.env` 中设置：

```dotenv
DOCKER_WORKMATE_LLM_PROVIDER=deepseek
DOCKER_WORKMATE_LLM_MODEL=deepseek-v4-flash
DOCKER_DEEPSEEK_BASE_URL=https://api.deepseek.com
DOCKER_DEEPSEEK_API_KEY=your_local_key
```

## 端口

默认端口：

- 前端/Nginx：`80`
- 后端：`8080`
- MySQL：`3306`
- Redis：`6379`

如果端口被占用，在 `.env` 中修改宿主机端口：

```dotenv
MYSQL_HOST_PORT=3307
REDIS_HOST_PORT=6380
BACKEND_HOST_PORT=8081
FRONTEND_HOST_PORT=8088
```

## 数据初始化

MySQL 初始化时会执行：

- `backend/src/main/resources/db/schema.sql`
- `backend/src/main/resources/db/data.sql`

当前 `data.sql` 包含演示用户、分类和企业知识库种子数据。已有 Docker volume 时，重新 `docker compose up -d` 不会重复初始化数据库。

清空本地演示数据并重新初始化：

```bash
docker compose down -v
./scripts/docker-up.sh
```

不要对需要保留的数据执行 `docker compose down -v`。

## 常见问题

如果 Docker Hub 拉取基础镜像失败，先确认网络后重试。由于本项目 Dockerfile 不再在容器内执行 Maven/npm 依赖下载，失败点通常只剩基础镜像拉取。

如果修改了前后端代码，重新执行：

```bash
./scripts/docker-up.sh
```
