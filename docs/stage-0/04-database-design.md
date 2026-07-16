# WorkMate AI 企业知识库问答 Agent

> 数据库设计文档  
> 版本：V1.0  
> 数据库：MySQL 8.0  
> 字符集：utf8mb4

## 1. 设计原则

1. 每张业务表使用 `BIGINT AUTO_INCREMENT` 主键。
2. 时间字段统一使用 `DATETIME`，Java 使用 `LocalDateTime`。
3. 业务删除优先采用逻辑删除。
4. 第一版不创建 MySQL 物理外键，关联完整性由 Service 校验。
5. 高频查询字段建立索引。
6. 不保存 API Key、数据库生产密码等敏感配置。
7. Agent Trace 不单独建表。
8. 第一版不使用触发器、存储过程和分库分表。

---

## 2. 数据表清单

第一版固定 8 张表：

| 表名 | 说明 |
|---|---|
| `sys_user` | 预置普通员工和管理员 |
| `knowledge_category` | 知识分类 |
| `knowledge` | 知识正文 |
| `chat_session` | 聊天会话 |
| `chat_message` | 用户与 Agent 消息 |
| `knowledge_reference` | Agent 消息与知识的多对多关系 |
| `ticket` | 人工咨询工单 |
| `model_call_log` | 模型与缓存调用日志 |

---

## 3. 表关系

```text
sys_user 1:N chat_session
chat_session 1:N chat_message

knowledge_category 1:N knowledge

chat_message N:N knowledge
通过 knowledge_reference 关联

sys_user 1:N ticket
chat_session 1:N ticket

sys_user 1:N model_call_log
chat_session 1:N model_call_log
```

---

## 4. 字段设计

### 4.1 sys_user

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| username | varchar(50) | 用户名，唯一 |
| display_name | varchar(50) | 显示名称 |
| role | varchar(20) | EMPLOYEE / ADMIN |
| status | tinyint | 1启用，0停用 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

索引：

- `uk_username(username)`
- `idx_role_status(role, status)`

### 4.2 knowledge_category

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| name | varchar(100) | 分类名称 |
| description | varchar(500) | 分类说明 |
| sort_order | int | 排序 |
| status | tinyint | 1启用，0停用 |
| is_deleted | tinyint | 0正常，1删除 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

索引：

- `uk_category_name(name, is_deleted)`
- `idx_category_status(status, is_deleted)`

### 4.3 knowledge

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| category_id | bigint | 分类 ID |
| title | varchar(200) | 标题 |
| keywords | varchar(500) | 英文逗号分隔关键词 |
| content | text | 正文 |
| status | tinyint | 1启用，0停用 |
| is_deleted | tinyint | 逻辑删除 |
| created_by | bigint | 创建人 |
| updated_by | bigint | 修改人 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

索引：

- `idx_knowledge_category(category_id)`
- `idx_knowledge_status(status, is_deleted)`
- `idx_knowledge_title(title)`
- `idx_knowledge_updated_at(updated_at)`

### 4.4 chat_session

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| user_id | bigint | 所属用户 |
| title | varchar(200) | 会话标题 |
| last_message_at | datetime | 最后消息时间 |
| is_deleted | tinyint | 逻辑删除 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

索引：

- `idx_session_user(user_id, is_deleted)`
- `idx_session_last_message(user_id, last_message_at)`

### 4.5 chat_message

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| session_id | bigint | 会话 ID |
| user_id | bigint | 用户 ID |
| role | varchar(20) | USER / ASSISTANT |
| content | text | 消息内容 |
| from_cache | tinyint | 是否来自缓存 |
| can_create_ticket | tinyint | 是否允许创建工单 |
| is_deleted | tinyint | 逻辑删除 |
| created_at | datetime | 创建时间 |

索引：

- `idx_message_session(session_id, created_at)`
- `idx_message_user(user_id, created_at)`
- `idx_message_role(role)`

### 4.6 knowledge_reference

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| message_id | bigint | Agent 消息 ID |
| knowledge_id | bigint | 知识 ID |
| created_at | datetime | 创建时间 |

