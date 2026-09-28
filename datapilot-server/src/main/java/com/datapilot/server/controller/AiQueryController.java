package com.datapilot.server.controller;

import com.datapilot.common.result.Result;
import com.datapilot.pojo.dto.AiQueryDTO;
import com.datapilot.pojo.vo.AiQueryVO;
import com.datapilot.server.ai.query.impl.AiQueryServiceImpl;
import com.datapilot.server.ai.service.impl.AiModelServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
public class AiQueryController {
    private final AiQueryServiceImpl aiQueryService;

    @PostMapping
    public Result<AiQueryVO> aiQuery(@Valid @RequestBody AiQueryDTO aiQueryDTO){
        System.out.println(aiQueryDTO);
        AiQueryVO aiQueryVO=aiQueryService.execute(aiQueryDTO);
        return Result.success(aiQueryVO);
    }
}
