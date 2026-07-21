package com.workmate.ai.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workmate.ai.client.LlmClient;
import com.workmate.ai.client.LlmRequest;
import com.workmate.ai.client.LlmResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(prefix = "workmate.llm", name = "provider", havingValue = "deepseek")
public class DeepSeekLlmClient implements LlmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String defaultModelName;

    public DeepSeekLlmClient(RestClient.Builder restClientBuilder,
                             ObjectMapper objectMapper,
                             @Value("${workmate.llm.deepseek.api-key:}") String apiKey,
                             @Value("${workmate.llm.model:deepseek-v4-flash}") String defaultModelName,
                             @Value("${workmate.llm.deepseek.base-url:https://api.deepseek.com}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(trimOrDefault(baseUrl, "https://api.deepseek.com")).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.defaultModelName = trimOrDefault(defaultModelName, "deepseek-v4-flash");
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("DeepSeek API Key 未配置");
        }

        String modelName = resolveModelName(request);
        Map<String, Object> body = Map.of(
                "model", modelName,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", request == null ? "" : request.getPrompt()
                )),
                "temperature", 0.2,
                "stream", false
        );

        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseResponse(responseBody, modelName);
        } catch (RestClientException exception) {
            throw new RuntimeException("DeepSeek 调用失败：" + exception.getMessage(), exception);
        }
    }

    private LlmResponse parseResponse(String responseBody, String requestModelName) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String answer = root.path("choices").path(0).path("message").path("content").asText("");
            if (answer.trim().isEmpty()) {
                throw new IllegalStateException("DeepSeek 返回内容为空");
            }

            JsonNode usage = root.path("usage");
            return new LlmResponse(
                    answer,
                    root.path("model").asText(requestModelName),
                    readNullableInt(usage, "prompt_tokens"),
                    readNullableInt(usage, "completion_tokens")
            );
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RuntimeException("DeepSeek 响应解析失败：" + exception.getMessage(), exception);
        }
    }

    private Integer readNullableInt(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        return node.get(fieldName).asInt();
    }

    private String resolveModelName(LlmRequest request) {
        if (request == null || request.getModelName() == null || request.getModelName().trim().isEmpty()) {
            return defaultModelName;
        }
        return request.getModelName().trim();
    }

    private static String trimOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
