package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.vo.CurrentUserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/users/current")
    public CommonResult<CurrentUserVO> currentUser(@RequestHeader("X-User-Id") Long userId) {
        if (userId == 1L) {
            return CommonResult.success(
                    new CurrentUserVO(1L, "employee_demo", "演示员工", "EMPLOYEE", 1)
            );
        }

        return CommonResult.success(
                new CurrentUserVO(2L, "admin_demo", "演示管理员", "ADMIN", 1)
        );
    }
}