package com.example.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dict.entity.ActivityParticipation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动参与记录Mapper
 */
@Mapper
public interface ActivityParticipationMapper extends BaseMapper<ActivityParticipation> {
}
