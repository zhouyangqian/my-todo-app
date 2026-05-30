package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.ParameterItem;
import com.example.dict.mapper.ParameterItemMapper;
import com.example.dict.service.ParameterItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 参数项服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParameterItemServiceImpl extends ServiceImpl<ParameterItemMapper, ParameterItem> implements ParameterItemService {

    @Override
    @Transactional
    public ParameterItem createItem(ParameterItem item) {
        save(item);
        return item;
    }

    @Override
    @Transactional
    public ParameterItem updateItem(ParameterItem item) {
        updateById(item);
        return item;
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        ParameterItem item = getById(id);
        if (item != null) {
            item.setDeleted(1);
            updateById(item);
        }
    }

    @Override
    public List<ParameterItem> getItemsByDictionaryId(Long dictionaryId) {
        return list(
            new LambdaQueryWrapper<ParameterItem>()
                .eq(ParameterItem::getDictionaryId, dictionaryId)
                .eq(ParameterItem::getDeleted, 0)
                .orderByAsc(ParameterItem::getSortOrder)
        );
    }

    @Override
    @Transactional
    public void batchSaveItems(Long dictionaryId, List<ParameterItem> items) {
        // 先软删除旧的参数项
        List<ParameterItem> existingItems = list(
            new LambdaQueryWrapper<ParameterItem>()
                .eq(ParameterItem::getDictionaryId, dictionaryId)
                .eq(ParameterItem::getDeleted, 0)
        );
        for (ParameterItem existing : existingItems) {
            existing.setDeleted(1);
            updateById(existing);
        }

        // 批量插入新参数项
        for (ParameterItem item : items) {
            item.setId(null);
            item.setDictionaryId(dictionaryId);
            save(item);
        }
    }
}
