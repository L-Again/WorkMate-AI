package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.service.UserService;
import com.workmate.ai.vo.CurrentUserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/current")
    public CommonResult<CurrentUserVO> currentUser(@RequestHeader("X-User-Id") Long userId) {
        return CommonResult.success(userService.getCurrentUser(userId));
    }
}