package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class SessionListVO {

    private Long sessionId;
    private String title;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;

    public SessionListVO(Long sessionId, String title, String lastMessage,
                         LocalDateTime lastMessageAt, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.title = title;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.createdAt = createdAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}