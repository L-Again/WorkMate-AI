package com.workmate.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TicketCreateDTO {

    @NotNull
    private Long sessionId;

    @NotNull
    private Long questionMessageId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String description;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getQuestionMessageId() { return questionMessageId; }
    public void setQuestionMessageId(Long questionMessageId) { this.questionMessageId = questionMessageId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}