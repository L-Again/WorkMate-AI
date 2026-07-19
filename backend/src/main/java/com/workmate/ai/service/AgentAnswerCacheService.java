package com.workmate.ai.service;

import com.workmate.ai.vo.AgentAnswerCacheValue;

import java.util.Optional;

public interface AgentAnswerCacheService {

    String normalizeQuestion(String question);

    String buildQuestionHash(String question);

    String buildCacheKey(String question);

    Optional<AgentAnswerCacheValue> get(String question);

    void save(String question, AgentAnswerCacheValue cacheValue);
}