package com.datapilot.server.service;

import com.datapilot.pojo.vo.ColumnMetadataVO;
import com.datapilot.pojo.vo.TableMetadataVO;

import java.util.List;

public interface MetadataService {
    void sync(Long datasourceId);

    List<TableMetadataVO> listTables(Long datasourceId);

    List<ColumnMetadataVO> listColumns(Long datasourceId,Long tableId);
}
