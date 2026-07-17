package com.workmate.ai.service.impl;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.UserService;
import com.workmate.ai.vo.CurrentUserVO;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final int ENABLED_STATUS = 1;

    private final SysUserMapper sysUserMapper;

    public UserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public CurrentUserVO getCurrentUser(Long userId) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null || !Integer.valueOf(ENABLED_STATUS).equals(sysUser.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        return new CurrentUserVO(
                sysUser.getId(),
                sysUser.getUsername(),
                sysUser.getDisplayName(),
                sysUser.getRole(),
                sysUser.getStatus()
        );
    }
}
