package com.workmate.ai.tool;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentToolRegistry {

    private final Map<String, AgentTool> tools;

    public AgentToolRegistry(List<AgentTool> tools) {
        Map<String, AgentTool> toolMap = new LinkedHashMap<>();
        for (AgentTool tool : tools) {
            if (toolMap.containsKey(tool.getName())) {
                throw new IllegalStateException("Duplicate agent tool name: " + tool.getName());
            }
            toolMap.put(tool.getName(), tool);
        }
        this.tools = Collections.unmodifiableMap(toolMap);
    }

    public Map<String, AgentTool> getTools() {
        return tools;
    }

    public AgentTool getTool(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        AgentTool tool = tools.get(name);
        if (tool == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        return tool;
    }
}