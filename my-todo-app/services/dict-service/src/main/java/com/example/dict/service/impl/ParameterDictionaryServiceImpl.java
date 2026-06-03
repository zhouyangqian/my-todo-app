package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.ParameterDictionary;
import com.example.dict.entity.ParameterItem;
import com.example.dict.mapper.ParameterDictionaryMapper;
import com.example.dict.mapper.ParameterItemMapper;
import com.example.dict.service.ParameterDictionaryService;
import com.example.common.core.util.CodeGenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 参数字典服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParameterDictionaryServiceImpl extends ServiceImpl<ParameterDictionaryMapper, ParameterDictionary> implements ParameterDictionaryService {

    private final ParameterItemMapper parameterItemMapper;

    @Override
    @Transactional
    public ParameterDictionary createDictionary(ParameterDictionary dictionary) {
        // 自动生成参数编码（格式：PARAM-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("PARAM",
                dictionary.getParamName(),
                code -> getOne(new LambdaQueryWrapper<ParameterDictionary>()
                        .eq(ParameterDictionary::getTenantId, dictionary.getTenantId())
                        .eq(ParameterDictionary::getParamCode, code)
                        .eq(ParameterDictionary::getDeleted, 0)) != null
        );
        dictionary.setParamCode(generatedCode);
        save(dictionary);
        return dictionary;
    }

    @Override
    @Transactional
    public ParameterDictionary updateDictionary(ParameterDictionary dictionary) {
        // MyBatis-Plus @Version 注解会自动处理乐观锁
        int rows = baseMapper.updateById(dictionary);
        if (rows == 0) {
            throw new IllegalArgumentException("更新失败，数据可能已被其他人修改，请刷新后重试");
        }
        return dictionary;
    }

    @Override
    @Transactional
    public void deleteDictionary(Long id) {
        ParameterDictionary dictionary = getById(id);
        if (dictionary != null) {
            dictionary.setDeleted(1);
            updateById(dictionary);

            // 删除关联的参数项
            parameterItemMapper.delete(
                new LambdaQueryWrapper<ParameterItem>()
                    .eq(ParameterItem::getDictionaryId, id)
            );
        }
    }

    @Override
    public Page<ParameterDictionary> getDictionaryPage(Long tenantId, int page, int size, Long categoryId, String paramName) {
        LambdaQueryWrapper<ParameterDictionary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParameterDictionary::getTenantId, tenantId)
               .eq(ParameterDictionary::getDeleted, 0);
        if (categoryId != null) {
            wrapper.eq(ParameterDictionary::getCategoryId, categoryId);
        }
        if (paramName != null && !paramName.isEmpty()) {
            wrapper.like(ParameterDictionary::getParamName, paramName);
        }
        wrapper.orderByAsc(ParameterDictionary::getSortOrder);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public ParameterDictionary getDictionaryByCode(Long tenantId, String paramCode) {
        return getOne(
            new LambdaQueryWrapper<ParameterDictionary>()
                .eq(ParameterDictionary::getTenantId, tenantId)
                .eq(ParameterDictionary::getParamCode, paramCode)
                .eq(ParameterDictionary::getDeleted, 0)
                .eq(ParameterDictionary::getStatus, 1)
        );
    }
}
