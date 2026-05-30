package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.Captcha;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供验证码表的 CRUD 操作</p>
 */
@Mapper
public interface CaptchaMapper extends BaseMapper<Captcha> {
}
