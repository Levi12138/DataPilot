package com.datapilot.pojo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateDataSourceDTO {

    private String name;


    @Pattern(regexp = "MYSQL", message = "当前只支持MYSQL")
    private String dbType;


    private String host;


    @Min(value = 1, message = "数据库端口不合法")
    @Max(value = 65535, message = "数据库端口不合法")
    private Integer port;

    private String databaseName;

    private String username;

    /**
     * 修改时可以不传密码。
     * 不传代表继续使用原密码。
     */
    private String password;

    @Min(value = 0, message = "数据源状态只能为0或1")
    @Max(value = 1, message = "数据源状态只能为0或1")
    private Integer status;

    private String remark;
}
