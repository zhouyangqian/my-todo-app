package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.MarketingActivity;
import com.example.dict.mapper.MarketingActivityMapper;
import com.example.dict.service.MarketingActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 营销活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingActivityServiceImpl extends ServiceImpl<MarketingActivityMapper, MarketingActivity> implements MarketingActivityService {

    @Override
    public Page<MarketingActivity> getActivityPage(int page, int size, String activityName) {
        LambdaQueryWrapper<MarketingActivity> wrapper = new LambdaQueryWrapper<>();
        if (activityName != null && !activityName.isEmpty()) {
            wrapper.like(MarketingActivity::getActivityName, activityName);
        }
        wrapper.orderByDesc(MarketingActivity::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public MarketingActivity createActivity(MarketingActivity activity) {
        save(activity);
        return activity;
    }

    @Override
    @Transactional
    public MarketingActivity updateActivity(MarketingActivity activity) {
        updateById(activity);
        return activity;
    }

    @Override
    @Transactional
    public void deleteActivity(Long id) {
        removeById(id);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        MarketingActivity activity = getById(id);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在: " + id);
        }
        activity.setStatus(1);
        updateById(activity);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        MarketingActivity activity = getById(id);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在: " + id);
        }
        activity.setStatus(0);
        updateById(activity);
    }
}
