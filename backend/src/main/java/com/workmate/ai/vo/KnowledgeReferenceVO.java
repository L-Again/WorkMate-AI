package com.workmate.ai.vo;

public class KnowledgeReferenceVO {

    private Long knowledgeId;
    private String title;
    private String categoryName;

    public KnowledgeReferenceVO() {
    }

    public KnowledgeReferenceVO(Long knowledgeId, String title, String categoryName) {
        this.knowledgeId = knowledgeId;
        this.title = title;
        this.categoryName = categoryName;
    }

    public Long getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(Long knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
