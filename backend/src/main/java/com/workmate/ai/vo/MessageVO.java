package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class MessageVO {

    private Long messageId;
    private Long sessionId;
    private String role;
    private String content;
    private Integer fromCache;
    private Integer canCreateTicket;
    private LocalDateTime createdAt;

    public MessageVO(Long messageId, Long sessionId, String role, String content,
                     Integer fromCache, Integer canCreateTicket, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.fromCache = fromCache;
        this.canCreateTicket = canCreateTicket;
        this.createdAt = createdAt;
    }

    public Long getMessageId() { return messageId; }
    public Long getSessionId() { return sessionId; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public Integer getFromCache() { return fromCache; }
    public Integer getCanCreateTicket() { return canCreateTicket; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}