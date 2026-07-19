package com.workmate.ai.dto;

public class ModelCallLogCreateDTO {

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

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getQuestionMessageId() { return questionMessageId; }
    public void setQuestionMessageId(Long questionMessageId) { this.questionMessageId = questionMessageId; }

    public Long getAnswerMessageId() { return answerMessageId; }
    public void setAnswerMessageId(Long answerMessageId) { this.answerMessageId = answerMessageId; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Boolean getFromCache() { return fromCache; }
    public void setFromCache(Boolean fromCache) { this.fromCache = fromCache; }

    public String getCallStatus() { return callStatus; }
    public void setCallStatus(String callStatus) { this.callStatus = callStatus; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}