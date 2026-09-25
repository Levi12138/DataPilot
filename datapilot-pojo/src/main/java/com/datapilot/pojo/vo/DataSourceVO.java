package com.datapilot.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DataSourceVO {
    private Long id;

    private String name;

    private String dbType;

    private String host;

    private Integer port;

    private String databaseName;

    private String username;

    private Integer status;

    private String remark;

    private Long createdBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
