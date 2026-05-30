package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.PurchaseReturn;

/**
 * 采购退货服务接口
 */
public interface PurchaseReturnService extends IService<PurchaseReturn> {

    Page<PurchaseReturn> getReturnPage(Long tenantId, int page, int size,
                                       Long supplierId, Integer returnStatus);

    PurchaseReturn getReturnDetail(Long returnId);

    PurchaseReturn createReturn(PurchaseReturn purchaseReturn, Long tenantId, Long userId);

    void submitForApproval(Long returnId);

    void approveReturn(Long returnId, Long userId);

    void cancelReturn(Long returnId);
}
