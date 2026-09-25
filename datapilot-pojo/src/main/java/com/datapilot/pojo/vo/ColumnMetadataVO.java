package com.datapilot.pojo.vo;

import lombok.Data;

@Data
public class ColumnMetadataVO {
    private Long id;

    private Long tableId;

    private String columnName;

    private String dataType;

    private String columnComment;

    private Integer nullable;

    private Integer primaryKey;

    private Integer ordinalPosition;
}
