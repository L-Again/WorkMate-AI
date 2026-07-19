package com.workmate.ai.client;

public interface LlmClient {

    LlmResponse chat(LlmRequest request);
}