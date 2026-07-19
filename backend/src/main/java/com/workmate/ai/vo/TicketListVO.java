package com.workmate.ai.vo;

import java.time.LocalDateTime;

public class TicketListVO {

    private Long ticketId;
    private String ticketNo;
    private Long userId;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketListVO(Long ticketId, String ticketNo, Long userId, String title,
                        String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.ticketId = ticketId;
        this.ticketNo = ticketNo;
        this.userId = userId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getTicketId() { return ticketId; }
    public String getTicketNo() { return ticketNo; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}