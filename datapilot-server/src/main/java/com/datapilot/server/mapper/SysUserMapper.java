package com.datapilot.server.mapper;

import com.datapilot.pojo.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {
    /**
     * 根据用户名查询系统用户
     * @param username
     * @return
     */
    public SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据id查询系统用户
     * @param id
     * @return
     */
    public SysUser selectById(@Param("id") Long id);




}
