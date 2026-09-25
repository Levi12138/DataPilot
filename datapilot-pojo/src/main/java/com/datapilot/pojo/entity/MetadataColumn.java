package com.datapilot.pojo.entity;

import lombok.Data;

@Data
public class MetadataColumn {
    private Long id;

    private Long tableId;

    private String columnName;

    private String dataType;

    private String columnComment;

    private Integer nullable;

    private Integer primaryKey;

    private Integer ordinalPosition;
}
