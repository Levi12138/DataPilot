package com.datapilot.server.ai.service.impl;

import com.datapilot.pojo.entity.QueryHistory;
import com.datapilot.server.ai.service.QueryHistoryService;
import com.datapilot.server.mapper.QueryHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryHistoryServiceImpl implements QueryHistoryService {

    QueryHistoryMapper queryHistoryMapper;

    @Override
    public void save(QueryHistory queryHistory){
        queryHistoryMapper.insert(queryHistory);
    }
}
