package com.workmate.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class DemoValidationDTO {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}