package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class KnowledgeDetailVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String keywords;
    private String content;
    private Integer status;
    private LocalDateTime updatedAt;

    public KnowledgeDetailVO(Long id, Long categoryId, String categoryName, String title,
                             String keywords, String content, Integer status, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.title = title;
        this.keywords = keywords;
        this.content = content;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getTitle() { return title; }
    public String getKeywords() { return keywords; }
    public String getContent() { return content; }
    public Integer getStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}