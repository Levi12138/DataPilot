package com.datapilot.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TableMetadataVO {
    private Long id;

    private Long dataSourceId;

    private String schemaName;

    private String tableName;

    private String tableComment;

    private LocalDateTime lastSyncTime;
}
