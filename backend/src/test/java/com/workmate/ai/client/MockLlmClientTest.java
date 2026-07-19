package com.workmate.ai.client;

import com.workmate.ai.client.impl.MockLlmClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockLlmClientTest {

    private final LlmClient llmClient = new MockLlmClient();

    @Test
    void shouldReturnMockAnswer() {
        LlmResponse response = llmClient.chat(new LlmRequest("mock-llm", "测试 Prompt"));

        assertThat(response.getAnswer()).isEqualTo("这是 Mock LLM 根据企业知识生成的测试回答。");
        assertThat(response.getModelName()).isEqualTo("mock-llm");
        assertThat(response.getPromptTokens()).isNull();
        assertThat(response.getCompletionTokens()).isNull();
    }

    @Test
    void shouldUseDefaultModelNameWhenRequestModelNameIsBlank() {
        LlmResponse response = llmClient.chat(new LlmRequest(" ", "测试 Prompt"));

        assertThat(response.getModelName()).isEqualTo("mock-llm");
    }
}