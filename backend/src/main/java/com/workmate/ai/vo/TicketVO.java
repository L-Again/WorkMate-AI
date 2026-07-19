package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class TicketVO {

    private Long ticketId;
    private String ticketNo;
    private Long userId;
    private Long sessionId;
    private Long questionMessageId;
    private String title;
    private String description;
    private String status;
    private String resolution;
    private Long handledBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketVO(Long ticketId, String ticketNo, Long userId, Long sessionId,
                    Long questionMessageId, String title, String description,
                    String status, String resolution, Long handledBy,
                    LocalDateTime resolvedAt, LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.ticketId = ticketId;
        this.ticketNo = ticketNo;
        this.userId = userId;
        this.sessionId = sessionId;
        this.questionMessageId = questionMessageId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.resolution = resolution;
        this.handledBy = handledBy;
        this.resolvedAt = resolvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getTicketId() { return ticketId; }
    public String getTicketNo() { return ticketNo; }
    public Long getUserId() { return userId; }
    public Long getSessionId() { return sessionId; }
    public Long getQuestionMessageId() { return questionMessageId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getResolution() { return resolution; }
    public Long getHandledBy() { return handledBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}