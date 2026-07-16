# WorkMate AI 企业知识库问答 Agent

> 业务流程说明（V1.0）

## 1. 系统整体架构

```text
用户浏览器
    │
    │ HTTP / Axios
    ▼
Vue 3 + Element Plus
    │
    │ REST API
    ▼
Spring Boot Controller
    │
    ▼
Service 业务层
    │
    ├───────────────┬────────────────┐
    ▼               ▼                ▼
Redis 缓存       Mapper / MySQL     大模型 API
```

系统采用前后端分离架构。

- Controller：接收请求、获取请求头、参数校验、返回统一结果。
- Service：处理完整业务流程。
- Mapper：访问 MySQL。
- Redis：缓存最终问答结果。
- 大模型：根据检索到的知识组织答案。

---

## 2. 核心业务模块

1. 知识分类模块。
2. 知识管理模块。
3. 会话与消息模块。
4. Agent 问答模块。
5. 工单模块。
6. 模型调用日志模块。

前端、缓存、异常处理和部署属于支撑能力，不计入六个核心业务模块。

---

## 3. 一次请求的完整流转

```text
Vue 页面
→ Axios
→ Controller
→ DTO
→ Service
→ Redis / Mapper / LlmClient
→ Entity
→ Service
→ VO
→ CommonResult
→ Controller
→ Vue 页面
```

职责：

- DTO：接收前端请求参数。
- Entity：对应数据库表。
- VO：返回前端页面数据。
- CommonResult：统一封装响应。

---

## 4. 知识维护流程

```text
管理员提交分类或知识
→ Controller 接收
→ 校验 X-User-Id
→ 查询 sys_user 并验证 ADMIN
→ Service 校验业务数据
→ Mapper 写入 MySQL
→ 数据库事务提交
→ 清理 agent:answer:* 缓存
→ 返回成功结果
```

知识变化后需要清理缓存的操作：

- 新增知识。
- 修改知识。
- 启停知识。
- 删除知识。
- 修改分类名称。
- 启停分类。

---

## 5. Agent 问答主流程

```text
用户输入问题
→ 校验当前用户
→ 校验会话存在且属于当前用户
→ 规范化问题
→ 保存用户消息
→ 查询 Redis
```

### 5.1 缓存命中

```text
Redis 命中
→ 读取缓存中的 answer 和 references
→ 保存 Agent 消息
→ 保存知识引用关系
→ 更新会话最后消息时间
→ 异步保存 CACHE_HIT 日志
→ 返回 fromCache=true
```

缓存命中仍然必须保存本轮聊天记录。

### 5.2 缓存未命中且检索到知识

```text
Redis 未命中
→ 使用 MySQL LIKE 检索最多 5 条有效知识
→ 构造 Prompt
→ 调用大模型 API
→ 保存 Agent 回答
→ 保存知识引用关系
→ 更新会话最后消息时间
→ 写入 Redis
→ 异步保存 SUCCESS 日志
→ 返回 fromCache=false
```

### 5.3 未检索到知识

```text
Redis 未命中
→ MySQL 检索为空
→ 不调用大模型
→ 保存知识不足提示消息
→ 更新会话最后消息时间
→ 异步保存 NO_KNOWLEDGE 日志
→ 返回 canCreateTicket=true
```

### 5.4 模型调用失败

```text
已保存用户问题
→ 已检索到知识
→ 大模型调用失败
→ 不保存虚假 Agent 回答
→ 异步保存 FAILED 日志
→ 返回明确错误
→ 前端允许用户重试或创建工单
```

---

## 6. 数据库事务边界

禁止将大模型 HTTP 调用放入长数据库事务。

推荐拆分：

### 事务一

```text
保存用户问题
更新会话最后消息时间
```

### 非事务流程

```text
查询 Redis
检索知识
构造 Prompt
调用大模型
```

### 事务二

```text
保存 Agent 回答
保存知识引用关系
更新会话最后消息时间
```

### 事务外

```text
写入 Redis
异步保存模型调用日志
```

---

## 7. Redis 缓存流程

