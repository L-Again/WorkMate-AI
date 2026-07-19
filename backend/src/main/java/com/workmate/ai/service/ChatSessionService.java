package com.workmate.ai.service;

import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.SessionCreateDTO;
import com.workmate.ai.dto.SessionTitleUpdateDTO;
import com.workmate.ai.vo.MessageVO;
import com.workmate.ai.vo.SessionListVO;
import com.workmate.ai.vo.SessionVO;

public interface ChatSessionService {

    SessionVO createSession(Long userId, SessionCreateDTO request);

    PageResult<SessionListVO> listSessions(Long userId, Long pageNum, Long pageSize);

    PageResult<MessageVO> listMessages(Long userId, Long sessionId, Long pageNum, Long pageSize);

    SessionVO getSessionDetail(Long userId, Long sessionId);

    SessionVO updateSessionTitle(Long userId, Long sessionId, SessionTitleUpdateDTO request);

    Boolean deleteSession(Long userId, Long sessionId);
}