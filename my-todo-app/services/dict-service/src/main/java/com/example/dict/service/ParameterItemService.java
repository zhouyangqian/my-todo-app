package com.example.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.ParameterItem;

import java.util.List;

/**
 * 参数项服务接口
 */
public interface ParameterItemService extends IService<ParameterItem> {

    ParameterItem createItem(ParameterItem item);

    ParameterItem updateItem(ParameterItem item);

    void deleteItem(Long id);

    List<ParameterItem> getItemsByDictionaryId(Long dictionaryId);

    void batchSaveItems(Long dictionaryId, List<ParameterItem> items);
}
