package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.DictItem;
import com.example.dict.entity.DictType;

import java.util.List;

/**
 * 字典服务接口
 */
public interface DictService extends IService<DictType> {

    Page<DictType> getDictTypePage(Long tenantId, int page, int size, String dictName);

    List<DictItem> getDictItemsByCode(String dictCode, Long tenantId);

    void clearDictCache(String dictCode, Long tenantId);

    void clearAllDictCache(Long tenantId);

    DictType createDictType(DictType dictType);

    DictType updateDictType(DictType dictType);

    void deleteDictType(Long id, Long tenantId);

    DictItem addDictItem(DictItem dictItem);

    DictItem updateDictItem(DictItem dictItem);

    void deleteDictItem(Long id, Long tenantId);

    List<DictItem> getDictItemsByTypeId(Long typeId);
}
