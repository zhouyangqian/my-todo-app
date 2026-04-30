package com.example.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * Jackson 配置类
 * <p>
 * 解决 JavaScript Number 精度问题：
 * JavaScript 的 Number 类型最大安全整数是 2^53 - 1 = 9007199254740991
 * 超过这个限制的 Long 类型会丢失精度
 * 因此将所有 Long 类型序列化为 String 传给前端
 * </p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置 Jackson 将 Long 类型序列化为 String
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance)
                                  .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
}
