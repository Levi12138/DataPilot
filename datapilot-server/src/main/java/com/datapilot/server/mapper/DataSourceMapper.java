package com.datapilot.server.mapper;

import com.datapilot.pojo.entity.DataSourceEntity;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DataSourceMapper {

    /**
     * 新增数据源
     */
    void insert(DataSourceEntity dataSource);

    /**
     * 根据id查询
     */
    DataSourceEntity selectById(Long id);

    /**
     * 查询当前用户创建的所有数据源
     */
    List<DataSourceEntity> selectByCreatedBy(Long createdBy);

    /**
     * 修改数据源
     */
    void update(DataSourceEntity dataSource);

    /**
     * 删除数据源
     */
    void deleteById(Long id);


}
