package com.workmate.ai.client;

public class LlmResponse {

    private final String answer;
    private final String modelName;
    private final Integer promptTokens;
    private final Integer completionTokens;

    public LlmResponse(String answer, String modelName, Integer promptTokens, Integer completionTokens) {
        this.answer = answer;
        this.modelName = modelName;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    public String getAnswer() {
        return answer;
    }

    public String getModelName() {
        return modelName;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }
}