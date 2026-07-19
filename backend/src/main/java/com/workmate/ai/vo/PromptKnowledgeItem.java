package com.workmate.ai.vo;

public class PromptKnowledgeItem {

    private final Long knowledgeId;
    private final String title;
    private final String categoryName;
    private final String content;

    public PromptKnowledgeItem(Long knowledgeId, String title, String categoryName, String content) {
        this.knowledgeId = knowledgeId;
        this.title = title;
        this.categoryName = categoryName;
        this.content = content;
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

    public String getContent() {
        return content;
    }
}