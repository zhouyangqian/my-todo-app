package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.MarketingActivity;

/**
 * 活动管理服务接口
 */
public interface ActivityService extends IService<MarketingActivity> {

    Page<MarketingActivity> getActivityPage(int page, int size, String activityName);

    MarketingActivity createActivity(MarketingActivity activity);

    MarketingActivity updateActivity(MarketingActivity activity);

    void deleteActivity(Long id);
}