缓存 Key：

```text
agent:answer:{questionHash}
```

问题规范化：

```text
去除首尾空格
→ 合并连续空格
→ 英文字母转小写
→ 生成 MD5 或 SHA-256 摘要
```

缓存内容：

```json
{
  "answer": "最终回答",
  "references": []
}
```

不缓存：

- sessionId。
- messageId。
- traceSteps。
- fromCache。
- 用户信息。

TTL：

```text
30 分钟
```

Redis 故障时：

```text
记录错误
→ 跳过缓存
→ 继续数据库检索和模型调用
```

---

## 8. 知识检索流程

第一版采用 MySQL `LIKE` 检索：

```text
用户问题
→ 规范化关键词
→ 查询 title / keywords / content
→ 只返回启用且未删除的知识
→ INNER JOIN 有效分类
→ 按更新时间排序
→ 最多返回 5 条
```

第一版不实现：

- 向量检索。
- Embedding。
- Elasticsearch。
- 混合检索。

---

## 9. 会话与消息流程

### 9.1 创建会话

```text
用户请求创建会话
→ 保存 chat_session
→ 允许空会话存在
→ 返回 sessionId
```

### 9.2 查询会话列表

```text
chat_session
LEFT JOIN 最后一条 chat_message
```

即使会话没有消息，也必须返回。

### 9.3 查询消息

按以下顺序返回：

```sql
ORDER BY created_at ASC, id ASC
```

用户只能查询自己的会话和消息。

---

## 10. 工单流程

```text
Agent 返回 canCreateTicket=true
→ 用户点击创建工单
→ 后端验证会话和问题消息归属
→ 创建 PENDING 工单
→ 管理员查询全部工单
→ 更新为 PROCESSING
→ 填写 resolution
→ 更新为 RESOLVED
→ 最终可更新为 CLOSED
```

允许状态流转：

```text
PENDING → PROCESSING
PENDING → RESOLVED
PROCESSING → RESOLVED
RESOLVED → CLOSED
```

禁止反向回退。

---

## 11. 模型调用日志流程

使用 `@Async` 和自定义线程池保存日志。

日志状态：

- `CACHE_HIT`
- `SUCCESS`
- `FAILED`
- `NO_KNOWLEDGE`

日志失败时：

```text
记录错误
不回滚主业务
不影响已经生成的问答结果
```

禁止直接使用：

```java
new Thread(...)
```

---

## 12. Agent 工具流程

第一版只有两个工具：

### searchKnowledge

- 根据问题检索有效知识。
- 可由 Agent 主流程自动调用。

### createTicket

- 用户明确确认后创建工单。
- 大模型不能未经用户确认直接执行。

工具统一通过：

```java
Map<String, AgentTool>
```

进行管理，用于学习接口、实现类、多态和集合。

---

## 13. Agent Trace

Trace 是系统执行摘要，不是模型思维链。

建议展示：

- 缓存查询。
- 知识检索。
- 模型调用。
- 消息保存。

示例：

```json
{
  "step": "CACHE_LOOKUP",
  "description": "检查 Redis 问答缓存",
  "success": true,
  "detail": "缓存未命中"
}
```

禁止返回：

- Chain of Thought。
- 系统 Prompt 全文。
- API Key。
- 原始异常堆栈。
- 外部服务认证信息。

---

## 14. Controller、Service、Mapper 调用关系

```text
KnowledgeController
→ KnowledgeService
→ KnowledgeMapper
→ MySQL
```

```text
ChatSessionController
→ ChatSessionService
→ ChatSessionMapper / ChatMessageMapper
→ MySQL
```

```text
AgentController
→ AgentService
→ CacheService / KnowledgeService / LlmClient / ChatMessageService
```

```text
TicketController
→ TicketService
→ TicketMapper
→ MySQL
```

原则：

1. Controller 不写 SQL。
2. Controller 不直接操作 Mapper。
3. Controller 不直接操作 Redis。
4. Controller 不直接调用大模型。
5. Mapper 不写业务逻辑。
6. Entity 不直接返回前端。
