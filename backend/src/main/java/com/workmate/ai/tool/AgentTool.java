package com.workmate.ai.tool;

import java.util.Map;

public interface AgentTool {

    String getName();

    Object execute(Map<String, Object> parameters);
}