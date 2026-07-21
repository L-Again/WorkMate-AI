package com.workmate.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workmate.ai.client.impl.DeepSeekLlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class DeepSeekLlmClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCallDeepSeekChatCompletionsAndParseResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DeepSeekLlmClient client = new DeepSeekLlmClient(
                builder,
                objectMapper,
                "test-api-key",
                "deepseek-v4-flash",
                "https://api.deepseek.com"
        );

        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
                .andExpect(jsonPath("$.model").value("deepseek-v4-flash"))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("测试 Prompt"))
                .andRespond(withSuccess("""
                        {
                          "model": "deepseek-v4-flash",
                          "choices": [
                            {
                              "message": {
                                "content": "这是 DeepSeek 生成的回答。"
                              }
                            }
                          ],
                          "usage": {
                            "prompt_tokens": 12,
                            "completion_tokens": 8
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        LlmResponse response = client.chat(new LlmRequest("deepseek-v4-flash", "测试 Prompt"));

        assertThat(response.getAnswer()).isEqualTo("这是 DeepSeek 生成的回答。");
        assertThat(response.getModelName()).isEqualTo("deepseek-v4-flash");
        assertThat(response.getPromptTokens()).isEqualTo(12);
        assertThat(response.getCompletionTokens()).isEqualTo(8);
        server.verify();
    }

    @Test
    void shouldRejectMissingApiKeyBeforeCallingDeepSeek() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DeepSeekLlmClient client = new DeepSeekLlmClient(
                builder,
                objectMapper,
                " ",
                "deepseek-v4-flash",
                "https://api.deepseek.com"
        );

        assertThatThrownBy(() -> client.chat(new LlmRequest("deepseek-v4-flash", "测试 Prompt")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DeepSeek API Key 未配置");

        server.verify();
    }
}
