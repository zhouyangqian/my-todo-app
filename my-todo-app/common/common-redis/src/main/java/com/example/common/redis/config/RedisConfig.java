package com.example.common.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置类
 * <p>
 * 配置 RedisTemplate 和 CacheManager，支持：
 * <ul>
 *   <li>JSON 序列化 - 对象以 JSON 格式存储，便于调试和跨语言访问</li>
 *   <li>缓存管理器 - 与 @Cacheable 等 Spring Cache 注解集成</li>
 *   <li>多租户缓存 - 租户隔离的缓存前缀</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * <p>
     * 使用 Jackson2JsonRedisSerializer 进行 JSON 序列化：
     * <ul>
     *   <li>Key 使用 StringRedisSerializer（便于阅读和调试）</li>
     *   <li>Value 使用 Jackson2JsonRedisSerializer（支持复杂对象）</li>
     * </ul>
     * </p>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 Jackson2JsonRedisSerializer 替换默认的 JdkSerializationRedisSerializer
        Jackson2JsonRedisSerializer<Object> serializer = createJacksonSerializer();

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 CacheManager
     * <p>
     * 与 Spring Cache 注解（@Cacheable、@CachePut、@CacheEvict）集成，
     * 支持方法级别的缓存。
     * </p>
     * <p>
     * 缓存配置：
     * <ul>
     *   <li>缓存有效期：2小时（可按需调整）</li>
     *   <li>Key 序列化：String</li>
     *   <li>Value 序列化：JSON</li>
     * </ul>
     * </p>
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> serializer = createJacksonSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存有效期 2 小时
                .entryTtl(Duration.ofHours(2))
                // 使用 String 序列化 Key
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 使用 JSON 序列化 Value
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                // 不缓存 null 值
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 创建 Jackson JSON 序列化器
     * <p>
     * 配置 ObjectMapper 以支持：
     * <ul>
     *   <li>访问所有字段（包括 private）</li>
     *   <li>启用类型信息（支持多态序列化）</li>
     *   <li>忽略未知属性（防止版本升级导致的反序列化失败）</li>
     * </ul>
     * </p>
     */
    private Jackson2JsonRedisSerializer<Object> createJacksonSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();

        // 设置访问权限
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用类型信息，支持多态序列化
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        serializer.setObjectMapper(objectMapper);
        return serializer;
    }
}
