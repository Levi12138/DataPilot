package com.datapilot.server.service;

import com.datapilot.pojo.dto.LoginDTO;
import com.datapilot.pojo.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO loginDTO);
}
