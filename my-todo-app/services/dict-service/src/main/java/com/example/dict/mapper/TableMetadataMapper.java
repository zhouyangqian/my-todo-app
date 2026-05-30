package com.example.dict.mapper;

import com.example.dict.dto.TableColumnDTO;
import com.example.dict.dto.TableInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库元数据查询Mapper（查询INFORMATION_SCHEMA）
 */
@Mapper
public interface TableMetadataMapper {

    /**
     * 查询指定数据库列表中的所有业务表
     */
    List<TableInfoDTO> listTables(@Param("schemas") List<String> schemas);

    /**
     * 查询指定表的字段信息
     */
    List<TableColumnDTO> listColumns(@Param("schema") String schema,
                                     @Param("tableName") String tableName);
}
