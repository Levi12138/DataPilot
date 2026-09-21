package com.datapilot.server.service.impl;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.pojo.dto.LoginDTO;
import com.datapilot.pojo.entity.SysUser;
import com.datapilot.pojo.vo.LoginVO;
import com.datapilot.pojo.vo.UserVO;
import com.datapilot.server.mapper.SysUserMapper;
import com.datapilot.server.security.JwtTokenProvider;
import com.datapilot.server.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(SysUserMapper sysUserMapper,PasswordEncoder passwordEncoder,JwtTokenProvider jwtTokenProvider){
        this.passwordEncoder=passwordEncoder;
        this.sysUserMapper=sysUserMapper;
        this.jwtTokenProvider=jwtTokenProvider;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO){

        SysUser sysUser=sysUserMapper.selectByUsername(loginDTO.getUsername());

        if(sysUser==null){
           throw new BusinessException("用户名或密码错误");
        }
        if(sysUser.getStatus()==0){
            throw new BusinessException("用户不可用，请使用其他账户登录");
        }
        if(!passwordEncoder.matches(loginDTO.getPassword(),sysUser.getPasswordHash())){
            throw new BusinessException("用户名或密码错误");
        }
        String token= jwtTokenProvider.generateToken(sysUser.getId(),sysUser.getUsername());

        UserVO userVO=new UserVO();

        userVO.setId(sysUser.getId());
        userVO.setUsername(sysUser.getUsername());
        userVO.setNickname(sysUser.getNickname());

        LoginVO loginVO=new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);

        return loginVO;


    }
}
