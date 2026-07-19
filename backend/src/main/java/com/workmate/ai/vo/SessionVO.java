package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class SessionVO {

    private Long sessionId;
    private String title;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;

    public SessionVO(Long sessionId, String title, LocalDateTime lastMessageAt, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.title = title;
        this.lastMessageAt = lastMessageAt;
        this.createdAt = createdAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}