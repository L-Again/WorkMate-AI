package com.workmate.ai.vo;

import java.util.ArrayList;
import java.util.List;

public class AgentAnswerCacheValue {

    private String answer;
    private List<KnowledgeReferenceVO> references = new ArrayList<>();

    public AgentAnswerCacheValue() {
    }

    public AgentAnswerCacheValue(String answer, List<KnowledgeReferenceVO> references) {
        this.answer = answer;
        setReferences(references);
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<KnowledgeReferenceVO> getReferences() {
        return references;
    }

    public void setReferences(List<KnowledgeReferenceVO> references) {
        if (references == null) {
            this.references = new ArrayList<>();
            return;
        }
        this.references = new ArrayList<>(references);
    }
}