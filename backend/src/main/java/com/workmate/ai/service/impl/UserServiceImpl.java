package com.workmate.ai.service.impl;

import com.workmate.ai.service.UserService;
import com.workmate.ai.vo.CurrentUserVO;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public CurrentUserVO getCurrentUser(Long userId) {
        if (userId == 1L) {
            return new CurrentUserVO(1L, "employee_demo", "演示员工", "EMPLOYEE", 1);
        }

        return new CurrentUserVO(2L, "admin_demo", "演示管理员", "ADMIN", 1);
    }
}