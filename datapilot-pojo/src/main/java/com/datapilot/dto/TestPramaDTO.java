package com.datapilot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestPramaDTO {

    @NotBlank(message = "name不能为空")
    private String name;

    @NotNull(message = "age不能为空")
    @Min(value = 1,message = "age必须大于等于1")
    private Integer age;
}
