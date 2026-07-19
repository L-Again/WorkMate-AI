package com.workmate.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workmate.ai.service.AgentAnswerCacheService;
import com.workmate.ai.vo.AgentAnswerCacheValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class AgentAnswerCacheServiceImpl implements AgentAnswerCacheService {

    private static final Logger log = LoggerFactory.getLogger(AgentAnswerCacheServiceImpl.class);
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
        try {
            String cachedJson = redisTemplate.opsForValue().get(buildCacheKey(question));
            if (cachedJson == null || cachedJson.trim().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cachedJson, AgentAnswerCacheValue.class));
        } catch (JsonProcessingException | RuntimeException exception) {
            log.warn("Failed to read agent answer cache, skip cache lookup.", exception);
            return Optional.empty();
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
        } catch (JsonProcessingException | RuntimeException exception) {
            log.warn("Failed to write agent answer cache, skip cache write.", exception);
        }
    }

    @Override
    public void evictAllAnswers() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            redisTemplate.delete(keys);
        } catch (RuntimeException exception) {
            log.warn("Failed to evict agent answer cache, skip cache eviction.", exception);
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