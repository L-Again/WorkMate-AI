package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class KnowledgeListItemVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String keywords;
    private Integer status;
    private LocalDateTime updatedAt;

    public KnowledgeListItemVO(Long id, Long categoryId, String categoryName, String title,
                               String keywords, Integer status, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.title = title;
        this.keywords = keywords;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getTitle() { return title; }
    public String getKeywords() { return keywords; }
    public Integer getStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}