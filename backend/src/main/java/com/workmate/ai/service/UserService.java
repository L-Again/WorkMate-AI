package com.workmate.ai.service;

import com.workmate.ai.vo.CurrentUserVO;

public interface UserService {

    CurrentUserVO getCurrentUser(Long userId);
}