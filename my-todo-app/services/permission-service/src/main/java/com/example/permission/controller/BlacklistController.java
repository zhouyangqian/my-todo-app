package com.example.permission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.permission.api.vo.BlacklistCreateVO;
import com.example.permission.entity.Blacklist;
import com.example.permission.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 黑名单管理控制器
 * <p>
 * 提供黑名单的加入、解除、分页查询、检查等接口。
 * 加入黑名单时会自动踢出该用户的所有会话。
 * </p>
 */
@Tag(name = "黑名单管理")
@RestController
@RequestMapping("/api/permissions/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    /**
     * 加入黑名单
     *
     * @param vo       拉黑参数
     * @param tenantId 租户ID
     * @param userId   当前操作用户ID
     * @return 创建的黑名单记录
     */
    @Operation(summary = "加入黑名单")
    @RequiresPermission(code = "system:blacklist:add", name = "加入黑名单")
    @PostMapping
    public ApiResponse<Blacklist> addToBlacklist(
            @RequestBody BlacklistCreateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        Blacklist blacklist = blacklistService.addToBlacklist(
                vo.getUserId(), vo.getReason(), userId, vo.getOperatorType(), tenantId);
        return ApiResponse.success(blacklist);
    }

    /**
     * 解除黑名单
     *
     * @param id     黑名单记录ID
     * @param userId 当前操作用户ID
     * @return 空响应
     */
    @Operation(summary = "解除黑名单")
    @RequiresPermission(code = "system:blacklist:remove", name = "解除黑名单")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeFromBlacklist(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        blacklistService.removeFromBlacklist(id, userId);
        return ApiResponse.success();
    }

    /**
     * 分页查询黑名单
     *
     * @param tenantId 租户ID
     * @param page     当前页码
     * @param size     每页条数
     * @param status   状态过滤（可选）
     * @return 分页数据
     */
    @Operation(summary = "分页查询黑名单")
    @RequiresPermission(code = "system:blacklist:list", name = "查询黑名单")
    @GetMapping
    public ApiResponse<PageResult<Blacklist>> getBlacklistPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<Blacklist> result = blacklistService.getBlacklistPage(tenantId, page, size, status);
        PageResult<Blacklist> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 检查用户是否在黑名单中
     *
     * @param userId   待检查用户ID
     * @param tenantId 租户ID
     * @return true=在黑名单中
     */
    @Operation(summary = "检查用户是否在黑名单中")
    @RequiresPermission(code = "system:blacklist:check", name = "检查黑名单状态")
    @GetMapping("/check")
    public ApiResponse<Boolean> checkBlacklist(
            @RequestParam Long userId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        boolean blacklisted = blacklistService.isUserBlacklisted(userId, tenantId);
        return ApiResponse.success(blacklisted);
    }
}
