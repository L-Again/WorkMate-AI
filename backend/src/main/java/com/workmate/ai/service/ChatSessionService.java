package com.workmate.ai.service;

import com.workmate.ai.dto.SessionCreateDTO;
import com.workmate.ai.dto.SessionTitleUpdateDTO;
import com.workmate.ai.vo.SessionVO;

public interface ChatSessionService {

    SessionVO createSession(Long userId, SessionCreateDTO request);

    SessionVO getSessionDetail(Long userId, Long sessionId);

    SessionVO updateSessionTitle(Long userId, Long sessionId, SessionTitleUpdateDTO request);

    Boolean deleteSession(Long userId, Long sessionId);
}