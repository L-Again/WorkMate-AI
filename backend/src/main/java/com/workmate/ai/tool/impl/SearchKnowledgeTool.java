package com.workmate.ai.tool.impl;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.tool.AgentTool;
import com.workmate.ai.vo.KnowledgeListItemVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchKnowledgeTool implements AgentTool {

    public static final String NAME = "searchKnowledge";

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 5;

    private final KnowledgeService knowledgeService;

    public SearchKnowledgeTool(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        Long userId = getRequiredLong(parameters, "userId");
        String keyword = getRequiredString(parameters, "keyword");
        Integer limit = getLimit(parameters);

        return search(userId, keyword, limit);
    }

    public List<KnowledgeListItemVO> search(Long userId, String keyword, Integer limit) {
        return knowledgeService.searchKnowledge(userId, keyword, limit);
    }

    private Long getRequiredLong(Map<String, Object> parameters, String key) {
        if (parameters == null || !(parameters.get(key) instanceof Number value)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return value.longValue();
    }

    private String getRequiredString(Map<String, Object> parameters, String key) {
        if (parameters == null || !(parameters.get(key) instanceof String value) || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return value.trim();
    }

    private Integer getLimit(Map<String, Object> parameters) {
        if (parameters == null || parameters.get("limit") == null) {
            return DEFAULT_LIMIT;
        }

        Object value = parameters.get("limit");
        if (!(value instanceof Number number)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        int limit = number.intValue();
        if (limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}