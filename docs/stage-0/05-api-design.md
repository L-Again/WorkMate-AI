# WorkMate AI 企业知识库问答 Agent

> 接口设计文档  
> 版本：V1.0  
> 风格：RESTful API  
> 数据格式：JSON  
> 基础路径：`/api`

## 1. 基础约定

本地地址：

```text
http://localhost:8080/api
```

第一版身份模拟：

```http
X-User-Id: 1
```

预置用户：

```text
1：普通员工
2：管理员
```

请求体统一使用：

```http
Content-Type: application/json
```

---

## 2. 统一返回结构

成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

失败：

```json
{
  "code": 40001,
  "message": "请求参数错误",
  "data": null
}
```

分页：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "pageNum": 1,
    "pageSize": 10,
    "total": 0,
    "pages": 0
  }
}
```

---

## 3. 业务状态码

| code | 含义 |
|---:|---|
| 200 | 成功 |
| 40001 | 请求参数错误 |
| 40002 | 缺少用户身份 |
| 40003 | 用户不存在或停用 |
| 40301 | 无权限 |
| 40401 | 数据不存在 |
| 40901 | 状态冲突 |
| 50001 | 系统错误 |
| 50002 | 数据库失败 |
| 50003 | Redis 失败 |
| 50004 | 大模型调用失败 |
| 50005 | 异步日志失败 |

---

## 4. 用户接口

### 获取当前用户

```http
GET /api/users/current
X-User-Id: 1
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "employee_demo",
    "displayName": "演示员工",
    "role": "EMPLOYEE",
    "status": 1
  }
}
```

---

## 5. 知识分类接口

### 新增分类

```http
POST /api/knowledge/categories
X-User-Id: 2
```

```json
{
  "name": "研发规范",
  "description": "Java、Git、接口和数据库开发规范",
  "sortOrder": 3
}
```

### 查询分类列表

```http
GET /api/knowledge/categories
GET /api/knowledge/categories?includeDisabled=true
```

### 查询分类详情

```http
GET /api/knowledge/categories/{id}
```

### 修改分类

```http
PUT /api/knowledge/categories/{id}
```

```json
{
  "name": "研发规范",
  "description": "研发团队内部规范",
  "sortOrder": 3
}
```

### 启停分类

```http
PATCH /api/knowledge/categories/{id}/status
```

```json
{
  "status": 0
}
```

规则：

- 新增、修改、启停仅管理员可操作。
- 分类停用后，其知识不参与 Agent 检索。
- 修改名称或状态后清理问答缓存。

---

## 6. 知识接口

### 新增知识

```http
POST /api/knowledge
X-User-Id: 2
```

```json
{
  "categoryId": 3,
  "title": "Git 分支命名规范",
  "keywords": "Git,分支,branch,feature,bugfix",
  "content": "功能分支统一使用 feature/功能名称。",
  "status": 1
}
```

### 修改知识

```http
PUT /api/knowledge/{id}
```

### 查询详情

```http
GET /api/knowledge/{id}
```

### 分页查询

```http
GET /api/knowledge?pageNum=1&pageSize=10&keyword=Git&categoryId=3&status=1
```

返回记录建议字段：

```json
{
  "id": 3,
  "categoryId": 3,
  "categoryName": "研发规范",
  "title": "Git 分支命名规范",
  "keywords": "Git,分支,branch",
  "status": 1,
  "updatedAt": "2026-07-16 19:00:00"
}
```

### 启停知识

```http
PATCH /api/knowledge/{id}/status
```

### 逻辑删除知识

```http
DELETE /api/knowledge/{id}
```

### 内部搜索测试接口

```http
GET /api/knowledge/search?keyword=Git&limit=5
```

规则：

- 新增、修改、启停、删除仅管理员可操作。
- `createdBy`、`updatedBy` 从当前用户获取。
- 知识变化后清理 Redis 问答缓存。
- 分页和搜索查询真实使用 `INNER JOIN`。

---

## 7. 会话接口

### 创建会话

```http
POST /api/chat/sessions
```

```json
{
  "title": "Git 规范咨询"
}
```

### 查询自己的会话

```http
GET /api/chat/sessions?pageNum=1&pageSize=20
```

返回：

```json
{
  "sessionId": 21,
  "title": "Git 规范咨询",
  "lastMessage": "功能分支统一使用 feature/……",
  "lastMessageAt": "2026-07-16 19:20:00",
  "createdAt": "2026-07-16 19:00:00"
}
```

该接口必须使用 `LEFT JOIN`，空会话也必须返回。

### 查询会话详情

```http
GET /api/chat/sessions/{sessionId}
```

### 修改标题

```http
PATCH /api/chat/sessions/{sessionId}/title
```

```json
{
  "title": "Git 与代码提交规范"
}
```

### 删除会话

```http
DELETE /api/chat/sessions/{sessionId}
```

### 查询消息

```http
GET /api/chat/sessions/{sessionId}/messages?pageNum=1&pageSize=50
```

消息按：

```sql
ORDER BY created_at ASC, id ASC
```

用户只能访问自己的会话。

---

## 8. Agent 核心接口

### 提交问题

```http
POST /api/agent/chat
X-User-Id: 1
```

```json
{
  "sessionId": 21,
  "question": "Git 分支应该怎么命名？"
}
```

### 正常回答

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": 21,
    "questionMessageId": 101,
    "answerMessageId": 102,
    "answer": "根据研发规范，功能分支应使用 feature/功能名称。",
    "fromCache": false,
    "canCreateTicket": false,
    "references": [
      {
        "knowledgeId": 3,
        "title": "Git 分支命名规范",
        "categoryName": "研发规范"
      }
    ],
    "traceSteps": [
      {
        "step": "CACHE_LOOKUP",
        "description": "检查 Redis 问答缓存",
        "success": true,
        "detail": "缓存未命中"
      },
      {
        "step": "KNOWLEDGE_SEARCH",
        "description": "检索企业知识库",
        "success": true,
        "detail": "找到 1 条相关知识"
      },
      {
        "step": "LLM_CALL",
        "description": "调用大模型生成回答",
        "success": true,
        "detail": "调用成功"
      }
    ]
  }
}
```