索引：

- `uk_message_knowledge(message_id, knowledge_id)`
- `idx_reference_knowledge(knowledge_id)`

### 4.7 ticket

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| ticket_no | varchar(32) | 工单号，唯一 |
| user_id | bigint | 创建用户 |
| session_id | bigint | 来源会话，可空 |
| question_message_id | bigint | 来源问题消息，可空 |
| title | varchar(200) | 标题 |
| description | text | 问题描述 |
| status | varchar(20) | 工单状态 |
| resolution | text | 处理结果 |
| handled_by | bigint | 管理员 ID |
| resolved_at | datetime | 解决时间 |
| is_deleted | tinyint | 逻辑删除 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

索引：

- `uk_ticket_no(ticket_no)`
- `idx_ticket_user(user_id, created_at)`
- `idx_ticket_status(status, created_at)`
- `idx_ticket_session(session_id)`

### 4.8 model_call_log

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| session_id | bigint | 会话 ID |
| question_message_id | bigint | 问题消息 ID |
| answer_message_id | bigint | 回答消息 ID |
| model_name | varchar(100) | 模型名称 |
| from_cache | tinyint | 是否缓存命中 |
| call_status | varchar(20) | 调用状态 |
| duration_ms | bigint | 耗时毫秒 |
| prompt_tokens | int | 输入 Token |
| completion_tokens | int | 输出 Token |
| error_message | varchar(1000) | 错误信息 |
| created_at | datetime | 创建时间 |

索引：

- `idx_log_user(user_id, created_at)`
- `idx_log_session(session_id, created_at)`
- `idx_log_status(call_status, created_at)`
- `idx_log_cache(from_cache, created_at)`

---

## 5. 状态枚举

### 用户角色

```text
EMPLOYEE
ADMIN
```

### 消息角色

```text
USER
ASSISTANT
```

### 工单状态

```text
PENDING
PROCESSING
RESOLVED
CLOSED
```

### 模型调用状态

```text
CACHE_HIT
SUCCESS
FAILED
NO_KNOWLEDGE
```

---

## 6. JOIN 使用场景

### 6.1 INNER JOIN

查询知识与有效分类：

```sql
SELECT
    k.id,
    k.title,
    k.keywords,
    k.content,
    c.name AS category_name
FROM knowledge k
INNER JOIN knowledge_category c
    ON k.category_id = c.id
WHERE k.status = 1
  AND k.is_deleted = 0
  AND c.status = 1
  AND c.is_deleted = 0
  AND (
      k.title LIKE CONCAT('%', #{keyword}, '%')
      OR k.keywords LIKE CONCAT('%', #{keyword}, '%')
      OR k.content LIKE CONCAT('%', #{keyword}, '%')
  )
ORDER BY k.updated_at DESC
LIMIT 5;
```

### 6.2 LEFT JOIN

查询会话及最后一条消息。空会话也必须返回：

```sql
SELECT
    s.id,
    s.title,
    s.created_at,
    s.last_message_at,
    m.content AS last_message
FROM chat_session s
LEFT JOIN chat_message m
    ON m.id = (
        SELECT cm.id
        FROM chat_message cm
        WHERE cm.session_id = s.id
          AND cm.is_deleted = 0
        ORDER BY cm.created_at DESC, cm.id DESC
        LIMIT 1
    )
WHERE s.user_id = #{userId}
  AND s.is_deleted = 0
ORDER BY COALESCE(s.last_message_at, s.created_at) DESC;
```

---

## 7. 建表 SQL

