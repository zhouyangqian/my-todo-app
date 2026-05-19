package com.example.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.inventory.entity.InventoryCheckItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryCheckItemMapper extends BaseMapper<InventoryCheckItem> {
}
