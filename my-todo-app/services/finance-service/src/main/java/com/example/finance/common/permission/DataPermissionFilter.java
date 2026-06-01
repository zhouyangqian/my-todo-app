package com.example.finance.common.permission;

import com.example.common.core.annotation.DataPermission;
import com.example.common.core.annotation.DataScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataPermissionFilter {

    /**
     * 财务数据权限检查辅助方法
     * 实际过滤由 common-mybatis DataPermissionInterceptor 处理
     * 此类提供业务层权限验证入口
     */
    public void checkFinanceDataPermission(Long userId, Long tenantId, String operation) {
        log.debug("财务数据权限检查: userId={}, tenantId={}, operation={}", userId, tenantId, operation);
        // 权限检查由 @DataPermission 注解 + DataPermissionInterceptor 自动处理
    }
}
