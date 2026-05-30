package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.SalesReturn;

/**
 * 销售退货服务接口
 */
public interface SalesReturnService extends IService<SalesReturn> {

    Page<SalesReturn> getReturnPage(Long tenantId, int page, int size,
                                     Long customerId, Integer returnStatus);

    SalesReturn getReturnDetail(Long returnId);

    SalesReturn createReturn(SalesReturn salesReturn, Long tenantId, Long userId);

    void submitForApproval(Long returnId);

    void approveReturn(Long returnId, Long userId);

    void cancelReturn(Long returnId);
}
