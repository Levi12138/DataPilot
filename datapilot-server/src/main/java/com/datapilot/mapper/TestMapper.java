package com.datapilot.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper {
    String getNameById(Long id);
}
