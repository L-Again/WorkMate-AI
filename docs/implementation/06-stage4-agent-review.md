# Stage 4 Agent 问答阶段复盘

> 范围：Stage 4 大模型问答文档同步与验收收口。  
> 状态：Stage 4 MVP 范围已完成。  
> 下一阶段：Stage 5 Redis 问答缓存。

## 1. 阶段范围

Stage 4 的目标是实现第一版知识库问答主链路，但不接入 Redis 缓存优化、不实现工单、不实现异步模型日志。

本阶段已完成：

- `LlmClient` 调用抽象。
- `MockLlmClient` 本地模拟实现。
- `PromptBuilder` 提示词构造。
- `POST /api/agent/chat` 问答接口。
- 用户问题保存。
- 企业知识检索。
- 有知识时调用 LLM。
- Agent 回答保存。
- 知识引用关系保存。
- 无知识兜底回答。
- 模型失败兜底错误。
- 安全的 `traceSteps` 执行摘要。

本阶段不包含：

- Redis 缓存读写。
- 缓存命中流程。
- 知识变更后的缓存清理。
- 工单创建接口。
- `AgentTool` 工具注册。
- `model_call_log` 异步日志写入。
- 真实大模型 API 调用。

这些内容分别进入 Stage 5 和 Stage 6。

## 2. 已完成任务

### S4-T01：新增 LLM 调用抽象和 Prompt 构造器

提交记录：

```text
410d33c feat(agent): add llm client and prompt builder