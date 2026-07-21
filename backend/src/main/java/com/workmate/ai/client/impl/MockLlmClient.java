package com.workmate.ai.client.impl;

import com.workmate.ai.client.LlmClient;
import com.workmate.ai.client.LlmRequest;
import com.workmate.ai.client.LlmResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "workmate.llm", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmClient implements LlmClient {

    private static final String DEFAULT_MODEL_NAME = "mock-llm";

    @Override
    public LlmResponse chat(LlmRequest request) {
        return new LlmResponse(
                "这是 Mock LLM 根据企业知识生成的测试回答。",
                resolveModelName(request),
                null,
                null
        );
    }

    private String resolveModelName(LlmRequest request) {
        if (request == null || request.getModelName() == null || request.getModelName().trim().isEmpty()) {
            return DEFAULT_MODEL_NAME;
        }
        return request.getModelName().trim();
    }
}
