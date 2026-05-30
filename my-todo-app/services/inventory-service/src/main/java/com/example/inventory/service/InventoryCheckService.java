package com.example.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.inventory.api.vo.CreateCheckVO;
import com.example.inventory.api.vo.SubmitCheckVO;
import com.example.inventory.entity.InventoryCheck;

import java.util.List;

/**
 * 库存盘点服务接口
 */
public interface InventoryCheckService {

    Page<InventoryCheck> getCheckPage(Long tenantId, int page, int size,
                                      Long warehouseId, Integer checkStatus);

    InventoryCheck getCheckDetail(Long checkId);

    InventoryCheck createCheck(InventoryCheck check, CreateCheckVO request,
                               Long tenantId, Long userId);

    void submitCheckResult(Long checkId, List<SubmitCheckVO.CheckItemSubmit> items, Long userId);

    void cancelCheck(Long checkId);
}
