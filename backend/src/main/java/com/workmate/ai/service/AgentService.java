package com.workmate.ai.service;

import com.workmate.ai.dto.AgentChatDTO;
import com.workmate.ai.vo.AgentAnswerVO;

public interface AgentService {

    AgentAnswerVO chat(Long userId, AgentChatDTO request);
}