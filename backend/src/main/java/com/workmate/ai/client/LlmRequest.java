package com.workmate.ai.client;

public class LlmRequest {

    private final String modelName;
    private final String prompt;

    public LlmRequest(String modelName, String prompt) {
        this.modelName = modelName;
        this.prompt = prompt;
    }

    public String getModelName() {
        return modelName;
    }

    public String getPrompt() {
        return prompt;
    }
}