# WorkMate AI 企业知识库问答 Agent

> 阶段 0 验收与开发基线  
> 版本：V1.0  
> 状态：冻结  
> 适用范围：第一版 MVP

## 1. 文档优先级

后续开发依据：

```text
05-阶段0验收与开发基线.md
→ 03-接口设计.md
→ 02-数据库设计.md
→ 01-需求规格说明书.md
→ 04-业务流程说明.md
```

存在冲突时，以优先级更高的文档为准。

---

## 2. 阶段 0 结论

已完成：

- 需求范围。
- 角色与权限。
- 业务流程。
- 技术范围。
- 非目标范围。
- 数据库表结构。
- 接口设计。
- 开发阶段。
- 验收标准。
- Codex 指导规则。

结论：

```text
阶段 0 验收通过
允许进入阶段 1
```

---

## 3. 项目最终定义

项目名称：

```text
WorkMate AI
```

项目类型：

```text
企业内部知识库问答 Agent
```

核心链路：

```text
用户提问
→ 校验用户和会话
→ 保存用户消息
→ Redis 缓存
→ MySQL 知识检索
→ Prompt
→ 大模型 API
→ 保存 Agent 回答
→ 保存引用关系
→ 更新会话
→ 写入缓存
→ 异步日志
→ 返回前端
```

无知识时：

```text
不调用大模型自由回答
→ 返回知识不足提示
→ canCreateTicket=true
→ 用户确认后创建工单
```

---

## 4. 第一版角色基线

### EMPLOYEE

- 查询有效知识。
- 管理自己的会话。
- Agent 提问。
- 创建和查看自己的工单。

### ADMIN

- 管理分类和知识。
- 查看、处理全部工单。
- 查看模型调用日志。

第一版不实现正式登录，统一使用：

```http
X-User-Id
```

---

## 5. 功能范围基线

六个核心业务模块：

1. 知识分类。
2. 知识管理。
3. 会话与消息。
4. Agent 问答。
5. 工单。
6. 模型调用日志。

支撑能力：

- Redis。
- 全局异常处理。
- 参数校验。
- Vue。
- Docker Compose。
- Nginx。
- GitHub。

---

## 6. 禁止范围

第一版禁止加入：

- 登录、JWT、Spring Security、RBAC。
- 文件上传、PDF、Markdown 自动导入。
- Obsidian 自动同步。
- Embedding、向量数据库、Elasticsearch。
- LangChain4j、复杂 Spring AI 工作流。
- 多 Agent。
- WebSocket、SSE。
- Kafka、RabbitMQ。
- 微服务、Spring Cloud。
- Kubernetes。
- 分布式锁。
- GitHub Actions。
- 复杂监控系统。

新增需求默认进入后续版本清单。

---

## 7. 技术栈基线

后端：

```text
Java 17
Spring Boot 3
Spring Web
MyBatis-Plus
MySQL 8
Spring Data Redis
Bean Validation
Spring Async
Maven
```

前端：

```text
Vue 3
Vite
Axios
Element Plus
```

部署：

```text
Docker
Docker Compose
Nginx
云服务器
GitHub
```

AI：

```text
HTTP 调用大模型 API
独立 LlmClient 接口封装
```

---

## 8. 数据库基线

固定 8 张表：

```text
sys_user
knowledge_category
knowledge
chat_session
chat_message
knowledge_reference
ticket
model_call_log
```

不得擅自新增表。

第一版不创建数据库物理外键，由 Service 校验关联完整性。

逻辑删除表：

```text
knowledge_category
knowledge
chat_session
chat_message
ticket
```

---

## 9. SQL 基线

必须真实使用：

### INNER JOIN

查询知识与有效分类。

### LEFT JOIN

查询会话和最后一条消息，空会话也必须返回。

不能只在文档中出现，必须落实到 Mapper SQL。

---

## 10. 事务基线

禁止：

```text
数据库事务
包含大模型 HTTP 长时间调用
```

推荐：

### 事务一

- 保存用户问题。
- 更新会话时间。

### 非事务

- Redis。
- 知识检索。
- Prompt。
- 大模型调用。

### 事务二

- 保存 Agent 回答。
- 保存引用关系。
- 更新会话时间。

### 事务外

- Redis 写入。
- 异步模型日志。

模型失败时：

```text
不保存虚假 Agent 回答
```

---

## 11. Redis 基线

缓存：

- 最终回答。
- 引用知识。

不缓存：

- sessionId。
- messageId。
- traceSteps。
- fromCache。
- 用户信息。

Key：

```text
agent:answer:{questionHash}
```

TTL：

```text
30 分钟
```

Redis 故障时：

```text
记录错误
→ 跳过缓存
→ 继续主问答流程
```

---

## 12. Agent Trace 基线

Trace 是系统执行摘要，不是模型思维链。

重点展示：

- 缓存查询。
- 知识检索。
- 模型调用。
- 消息保存。

禁止返回：

