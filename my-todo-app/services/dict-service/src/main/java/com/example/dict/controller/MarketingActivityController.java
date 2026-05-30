package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.MarketingActivity;
import com.example.dict.service.MarketingActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 营销活动管理控制器
 */
@Tag(name = "营销活动管理", description = "营销活动CRUD、激活/停用API")
@RestController
@RequestMapping("/api/dict/activity")
@RequiredArgsConstructor
public class MarketingActivityController {

    private final MarketingActivityService marketingActivityService;

    @RequiresPermission(code = "dict:activity:list", name = "查询活动列表")
    @Operation(summary = "分页查询活动")
    @GetMapping("/page")
    public ApiResponse<PageResult<MarketingActivity>> getActivityPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String activityName) {
        Page<MarketingActivity> result = marketingActivityService.getActivityPage(page, size, activityName);
        PageResult<MarketingActivity> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:activity:detail", name = "查询活动详情")
    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public ApiResponse<MarketingActivity> getActivity(@PathVariable Long id) {
        return ApiResponse.success(marketingActivityService.getById(id));
    }

    @RequiresPermission(code = "dict:activity:create", name = "创建活动")
    @Operation(summary = "创建活动")
    @PostMapping
    public ApiResponse<MarketingActivity> createActivity(@RequestBody MarketingActivity activity) {
        return ApiResponse.success(marketingActivityService.createActivity(activity));
    }

    @RequiresPermission(code = "dict:activity:update", name = "更新活动")
    @Operation(summary = "更新活动")
    @PutMapping("/{id}")
    public ApiResponse<MarketingActivity> updateActivity(
            @PathVariable Long id,
            @RequestBody MarketingActivity activity) {
        activity.setId(id);
        return ApiResponse.success(marketingActivityService.updateActivity(activity));
    }

    @RequiresPermission(code = "dict:activity:delete", name = "删除活动")
    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        marketingActivityService.deleteActivity(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "dict:activity:update", name = "激活活动")
    @Operation(summary = "激活活动")
    @PutMapping("/{id}/activate")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        marketingActivityService.activate(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "dict:activity:update", name = "停用活动")
    @Operation(summary = "停用活动")
    @PutMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        marketingActivityService.deactivate(id);
        return ApiResponse.success();
    }
}
