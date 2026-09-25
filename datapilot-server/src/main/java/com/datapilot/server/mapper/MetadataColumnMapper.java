package com.datapilot.server.mapper;

import com.datapilot.pojo.entity.MetadataColumn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MetadataColumnMapper {
    void insert(MetadataColumn metadataColumn);

    void deleteByDatasourceId(Long datasourceId);

    List<MetadataColumn> selectByTableId(Long tableId);
}
