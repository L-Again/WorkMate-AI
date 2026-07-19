package com.workmate.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workmate.ai.service.impl.AgentAnswerCacheServiceImpl;
import com.workmate.ai.vo.AgentAnswerCacheValue;
import com.workmate.ai.vo.KnowledgeReferenceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentAnswerCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AgentAnswerCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new AgentAnswerCacheServiceImpl(redisTemplate, new ObjectMapper());
    }

    @Test
    void shouldNormalizeQuestion() {
        String normalized = cacheService.normalizeQuestion("  Git   Commit\t规范  ");

        assertThat(normalized).isEqualTo("git commit 规范");
    }

    @Test
    void shouldBuildSameCacheKeyForEquivalentQuestions() {
        String firstKey = cacheService.buildCacheKey("  Git   Commit 规范 ");
        String secondKey = cacheService.buildCacheKey("git commit 规范");

        assertThat(firstKey).isEqualTo(secondKey);
        assertThat(firstKey).startsWith("agent:answer:");
        assertThat(firstKey.substring("agent:answer:".length())).hasSize(64);
    }

    @Test
    void shouldSaveAnswerAndReferencesOnlyWithThirtyMinuteTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AgentAnswerCacheValue cacheValue = new AgentAnswerCacheValue(
                "缓存回答",
                List.of(new KnowledgeReferenceVO(
                        3L,
                        "Git 分支命名规范",
                        "研发规范"
                ))
        );

        cacheService.save("Git 分支应该怎么命名？", cacheValue);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(
                eq(cacheService.buildCacheKey("Git 分支应该怎么命名？")),
                jsonCaptor.capture(),
                eq(Duration.ofMinutes(30))
        );

        String cachedJson = jsonCaptor.getValue();

        assertThat(cachedJson).contains("缓存回答");
        assertThat(cachedJson).contains("references");
        assertThat(cachedJson).contains("knowledgeId");
        assertThat(cachedJson).contains("Git 分支命名规范");
        assertThat(cachedJson).doesNotContain("sessionId");
        assertThat(cachedJson).doesNotContain("questionMessageId");
        assertThat(cachedJson).doesNotContain("answerMessageId");
        assertThat(cachedJson).doesNotContain("userId");
        assertThat(cachedJson).doesNotContain("traceSteps");
        assertThat(cachedJson).doesNotContain("fromCache");
    }

    @Test
    void shouldReadCachedAnswerValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String question = " Git   分支 ";
        String cacheKey = cacheService.buildCacheKey(question);
        String cachedJson = """
                {
                  "answer": "缓存回答",
                  "references": [
                    {
                      "knowledgeId": 3,
                      "title": "Git 分支命名规范",
                      "categoryName": "研发规范"
                    }
                  ]
                }
                """;

        when(valueOperations.get(cacheKey)).thenReturn(cachedJson);

        Optional<AgentAnswerCacheValue> result = cacheService.get(question);

        assertThat(result).isPresent();
        assertThat(result.get().getAnswer()).isEqualTo("缓存回答");
        assertThat(result.get().getReferences()).hasSize(1);
        assertThat(result.get().getReferences().get(0).getKnowledgeId()).isEqualTo(3L);
        assertThat(result.get().getReferences().get(0).getTitle()).isEqualTo("Git 分支命名规范");
        assertThat(result.get().getReferences().get(0).getCategoryName()).isEqualTo("研发规范");
    }

    @Test
    void shouldReturnEmptyWhenCacheMisses() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String question = "未知问题";
        when(valueOperations.get(cacheService.buildCacheKey(question))).thenReturn(null);

        Optional<AgentAnswerCacheValue> result = cacheService.get(question);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldEvictAllAgentAnswerCacheKeys() {
        when(redisTemplate.keys("agent:answer:*"))
                .thenReturn(Set.of("agent:answer:first", "agent:answer:second"));

        cacheService.evictAllAnswers();

        verify(redisTemplate).delete(Set.of("agent:answer:first", "agent:answer:second"));
    }

    @Test
    void shouldSkipEvictWhenNoCacheKeyExists() {
        when(redisTemplate.keys("agent:answer:*")).thenReturn(Set.of());

        cacheService.evictAllAnswers();

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void shouldNotThrowWhenRedisFails() {
        doThrow(new RuntimeException("Redis unavailable"))
                .when(redisTemplate).keys("agent:answer:*");

        cacheService.evictAllAnswers();
    }

}
