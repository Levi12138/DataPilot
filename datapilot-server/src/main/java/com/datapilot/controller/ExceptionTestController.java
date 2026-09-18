package com.datapilot.controller;

import com.datapilot.dto.TestPramaDTO;
import com.datapilot.result.Result;
import com.datapilot.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ExceptionTestController {
    @GetMapping("/business-error")
    public Result<Void> bExceptionTest(){
        throw new BusinessException("测试业务异常");
    }

    @GetMapping("/system-error")
    public Result<Void> sExceptionTest(){
        throw new RuntimeException("测试系统异常");
    }

    @PostMapping("/validate")
    public Result<String> validate(@RequestBody @Valid TestPramaDTO testPramaDTO){
        return Result.success("参数正确");
    }
}
