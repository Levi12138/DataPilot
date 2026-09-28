package com.datapilot.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueryHistory {
    private Long id;

    private Long userId;

    private Long datasourceId;

    private String question;

    private String generatedSql;

    private String status;

    private String errorMessage;

    private Integer rowCount;

    private Long elapsedMs;

    private String modelName;

    private LocalDateTime createTime;
}
