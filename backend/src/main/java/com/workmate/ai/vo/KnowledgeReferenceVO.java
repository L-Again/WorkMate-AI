package com.workmate.ai.vo;

public class KnowledgeReferenceVO {

    private Long knowledgeId;
    private String title;
    private String categoryName;

    public KnowledgeReferenceVO(Long knowledgeId, String title, String categoryName) {
        this.knowledgeId = knowledgeId;
        this.title = title;
        this.categoryName = categoryName;
    }

    public Long getKnowledgeId() {
        return knowledgeId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategoryName() {
        return categoryName;
    }
}