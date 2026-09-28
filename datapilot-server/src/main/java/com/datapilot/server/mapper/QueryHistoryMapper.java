package com.datapilot.server.mapper;

import com.datapilot.pojo.entity.QueryHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QueryHistoryMapper {
    int insert(QueryHistory queryHistory);
}
