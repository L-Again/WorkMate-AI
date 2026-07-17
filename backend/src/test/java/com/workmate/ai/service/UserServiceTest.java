package com.workmate.ai.service;

import com.workmate.ai.service.impl.UserServiceImpl;
import com.workmate.ai.vo.CurrentUserVO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

    private final UserService userService = new UserServiceImpl();

    @Test
    void shouldReturnEmployeeWhenUserIdIsOne() {
        CurrentUserVO currentUser = userService.getCurrentUser(1L);

        assertThat(currentUser.getId()).isEqualTo(1L);
        assertThat(currentUser.getUsername()).isEqualTo("employee_demo");
        assertThat(currentUser.getDisplayName()).isEqualTo("演示员工");
        assertThat(currentUser.getRole()).isEqualTo("EMPLOYEE");
        assertThat(currentUser.getStatus()).isEqualTo(1);
    }

    @Test
    void shouldReturnAdminWhenUserIdIsTwo() {
        CurrentUserVO currentUser = userService.getCurrentUser(2L);

        assertThat(currentUser.getId()).isEqualTo(2L);
        assertThat(currentUser.getUsername()).isEqualTo("admin_demo");
        assertThat(currentUser.getDisplayName()).isEqualTo("演示管理员");
        assertThat(currentUser.getRole()).isEqualTo("ADMIN");
        assertThat(currentUser.getStatus()).isEqualTo(1);
    }
}