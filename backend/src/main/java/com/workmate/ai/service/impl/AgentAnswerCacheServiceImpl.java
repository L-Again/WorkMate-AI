package com.workmate.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workmate.ai.service.AgentAnswerCacheService;
import com.workmate.ai.vo.AgentAnswerCacheValue;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
public class AgentAnswerCacheServiceImpl implements AgentAnswerCacheService {

    private static final String CACHE_KEY_PREFIX = "agent:answer:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AgentAnswerCacheServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String normalizeQuestion(String question) {
        if (question == null) {
            return "";
        }
        return question.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    @Override
    public String buildQuestionHash(String question) {
        String normalizedQuestion = normalizeQuestion(question);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(normalizedQuestion.getBytes(StandardCharsets.UTF_8));
            return toHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    @Override
    public String buildCacheKey(String question) {
        return CACHE_KEY_PREFIX + buildQuestionHash(question);
    }

    @Override
    public Optional<AgentAnswerCacheValue> get(String question) {
        String cachedJson = redisTemplate.opsForValue().get(buildCacheKey(question));
        if (cachedJson == null || cachedJson.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(cachedJson, AgentAnswerCacheValue.class));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize agent answer cache value", exception);
        }
    }

    @Override
    public void save(String question, AgentAnswerCacheValue cacheValue) {
        if (cacheValue == null) {
            throw new IllegalArgumentException("cacheValue must not be null");
        }

        try {
            String cachedJson = objectMapper.writeValueAsString(cacheValue);
            redisTemplate.opsForValue().set(buildCacheKey(question), cachedJson, CACHE_TTL);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize agent answer cache value", exception);
        }
    }

    private String toHex(byte[] hashBytes) {
        StringBuilder builder = new StringBuilder(hashBytes.length * 2);
        for (byte hashByte : hashBytes) {
            builder.append(String.format("%02x", hashByte & 0xff));
        }
        return builder.toString();
    }
}