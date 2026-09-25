package com.datapilot.pojo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateDataSourceDTO {
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @NotBlank(message = "数据库类型不能为空")
    @Pattern(regexp = "MYSQL",message = "目前只支持MYSQL")
    private String dbType;

    @NotBlank(message = "数据库地址不能为空")
    private String host;

    @NotNull(message = "端口号不能为空")
    @Min(value = 1,message = "数据库端口不合法")
    @Max(value = 65535,message = "数据库端口不合法")
    private Integer port;

    @NotBlank(message = "数据库名称不能为空")
    private String databaseName;

    @NotBlank(message = "数据库用户名不能为空")
    private String username;

    @NotBlank(message = "数据库密码不能为空")
    private String password;

    private Integer status=1;

    private String remark;
}
