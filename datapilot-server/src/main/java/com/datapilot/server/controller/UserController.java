package com.datapilot.server.controller;

import com.datapilot.common.result.Result;
import com.datapilot.pojo.entity.SysUser;
import com.datapilot.pojo.vo.UserVO;
import com.datapilot.server.mapper.SysUserMapper;
import com.datapilot.server.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final SysUserMapper sysUserMapper;
    public UserController(SysUserMapper sysUserMapper){
        this.sysUserMapper=sysUserMapper;
    }

    @GetMapping("/me")
    public Result<UserVO> me(){
        UserVO userVO=new UserVO();

        Long userId= SecurityUtils.getCurrentId();
        SysUser sysUser=sysUserMapper.selectById(userId);

        userVO.setId(userId);
        userVO.setUsername(sysUser.getUsername());
        userVO.setNickname(sysUser.getNickname());

        return Result.success(userVO);
    }
}

