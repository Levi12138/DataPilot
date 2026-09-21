package com.datapilot.server.controller;

import com.datapilot.pojo.dto.LoginDTO;
import com.datapilot.pojo.vo.LoginVO;
import com.datapilot.server.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final AuthServiceImpl authService;
    public LoginController(AuthServiceImpl authService){
        this.authService=authService;
    }
    @PostMapping("/auth/login")
    public LoginVO login(@RequestBody @Valid LoginDTO loginDTO){
        return authService.login(loginDTO);
    }
}
