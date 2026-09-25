package com.datapilot.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MetadataTable {
    private Long id;

    private Long datasourceId;

    private String schemaName;

    private String tableName;

    private String tableComment;

    private LocalDateTime lastSyncTime;

}
