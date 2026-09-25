package com.datapilot.server.mapper;

import com.datapilot.pojo.entity.MetadataTable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MetadataTableMapper {
    void insert(MetadataTable matedataTable);

    void deleteByDatasourceId(Long dataSourceId);

    List<MetadataTable> selectByDatasourceId(Long dataSourceId);
}
