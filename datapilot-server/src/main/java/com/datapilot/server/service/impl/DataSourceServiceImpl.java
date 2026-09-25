package com.datapilot.server.service.impl;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.pojo.dto.CreateDataSourceDTO;
import com.datapilot.pojo.dto.UpdateDataSourceDTO;
import com.datapilot.pojo.entity.DataSourceEntity;
import com.datapilot.pojo.vo.DataSourceVO;
import com.datapilot.server.datasource.ExternalDataSourceManager;
import com.datapilot.server.mapper.DataSourceMapper;
import com.datapilot.server.security.DataSourcePasswordCrypto;
import com.datapilot.server.security.SecurityUtils;
import com.datapilot.server.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceMapper dataSourceMapper;
    private final DataSourcePasswordCrypto crypto;
    private final ExternalDataSourceManager dataSourceManager;

    @Override
    public Long create(CreateDataSourceDTO dto){
        //获取当前用户的id
        Long userId= SecurityUtils.getCurrentId();

        //构建实体类对象
        DataSourceEntity dataSourceEntity=new DataSourceEntity();
        BeanUtils.copyProperties(dto,dataSourceEntity);
        dataSourceEntity.setCreatedBy(userId);

        //加密数据源密码
        dataSourceEntity.setPasswordCipher(crypto.encrypt(dto.getPassword()));

        //插入数据库
        dataSourceMapper.insert(dataSourceEntity);
        //获取数据源id
        return dataSourceEntity.getId();
    }

    @Override
    public List<DataSourceVO> list(){
        //获取当前用户id
        Long userId=SecurityUtils.getCurrentId();

        List<DataSourceVO> dataSourceVOS=new ArrayList<>();

        List<DataSourceEntity> entities=dataSourceMapper.selectByCreatedBy(userId);

        for(DataSourceEntity entity:entities){
            DataSourceVO vo=new DataSourceVO();
            BeanUtils.copyProperties(entity,vo);
            dataSourceVOS.add(vo);
        }

        return dataSourceVOS;
    }

    @Override
    public DataSourceVO getById(Long id){
        DataSourceEntity entity=getOwnDataSource(id);

        DataSourceVO vo=new DataSourceVO();
        BeanUtils.copyProperties(entity,vo);

        return vo;
    }

    @Override
    public void update(Long id, UpdateDataSourceDTO dto){
        DataSourceEntity entity=getOwnDataSource(id);

        if(dto.getPassword()!=null && !dto.getPassword().isBlank()){
            String passwordCipher=crypto.encrypt(dto.getPassword());
            entity.setPasswordCipher(passwordCipher);
        }

        entity.setName(dto.getName());
        entity.setDbType(dto.getDbType());
        entity.setHost(dto.getHost());
        entity.setPort(dto.getPort());
        entity.setDatabaseName(dto.getDatabaseName());
        entity.setStatus(dto.getStatus());
        entity.setUsername(dto.getUsername());
        entity.setRemark(dto.getRemark());

        dataSourceMapper.update(entity);

        //配置变化，销毁连接池
        dataSourceManager.remove(id);
    }

    @Override
    public void delete(Long id){
        DataSourceEntity entity=getOwnDataSource(id);

        if(entity.getStatus()==1){
            throw new BusinessException("请先禁用数据源后再删除");
        }
        dataSourceMapper.deleteById(id);

        //数据源删除后，销毁连接池
        dataSourceManager.remove(id);
    }

    private DataSourceEntity getOwnDataSource(Long id){
        DataSourceEntity entity=dataSourceMapper.selectById(id);

        if(entity==null){
            throw new BusinessException("数据源不存在");
        }
        if(!entity.getCreatedBy().equals(SecurityUtils.getCurrentId())){
            throw new BusinessException("无权访问该数据源");
        }
        return entity;
    }

    @Override
    public void testConnection(Long id){
        DataSourceEntity entity=getOwnDataSource(id);

        String password=crypto.decrypt(entity.getPasswordCipher());

        String jdbcUrl=String.format(
                "jdbc:mysql://%s:%d/%s"+"?connectionTimeout=5000"+"&socketTimeout=5000"+"&useUnicode=true"+
                        "characterEncoding=UTF-8"+"&serverTimezone=Asia/Shanghai",
                entity.getHost(),entity.getPort(),entity.getDatabaseName()
        );

        try(Connection connection= DriverManager.getConnection(jdbcUrl,entity.getUsername(),password)){
            if(!connection.isValid(5)){
                throw new BusinessException("数据库连接失败");
            }
        }catch (SQLException e){
            e.printStackTrace();
            throw new BusinessException("数据库连接失败，请检查数据库配置");
        }
    }
}
