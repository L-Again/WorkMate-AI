package com.workmate.ai.vo;

public class AgentTraceStepVO {

    private String step;
    private String description;
    private Boolean success;
    private String detail;

    public AgentTraceStepVO(String step, String description, Boolean success, String detail) {
        this.step = step;
        this.description = description;
        this.success = success;
        this.detail = detail;
    }

    public String getStep() {
        return step;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getDetail() {
        return detail;
    }
}