### 缓存命中

返回：

```json
{
  "fromCache": true,
  "canCreateTicket": false
}
```

业务要求：

- 缓存命中仍保存用户消息和 Agent 消息。
- 不调用大模型。
- 日志状态为 `CACHE_HIT`。

### 无知识

```json
{
  "code": 200,
  "message": "知识库暂无可靠答案",
  "data": {
    "answer": "当前知识库中没有找到可靠内容。你可以创建人工咨询工单。",
    "fromCache": false,
    "canCreateTicket": true,
    "references": []
  }
}
```

业务要求：

- 无知识时不调用大模型。
- 保存用户问题和知识不足提示消息。
- 日志状态为 `NO_KNOWLEDGE`。

### 模型失败

```json
{
  "code": 50004,
  "message": "大模型服务暂时不可用，请稍后重试",
  "data": {
    "sessionId": 21,
    "questionMessageId": 107,
    "answerMessageId": null,
    "fromCache": false,
    "canCreateTicket": true
  }
}
```

模型失败时不保存虚假 Agent 回答。

---

## 9. 工单接口

### 创建工单

```http
POST /api/tickets
```

```json
{
  "sessionId": 21,
  "questionMessageId": 105,
  "title": "咨询开发机申请流程",
  "description": "Agent 未找到相关知识，希望人工确认。"
}
```

返回：

```json
{
  "ticketId": 15,
  "ticketNo": "TK20260716000125",
  "status": "PENDING"
}
```

### 查询自己的工单

```http
GET /api/tickets/my?pageNum=1&pageSize=10&status=PENDING
```

### 查询工单详情

```http
GET /api/tickets/{ticketId}
```

普通员工只能查看自己的工单，管理员可查看全部工单。

