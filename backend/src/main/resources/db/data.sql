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

SET @hr_category_id := (
    SELECT id
    FROM knowledge_category
    WHERE name = '人事制度'
      AND is_deleted = 0
    LIMIT 1
);

SET @project_category_id := (
    SELECT id
    FROM knowledge_category
    WHERE name = '项目流程'
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

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @hr_category_id,
    '年假申请规则',
    '年假,请假,休假,审批,人事',
    '员工申请年假需要至少提前 3 个工作日在 OA 系统提交申请，选择请假类型为年假，填写请假时间和交接事项。连续请假超过 3 天时，需要直属负责人和部门负责人共同审批。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '年假申请规则'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @hr_category_id,
    '病假材料提交要求',
    '病假,请假,证明,医院,人事',
    '员工申请病假时，应在返岗后 2 个工作日内补充医院诊断证明或挂号记录。单次病假超过 2 天时，需要上传医院出具的病假建议证明，直属负责人确认后由人事归档。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '病假材料提交要求'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @hr_category_id,
    '考勤异常处理流程',
    '考勤,打卡,补卡,迟到,人事',
    '忘记打卡或外出办公导致考勤异常时，员工需要在当月考勤结算前提交补卡申请，并说明异常原因。每月补卡次数原则上不超过 3 次，超过次数需要部门负责人额外确认。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '考勤异常处理流程'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @hr_category_id,
    '差旅报销材料要求',
    '报销,差旅,发票,交通,住宿',
    '差旅报销需要提交审批通过的出差申请、交通票据、住宿发票和行程说明。电子发票需上传原始 PDF 文件，纸质票据需在报销单提交后 5 个工作日内交给财务。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '差旅报销材料要求'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @hr_category_id,
    '试用期转正流程',
    '试用期,转正,绩效,人事,审批',
    '员工试用期结束前 10 个工作日，人事会发起转正评估。员工需要提交试用期工作总结，直属负责人填写评价意见，通过后由人事更新员工状态并同步薪酬信息。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '试用期转正流程'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @it_category_id,
    'VPN 账号申请流程',
    'VPN,远程办公,账号,权限,IT',
    '员工需要远程访问公司内部系统时，应在 IT 服务台提交 VPN 账号申请，填写访问系统、使用期限和业务原因。申请通过后，IT 会开通账号并发送首次登录说明。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = 'VPN 账号申请流程'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @it_category_id,
    '企业邮箱密码重置流程',
    '邮箱,密码,重置,账号,IT',
    '企业邮箱无法登录时，员工可以通过 IT 服务台提交密码重置工单。IT 核验员工身份后会重置临时密码，员工首次登录后必须立即修改为个人强密码。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '企业邮箱密码重置流程'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @it_category_id,
    '办公网络故障处理',
    '网络,WiFi,办公网,故障,IT',
    '办公网络无法连接时，员工应先确认是否为单人故障，并尝试重新连接公司 WiFi。若仍无法恢复，需要向 IT 提供办公地点、设备编号、网络名称和故障截图。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '办公网络故障处理'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @it_category_id,
    '代码仓库权限申请',
    'GitLab,代码仓库,权限,项目,IT',
    '申请代码仓库权限时，需要在 IT 服务台填写项目名称、仓库地址、所需权限级别和有效期。项目负责人审批通过后，IT 或仓库管理员会按最小权限原则开通。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '代码仓库权限申请'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @it_category_id,
    '测试数据库账号申请',
    '数据库,测试环境,账号,权限,MySQL',
    '测试数据库账号申请需要说明项目、库名、访问原因和权限范围。默认只开放只读权限，如需写入权限，必须由项目负责人审批并注明使用期限。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '测试数据库账号申请'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @dev_category_id,
    '代码评审基本要求',
    '代码评审,Code Review,MR,PR,研发规范',
    '所有功能分支合并到主干前必须提交代码评审。评审内容包括业务逻辑、异常处理、测试覆盖、接口兼容性和安全风险。至少一名同组开发通过后才能合并。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '代码评审基本要求'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @dev_category_id,
    'REST API 命名规范',
    'API,REST,接口,命名,研发规范',
    'REST API 统一使用名词复数表示资源，路径以 /api 开头。查询使用 GET，新增使用 POST，整体更新使用 PUT，局部状态变更使用 PATCH，删除使用 DELETE。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = 'REST API 命名规范'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @dev_category_id,
    '数据库变更提交规范',
    '数据库,DDL,SQL,迁移,研发规范',
    '涉及数据库表结构变更时，开发需要提交 SQL 脚本、回滚方案和影响说明。新增字段必须明确是否可空、默认值和索引需求，禁止直接修改生产库结构。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '数据库变更提交规范'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @dev_category_id,
    '单元测试覆盖要求',
    '单元测试,测试覆盖,JUnit,Mockito,研发规范',
    '核心 Service、工具类和异常分支应补充单元测试。新增业务逻辑至少覆盖正常路径、参数错误和权限失败路径，提交代码前需要执行后端测试或前端构建验证。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '单元测试覆盖要求'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @dev_category_id,
    '生产日志记录规范',
    '日志,异常,TraceId,排查,研发规范',
    '生产日志应记录关键业务节点、请求标识和异常摘要，禁止输出密码、Token、API Key 等敏感信息。异常日志需要保留可排查的信息，但不得直接暴露给前端用户。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '生产日志记录规范'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @project_category_id,
    '需求评审流程',
    '需求,评审,产品,研发,项目流程',
    '需求进入开发前必须完成需求评审。产品经理说明背景、目标、范围和验收标准，研发评估技术方案和风险，测试确认测试范围。评审结论需要记录在需求文档中。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '需求评审流程'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @project_category_id,
    '提测准入标准',
    '提测,测试,准入,质量,项目流程',
    '功能提测前需要完成自测、单元测试、接口联调和测试说明。提测单应包含需求链接、影响范围、数据库变更、配置变更和已知风险。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '提测准入标准'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @project_category_id,
    '上线审批流程',
    '上线,发布,审批,生产,项目流程',
    '生产上线需要提交上线申请，包含版本内容、影响范围、验证步骤、回滚方案和上线窗口。项目负责人、测试负责人和运维确认后方可发布。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '上线审批流程'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @project_category_id,
    '生产回滚流程',
    '回滚,上线,生产故障,版本,项目流程',
    '上线后发现严重问题时，值班负责人可以发起回滚。回滚前需要确认影响范围、当前版本、目标版本和数据兼容性，回滚完成后必须执行核心链路验证并记录原因。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '生产回滚流程'
      AND is_deleted = 0
);

INSERT INTO knowledge
(category_id, title, keywords, content, status, created_by, updated_by)
SELECT
    @project_category_id,
    '线上故障响应机制',
    '线上故障,响应,SLA,值班,项目流程',
    '线上故障按影响范围分级处理。P1 故障需要立即拉起应急群，研发、测试、运维和产品同步响应；问题恢复后 2 个工作日内完成复盘，输出根因和改进措施。',
    1,
    @admin_id,
    @admin_id
WHERE NOT EXISTS (
    SELECT 1
    FROM knowledge
    WHERE title = '线上故障响应机制'
      AND is_deleted = 0
);
