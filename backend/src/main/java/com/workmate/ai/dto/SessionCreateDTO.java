package com.workmate.ai.dto;

import jakarta.validation.constraints.Size;

public class SessionCreateDTO {

    @Size(max = 200)
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}