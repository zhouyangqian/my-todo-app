package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.ParameterDictionary;

/**
 * 参数字典服务接口
 */
public interface ParameterDictionaryService extends IService<ParameterDictionary> {

    ParameterDictionary createDictionary(ParameterDictionary dictionary);

    ParameterDictionary updateDictionary(ParameterDictionary dictionary);

    void deleteDictionary(Long id);

    Page<ParameterDictionary> getDictionaryPage(Long tenantId, int page, int size, Long categoryId, String paramName);

    ParameterDictionary getDictionaryByCode(Long tenantId, String paramCode);
}
