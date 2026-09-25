package com.datapilot.server.service;

import com.datapilot.pojo.dto.CreateDataSourceDTO;
import com.datapilot.pojo.dto.UpdateDataSourceDTO;
import com.datapilot.pojo.vo.DataSourceVO;

import java.util.List;

public interface DataSourceService {
    /**
     * 新增数据源
     */
    Long create(CreateDataSourceDTO dto);

    /**
     * 查询当前用户的数据源列表
     */
    List<DataSourceVO> list();

    /**
     * 根据id查询数据源
     */
    DataSourceVO getById(Long id);

    /**
     * 修改数据源
     */
    void update(Long id, UpdateDataSourceDTO dto);

    /**
     * 删除数据源
     */
    void delete(Long id);

    /**
     * 测试数据源连接
     */
    void testConnection(Long id);
}
