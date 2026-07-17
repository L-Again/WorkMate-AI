SET NAMES utf8mb4;

USE workmate_ai;

INSERT INTO sys_user
(username, display_name, role, status)
VALUES
('employee_demo', '演示员工', 'EMPLOYEE', 1),
('admin_demo', '演示管理员', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    role = VALUES(role),
    status = VALUES(status);

INSERT INTO knowledge_category
(name, description, sort_order, status)
VALUES
('人事制度', '请假、报销、考勤和转正等制度', 1, 1),
('IT支持', '账号、网络、VPN和权限申请', 2, 1),
('研发规范', 'Git、Java、接口和数据库开发规范', 3, 1),
('项目流程', '需求、测试、上线和回滚流程', 4, 1)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    sort_order = VALUES(sort_order),
    status = VALUES(status);

SET @admin_id := (
    SELECT id
    FROM sys_user
    WHERE username = 'admin_demo'
    LIMIT 1
);

SET @dev_category_id := (
    SELECT id
    FROM knowledge_category
    WHERE name = '研发规范'
      AND is_deleted = 0
    LIMIT 1
);

SET @it_category_id := (
    SELECT id
    FROM knowledge_category
    WHERE name = 'IT支持'
      AND is_deleted = 0
    LIMIT 1
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @dev_category_id,
    'Git 分支命名规范',
    'Git,分支,branch,feature,bugfix',
    '功能分支统一使用 feature/功能名称，缺陷修复分支使用 bugfix/缺陷名称。分支名称使用小写英文和短横线。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = 'Git 分支命名规范'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @dev_category_id,
    'Git Commit 提交规范',
    'Git,commit,提交,feat,fix',
    'Git提交信息建议采用类型加简短描述的形式。常见类型包括 feat、fix、docs、refactor、test 和 chore。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = 'Git Commit 提交规范'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @it_category_id,
    '测试服务器权限申请流程',
    '服务器,权限,测试环境,申请',
    '申请测试服务器权限时，需要提交用途、项目名称、所需权限和使用期限，由项目负责人和运维人员审核。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '测试服务器权限申请流程'
      AND is_deleted = 0
);
