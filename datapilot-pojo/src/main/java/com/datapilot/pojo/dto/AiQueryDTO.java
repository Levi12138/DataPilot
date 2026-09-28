package com.datapilot.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AiQueryDTO {
    /**
     * 要查询的数据源
     */
    @NotNull(message = "数据源ID不能为空")
    @Positive(message = "数据源ID必须大于0")
    private Long datasourceId;

    /**
     * 用户自然语言问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 多轮对话ID
     */
    private Long conversationId;
}
