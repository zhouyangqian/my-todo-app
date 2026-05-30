package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.ActivityParticipation;
import com.example.dict.entity.MarketingActivity;
import com.example.dict.mapper.ActivityParticipationMapper;
import com.example.dict.mapper.MarketingActivityMapper;
import com.example.dict.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 活动管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<MarketingActivityMapper, MarketingActivity> implements ActivityService {

    private final ActivityParticipationMapper participationMapper;

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
    public List<ActivityParticipation> getParticipations(Long activityId) {
        LambdaQueryWrapper<ActivityParticipation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipation::getActivityId, activityId)
               .orderByDesc(ActivityParticipation::getCreatedAt);
        return participationMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public ActivityParticipation joinActivity(Long activityId, Long tenantId) {
        // 检查是否已参与
        LambdaQueryWrapper<ActivityParticipation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipation::getActivityId, activityId)
               .eq(ActivityParticipation::getTenantId, tenantId)
               .eq(ActivityParticipation::getStatus, 1);
        ActivityParticipation existing = participationMapper.selectOne(wrapper);
        if (existing != null) {
            throw new IllegalStateException("该租户已参与此活动");
        }

        // 检查活动是否存在且有效
        MarketingActivity activity = getById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在: " + activityId);
        }
        if (activity.getStatus() == null || activity.getStatus() != 1) {
            throw new IllegalStateException("活动未启用，无法参与");
        }

        ActivityParticipation participation = new ActivityParticipation();
        participation.setActivityId(activityId);
        participation.setTenantId(tenantId);
        participation.setStatus(1);
        participationMapper.insert(participation);
        return participation;
    }

    @Override
    @Transactional
    public void cancelParticipation(Long participationId) {
        ActivityParticipation participation = participationMapper.selectById(participationId);
        if (participation == null) {
            throw new IllegalArgumentException("参与记录不存在: " + participationId);
        }
        participation.setStatus(0);
        participationMapper.updateById(participation);
    }
}
