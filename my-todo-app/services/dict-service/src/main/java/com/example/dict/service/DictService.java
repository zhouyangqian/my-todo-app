package com.example.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.DictItem;
import com.example.dict.entity.DictType;
import com.example.dict.mapper.DictItemMapper;
import com.example.dict.mapper.DictTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 字典服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictService extends ServiceImpl<DictTypeMapper, DictType> {

    private final DictItemMapper dictItemMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 字典缓存Key前缀
     */
    private static final String DICT_CACHE_KEY = "dict:";
    /**
     * 缓存过期时间(小时)
     */
    private static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 分页查询字典类型
     */
    public Page<DictType> getDictTypePage(Long tenantId, int page, int size, String dictName) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getTenantId, tenantId)
               .eq(DictType::getDeleted, 0);
        if (dictName != null && !dictName.isEmpty()) {
            wrapper.like(DictType::getDictName, dictName);
        }
        wrapper.orderByAsc(DictType::getSort);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据字典编码获取字典项列表(带缓存)
     */
    @SuppressWarnings("unchecked")
    public List<DictItem> getDictItemsByCode(String dictCode, Long tenantId) {
        String cacheKey = DICT_CACHE_KEY + tenantId + ":" + dictCode;

        // 先从缓存获取
        List<DictItem> cachedItems = (List<DictItem>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedItems != null) {
            return cachedItems;
        }

        // 从数据库查询
        List<DictItem> items = dictItemMapper.selectByDictCode(dictCode, tenantId);

        // 写入缓存
        if (!items.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, items, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return items;
    }

    /**
     * 清除字典缓存
     */
    public void clearDictCache(String dictCode, Long tenantId) {
        String cacheKey = DICT_CACHE_KEY + tenantId + ":" + dictCode;
        redisTemplate.delete(cacheKey);
        log.info("清除字典缓存: {}", dictCode);
    }

    /**
     * 清除租户所有字典缓存
     */
    public void clearAllDictCache(Long tenantId) {
        String pattern = DICT_CACHE_KEY + tenantId + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("清除租户所有字典缓存: tenantId={}", tenantId);
    }

    /**
     * 创建字典类型
     */
    @Transactional
    public DictType createDictType(DictType dictType) {
        // 检查编码是否已存在
        DictType existing = getOne(
            new LambdaQueryWrapper<DictType>()
                .eq(DictType::getTenantId, dictType.getTenantId())
                .eq(DictType::getDictCode, dictType.getDictCode())
                .eq(DictType::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("字典编码已存在: " + dictType.getDictCode());
        }
        save(dictType);
        return dictType;
    }

    /**
     * 更新字典类型
     */
    @Transactional
    public DictType updateDictType(DictType dictType) {
        updateById(dictType);
        // 清除缓存
        clearDictCache(dictType.getDictCode(), dictType.getTenantId());
        return dictType;
    }

    /**
     * 删除字典类型(软删除)
     */
    @Transactional
    public void deleteDictType(Long id, Long tenantId) {
        DictType dictType = getById(id);
        if (dictType != null) {
            dictType.setDeleted(1);
            updateById(dictType);

            // 删除关联的字典项
            dictItemMapper.delete(
                new LambdaQueryWrapper<DictItem>()
                    .eq(DictItem::getDictTypeId, id)
            );

            // 清除缓存
            clearDictCache(dictType.getDictCode(), tenantId);
        }
    }

    /**
     * 添加字典项
     */
    @Transactional
    public DictItem addDictItem(DictItem dictItem) {
        dictItemMapper.insert(dictItem);
        // 清除缓存
        DictType dictType = getById(dictItem.getDictTypeId());
        if (dictType != null) {
            clearDictCache(dictType.getDictCode(), dictItem.getTenantId());
        }
        return dictItem;
    }

    /**
     * 更新字典项
     */
    @Transactional
    public DictItem updateDictItem(DictItem dictItem) {
        dictItemMapper.updateById(dictItem);
        // 清除缓存
        DictType dictType = getById(dictItem.getDictTypeId());
        if (dictType != null) {
            clearDictCache(dictType.getDictCode(), dictItem.getTenantId());
        }
        return dictItem;
    }

    /**
     * 删除字典项(软删除)
     */
    @Transactional
    public void deleteDictItem(Long id, Long tenantId) {
        DictItem dictItem = dictItemMapper.selectById(id);
        if (dictItem != null) {
            dictItem.setDeleted(1);
            dictItemMapper.updateById(dictItem);

            // 清除缓存
            DictType dictType = getById(dictItem.getDictTypeId());
            if (dictType != null) {
                clearDictCache(dictType.getDictCode(), tenantId);
            }
        }
    }

    /**
     * 获取字典类型的字典项列表
     */
    public List<DictItem> getDictItemsByTypeId(Long typeId) {
        return dictItemMapper.selectList(
            new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getDictTypeId, typeId)
                .eq(DictItem::getDeleted, 0)
                .orderByAsc(DictItem::getSort)
        );
    }
}
