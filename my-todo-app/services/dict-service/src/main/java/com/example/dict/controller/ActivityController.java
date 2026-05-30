package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.MarketingActivity;
import com.example.dict.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动管理控制器
 */
@Tag(name = "活动管理", description = "营销活动管理")
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "分页查询活动")
    @GetMapping("/page")
    public ApiResponse<PageResult<MarketingActivity>> getActivityPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String activityName) {
        Page<MarketingActivity> result = activityService.getActivityPage(page, size, activityName);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public ApiResponse<MarketingActivity> getActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.getById(id));
    }

    @Operation(summary = "创建活动")
    @PostMapping("/create")
    public ApiResponse<MarketingActivity> createActivity(@RequestBody MarketingActivity activity) {
        return ApiResponse.success(activityService.createActivity(activity));
    }

    @Operation(summary = "更新活动")
    @PutMapping("/update/{id}")
    public ApiResponse<MarketingActivity> updateActivity(
            @PathVariable Long id,
            @RequestBody MarketingActivity activity) {
        activity.setId(id);
        return ApiResponse.success(activityService.updateActivity(activity));
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ApiResponse.success();
    }
}
