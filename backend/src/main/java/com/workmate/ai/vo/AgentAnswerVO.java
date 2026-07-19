package com.workmate.ai.vo;

import java.util.List;

public class AgentAnswerVO {

    private Long sessionId;
    private Long questionMessageId;
    private Long answerMessageId;
    private String answer;
    private Boolean fromCache;
    private Boolean canCreateTicket;
    private List<KnowledgeReferenceVO> references;
    private List<AgentTraceStepVO> traceSteps;

    public AgentAnswerVO(Long sessionId,
                         Long questionMessageId,
                         Long answerMessageId,
                         String answer,
                         Boolean fromCache,
                         Boolean canCreateTicket,
                         List<KnowledgeReferenceVO> references,
                         List<AgentTraceStepVO> traceSteps) {
        this.sessionId = sessionId;
        this.questionMessageId = questionMessageId;
        this.answerMessageId = answerMessageId;
        this.answer = answer;
        this.fromCache = fromCache;
        this.canCreateTicket = canCreateTicket;
        this.references = references;
        this.traceSteps = traceSteps;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getQuestionMessageId() {
        return questionMessageId;
    }

    public Long getAnswerMessageId() {
        return answerMessageId;
    }

    public String getAnswer() {
        return answer;
    }

    public Boolean getFromCache() {
        return fromCache;
    }

    public Boolean getCanCreateTicket() {
        return canCreateTicket;
    }

    public List<KnowledgeReferenceVO> getReferences() {
        return references;
    }

    public List<AgentTraceStepVO> getTraceSteps() {
        return traceSteps;
    }
}