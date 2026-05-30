package com.example.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.Blacklist;

/**
 * 黑名单管理服务接口
 */
public interface BlacklistService extends IService<Blacklist> {

    Blacklist addToBlacklist(Long userId, String reason, Long operatorId, String operatorType, Long tenantId);

    void removeFromBlacklist(Long id, Long removedBy);

    boolean isUserBlacklisted(Long userId, Long tenantId);

    Page<Blacklist> getBlacklistPage(Long tenantId, int page, int size, Integer status);

    Blacklist getByUserId(Long userId, Long tenantId);
}