- Chain of Thought。
- 系统 Prompt 全文。
- API Key。
- 原始异常堆栈。
- 敏感请求头。

Trace 不单独建表。

---

## 13. 工单基线

创建工单前，后端验证：

- 会话属于当前用户。
- 问题消息属于当前用户。
- 问题消息属于指定会话。
- 消息角色为 USER。

允许流转：

```text
PENDING → PROCESSING
PENDING → RESOLVED
PROCESSING → RESOLVED
RESOLVED → CLOSED
```

状态为 `RESOLVED` 时：

- resolution 必填。
- handled_by 设置管理员。
- resolved_at 设置当前时间。

---

## 14. 包结构基线

后端：

```text
com.workmate.ai
├── common
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── mapper
├── service
│   └── impl
├── tool
│   └── impl
├── client
│   └── impl
├── utils
└── vo
```

前端：

```text
src
├── api
├── assets
├── components
├── router
├── utils
├── views
└── App.vue
```

第一版不强制 Pinia。

---

## 15. Agent 工具基线

第一版只有：

```text
searchKnowledge
createTicket
```

工具接口建议：

```java
public interface AgentTool {

    String getName();

    Object execute(Map<String, Object> parameters);
}
```

通过：

```java
Map<String, AgentTool>
```

管理工具。

`createTicket` 必须在用户明确确认后执行。

---

## 16. 异步日志基线

使用：

```java
@Async
```

配置自定义线程池。

禁止：

```java
new Thread(...)
```

异步失败：

- 记录错误。
- 不回滚主业务。
- 不影响问答响应。

---

## 17. 配置与安全基线

不得提交 GitHub：

- API Key。
- 数据库生产密码。
- Redis 生产密码。
- SSH 私钥。
- 证书私钥。
- 个人隐私数据。

提交：

```text
.env.example
```

不提交：

```text
.env
```

`.env` 必须进入 `.gitignore`。

---

## 18. 开发阶段

项目共九个阶段：

### 阶段 0：需求与设计

状态：已完成。

### 阶段 1：后端基础工程

- Spring Boot 空项目。
- MySQL。
- MyBatis-Plus。
- 包结构。
- 统一返回。
- 全局异常。
- 参数校验。
- 建表与初始化。

### 阶段 2：知识库 CRUD

- 分类。
- 知识。
- 分页。
- 搜索。
- INNER JOIN。

### 阶段 3：会话与消息

- 会话。
- 消息。
- LEFT JOIN。

### 阶段 4：大模型问答

- LlmClient。
- Prompt。
- 知识检索。
- 消息保存。
- 引用关系。
- Trace。

### 阶段 5：Redis

- 缓存读写。
- TTL。
- 缓存命中。
- 缓存失效。
- Redis 降级。

### 阶段 6：Agent 工具、工单与异步日志

- AgentTool。
- 工具实现。
- 工单。
- `@Async`。
- 自定义线程池。

### 阶段 7：Vue 与联调

- 聊天页面。
- 知识管理。
- 分类管理。
- 工单管理。
- Axios。

### 阶段 8：Docker 与项目包装

- Dockerfile。
- Docker Compose。
- Nginx。
- 云服务器。
- README。
- 截图。
- 演示数据。

---

## 19. 每阶段统一流程

```text
确认需求
→ 拆分步骤
→ 编码
→ 编译
→ 启动
→ Postman或页面测试
→ 检查MySQL或Redis
→ 修复
→ 更新文档
→ Git提交
→ 阶段验收
```

禁止：

- 接口未测试就开发下一模块。
- 项目无法启动还继续堆代码。
- 只看代码不真实运行。

---

## 20. Git 基线

建议提交：

```text
docs: add project requirements and design baseline
feat: initialize Spring Boot backend
feat: add common response and exception handling
feat: implement knowledge category management
feat: implement knowledge CRUD and search
feat: implement chat session and message module
feat: integrate LLM knowledge answering
feat: add Redis answer cache
feat: implement agent tools and ticket workflow
feat: add asynchronous model call logs
feat: complete Vue frontend integration
deploy: add Docker Compose deployment
```

一次提交只解决一个明确问题。

---

## 21. 阶段 0 最终验收

- [x] 目标明确。
- [x] 角色明确。
- [x] 第一版功能明确。
- [x] 非目标明确。
- [x] 业务流程明确。
- [x] 8 张表明确。
- [x] 接口明确。
- [x] 缓存策略明确。
- [x] 事务边界明确。
- [x] Agent Trace 明确。
- [x] 工单状态明确。
- [x] 开发阶段明确。
- [x] Git 规则明确。
- [x] Docker 部署目标明确。

最终结论：

```text
阶段 0 验收通过
允许进入阶段 1：后端基础工程
```

阶段 1 的第一项任务只能是：

```text
确认本地环境
→ 创建目录和 Git 仓库
→ 创建 Spring Boot 空项目
→ 验证项目可以启动
```

不得提前开发知识、会话、Agent、Redis或工单功能。
