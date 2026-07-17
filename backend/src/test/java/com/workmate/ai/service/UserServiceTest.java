package com.workmate.ai.service;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.impl.UserServiceImpl;
import com.workmate.ai.vo.CurrentUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(sysUserMapper);
    }

    @Test
    void shouldReturnCurrentUserFromDatabaseWhenUserIsEnabled() {
        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("employee_demo");
        sysUser.setDisplayName("演示员工");
        sysUser.setRole("EMPLOYEE");
        sysUser.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(sysUser);

        CurrentUserVO currentUser = userService.getCurrentUser(1L);

        assertThat(currentUser.getId()).isEqualTo(1L);
        assertThat(currentUser.getUsername()).isEqualTo("employee_demo");
        assertThat(currentUser.getDisplayName()).isEqualTo("演示员工");
        assertThat(currentUser.getRole()).isEqualTo("EMPLOYEE");
        assertThat(currentUser.getStatus()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userService.getCurrentUser(999L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND_OR_DISABLED));
    }

    @Test
    void shouldThrowWhenUserIsDisabled() {
        SysUser sysUser = new SysUser();
        sysUser.setId(3L);
        sysUser.setUsername("disabled_demo");
        sysUser.setDisplayName("停用用户");
        sysUser.setRole("EMPLOYEE");
        sysUser.setStatus(0);
        when(sysUserMapper.selectById(3L)).thenReturn(sysUser);

        assertThatThrownBy(() -> userService.getCurrentUser(3L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND_OR_DISABLED));
    }
}
