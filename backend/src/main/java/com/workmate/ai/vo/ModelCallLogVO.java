package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class ModelCallLogVO {

    private Long id;
    private Long userId;
    private Long sessionId;
    private Long questionMessageId;
    private Long answerMessageId;
    private String modelName;
    private Boolean fromCache;
    private String callStatus;
    private Long durationMs;
    private Integer promptTokens;
    private Integer completionTokens;
    private String errorMessage;
    private LocalDateTime createdAt;

    public ModelCallLogVO(Long id, Long userId, Long sessionId, Long questionMessageId,
                          Long answerMessageId, String modelName, Boolean fromCache,
                          String callStatus, Long durationMs, Integer promptTokens,
                          Integer completionTokens, String errorMessage, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.questionMessageId = questionMessageId;
        this.answerMessageId = answerMessageId;
        this.modelName = modelName;
        this.fromCache = fromCache;
        this.callStatus = callStatus;
        this.durationMs = durationMs;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getSessionId() { return sessionId; }
    public Long getQuestionMessageId() { return questionMessageId; }
    public Long getAnswerMessageId() { return answerMessageId; }
    public String getModelName() { return modelName; }
    public Boolean getFromCache() { return fromCache; }
    public String getCallStatus() { return callStatus; }
    public Long getDurationMs() { return durationMs; }
    public Integer getPromptTokens() { return promptTokens; }
    public Integer getCompletionTokens() { return completionTokens; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}