```sql
CREATE DATABASE IF NOT EXISTS workmate_ai
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

USE workmate_ai;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    KEY idx_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_name (name, is_deleted),
    KEY idx_category_status (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    keywords VARCHAR(500) DEFAULT NULL,
    content TEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_knowledge_category (category_id),
    KEY idx_knowledge_status (status, is_deleted),
    KEY idx_knowledge_title (title),
    KEY idx_knowledge_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL DEFAULT '新会话',
    last_message_at DATETIME DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_session_user (user_id, is_deleted),
    KEY idx_session_last_message (user_id, last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    from_cache TINYINT NOT NULL DEFAULT 0,
    can_create_ticket TINYINT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_message_session (session_id, created_at),
    KEY idx_message_user (user_id, created_at),
    KEY idx_message_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_reference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    knowledge_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_message_knowledge (message_id, knowledge_id),
    KEY idx_reference_knowledge (knowledge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    session_id BIGINT DEFAULT NULL,
    question_message_id BIGINT DEFAULT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolution TEXT DEFAULT NULL,
    handled_by BIGINT DEFAULT NULL,
    resolved_at DATETIME DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ticket_no (ticket_no),
    KEY idx_ticket_user (user_id, created_at),
    KEY idx_ticket_status (status, created_at),
    KEY idx_ticket_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE model_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    question_message_id BIGINT DEFAULT NULL,
    answer_message_id BIGINT DEFAULT NULL,
    model_name VARCHAR(100) DEFAULT NULL,
    from_cache TINYINT NOT NULL DEFAULT 0,
    call_status VARCHAR(20) NOT NULL,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    prompt_tokens INT DEFAULT NULL,
    completion_tokens INT DEFAULT NULL,
    error_message VARCHAR(1000) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_log_user (user_id, created_at),
    KEY idx_log_session (session_id, created_at),
    KEY idx_log_status (call_status, created_at),
    KEY idx_log_cache (from_cache, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 8. 初始化数据

```sql
INSERT INTO sys_user
(username, display_name, role, status)
VALUES
('employee_demo', '演示员工', 'EMPLOYEE', 1),
('admin_demo', '演示管理员', 'ADMIN', 1);

INSERT INTO knowledge_category
(name, description, sort_order, status)
VALUES
('人事制度', '请假、报销、考勤和转正等制度', 1, 1),
('IT支持', '账号、网络、VPN和权限申请', 2, 1),
('研发规范', 'Git、Java、接口和数据库开发规范', 3, 1),
('项目流程', '需求、测试、上线和回滚流程', 4, 1);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
VALUES
(
    3,
    'Git 分支命名规范',
    'Git,分支,branch,feature,bugfix',
    '功能分支统一使用 feature/功能名称，缺陷修复分支使用 bugfix/缺陷名称。分支名称使用小写英文和短横线。',
    1,
    2,
    2
),
(
    3,
    'Git Commit 提交规范',
    'Git,commit,提交,feat,fix',
    'Git提交信息建议采用类型加简短描述的形式。常见类型包括 feat、fix、docs、refactor、test 和 chore。',
    1,
    2,
    2
),
(
    2,
    '测试服务器权限申请流程',
    '服务器,权限,测试环境,申请',
    '申请测试服务器权限时，需要提交用途、项目名称、所需权限和使用期限，由项目负责人和运维人员审核。',
    1,
    2,
    2
);
```

---

## 9. 数据一致性规则

1. 新增知识时分类必须存在且未删除。
2. 停用分类后，该分类下知识不参与 Agent 检索。
3. 用户只能查询自己的会话和工单。
4. 创建消息前，会话必须存在且属于当前用户。
5. 创建工单时，来源会话和消息必须属于当前用户。
6. Agent 消息保存成功后，才能写入知识引用关系。
7. 修改知识后必须清理问答缓存。
8. 删除会话时只逻辑删除会话，不物理删除消息。
9. 工单状态变化必须符合状态流转规则。
10. 模型日志保存失败不能导致主问答失败。

---

## 10. 事务边界

### 保存用户问题

```text
保存 chat_message(USER)
更新 chat_session.last_message_at
```

### 保存 Agent 回答

```text
保存 chat_message(ASSISTANT)
保存 knowledge_reference
更新 chat_session.last_message_at
```

### 知识修改

```text
数据库事务内修改知识
事务提交后清理 Redis
```

大模型 HTTP 调用与 Redis 操作不进入数据库长事务。