### 管理员查询全部工单

```http
GET /api/admin/tickets?pageNum=1&pageSize=10&status=PENDING&keyword=开发机
```

### 更新工单状态

```http
PATCH /api/admin/tickets/{ticketId}/status
```

```json
{
  "status": "RESOLVED",
  "resolution": "由项目负责人提交资产申请，审批后由 IT 配置。"
}
```

状态变为 `RESOLVED` 时，`resolution` 必填。

---

## 10. 模型日志接口

### 分页查询

```http
GET /api/admin/model-logs?pageNum=1&pageSize=20&callStatus=SUCCESS
```

### 统计

```http
GET /api/admin/model-logs/statistics
```

返回建议：

```json
{
  "totalRequests": 120,
  "modelCalls": 82,
  "cacheHits": 25,
  "noKnowledgeCount": 10,
  "failedCount": 3,
  "cacheHitRate": 0.2083,
  "averageDurationMs": 1260
}
```

统计接口优先级低于日志写入和分页查询。

---

## 11. 健康检查

```http
GET /api/health
```

返回：

```json
{
  "application": "UP",
  "mysql": "UP",
  "redis": "UP",
  "timestamp": "2026-07-16 20:30:00"
}
```

健康检查不真实调用大模型，避免额外费用与延迟。

---

## 12. DTO 建议

- CategoryCreateDTO
- CategoryUpdateDTO
- StatusUpdateDTO
- KnowledgeCreateDTO
- KnowledgeUpdateDTO
- KnowledgeQueryDTO
- SessionCreateDTO
- SessionTitleUpdateDTO
- AgentChatDTO
- TicketCreateDTO
- TicketStatusUpdateDTO
- TicketQueryDTO
- ModelLogQueryDTO

示例：

```java
public class AgentChatDTO {

    @NotNull
    private Long sessionId;

    @NotBlank
    @Size(max = 2000)
    private String question;
}
```

---

## 13. VO 建议

- CurrentUserVO
- CategoryVO
- KnowledgeDetailVO
- KnowledgeListVO
- KnowledgeSearchVO
- SessionVO
- SessionListVO
- MessageVO
- KnowledgeReferenceVO
- AgentAnswerVO
- AgentTraceStepVO
- TicketVO
- TicketListVO
- ModelCallLogVO
- ModelLogStatisticsVO
- HealthVO
- PageResult<T>

---

## 14. 权限矩阵

| 功能 | 员工 | 管理员 |
|---|---:|---:|
| 获取当前用户 | 是 | 是 |
| 查询有效分类 | 是 | 是 |
| 管理分类 | 否 | 是 |
| 查询有效知识 | 是 | 是 |
| 管理知识 | 否 | 是 |
| 管理自己的会话 | 是 | 是 |
| Agent 提问 | 是 | 是 |
| 创建工单 | 是 | 是 |
| 查看自己的工单 | 是 | 是 |
| 查看全部工单 | 否 | 是 |
| 更新工单 | 否 | 是 |
| 查看模型日志 | 否 | 是 |

---

## 15. Redis 约定

Key：

```text
agent:answer:{questionHash}
```

TTL：

```text
30 分钟
```

缓存内容：

```json
{
  "answer": "……",
  "references": []
}
```

知识变化后清理：

```text
agent:answer:*
```

Redis 异常时跳过缓存，不阻塞主流程。

---

## 16. Postman 核心验收

1. 缺少 `X-User-Id` 返回错误。
2. 普通员工不能新增分类和知识。
3. 新增、修改、启停知识成功。
4. 空会话能够通过 `LEFT JOIN` 查询。
5. 首次提问 `fromCache=false`。
6. 第二次相同问题 `fromCache=true`。
7. 第二次不调用大模型。
8. 无知识时 `canCreateTicket=true`。
9. 模型失败时不保存虚假回答。
10. 用户不能访问他人会话和工单。
11. 非法工单状态流转失败。
12. 模型日志正确区分四种状态。
