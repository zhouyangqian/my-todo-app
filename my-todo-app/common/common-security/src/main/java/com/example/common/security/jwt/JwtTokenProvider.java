package com.example.common.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌提供者
 * <p>
 * 负责 JWT 令牌的生成、解析和验证。支持访问令牌（access token）和刷新令牌（refresh token）。
 * 令牌中携带用户ID、用户名、租户ID等信息，用于服务间身份传递。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * 获取签名密钥
     * <p>将配置的密钥字符串转换为 HMAC-SHA 密钥对象</p>
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param tenantId 租户ID
     * @return JWT 访问令牌字符串
     */
    public String generateAccessToken(Long userId, String username, Long tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);         // 用户ID
        claims.put("username", username);     // 用户名
        claims.put("tenantId", tenantId);     // 租户ID
        claims.put("type", "access");         // 令牌类型：访问令牌
        return generateToken(claims, userId.toString(), jwtProperties.getAccessTokenExpiration());
    }

    /**
     * 生成刷新令牌
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return JWT 刷新令牌字符串
     */
    public String generateRefreshToken(Long userId, Long tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);         // 用户ID
        claims.put("tenantId", tenantId);     // 租户ID
        claims.put("type", "refresh");        // 令牌类型：刷新令牌
        return generateToken(claims, userId.toString(), jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * 生成 JWT 令牌的核心方法
     *
     * @param claims     自定义声明（载荷数据）
     * @param subject    令牌主题（通常为用户ID）
     * @param expiration 过期时间（毫秒）
     * @return JWT 令牌字符串
     */
    private String generateToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)                                          // 设置自定义声明
                .subject(subject)                                        // 设置主题
                .issuer(jwtProperties.getIssuer())                       // 设置签发者
                .audience().add(jwtProperties.getAudience()).and()        // 设置受众
                .issuedAt(now)                                           // 设置签发时间
                .expiration(expiryDate)                                  // 设置过期时间
                .id(java.util.UUID.randomUUID().toString())              // 设置唯一ID（用于令牌吊销）
                .signWith(getSigningKey())                               // 使用密钥签名
                .compact();
    }

    /**
     * 解析 JWT 令牌并返回声明数据
     * <p>
     * 如果令牌过期、格式错误或签名不匹配，将抛出对应的异常。
     * </p>
     *
     * @param token JWT 令牌字符串
     * @return Claims 声明数据
     * @throws JwtTokenExpiredException  令牌过期
     * @throws JwtTokenInvalidException  令牌无效
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 令牌已过期: {}", e.getMessage());
            throw new JwtTokenExpiredException("令牌已过期", e);
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 JWT 令牌: {}", e.getMessage());
            throw new JwtTokenInvalidException("不支持的令牌格式", e);
        } catch (MalformedJwtException e) {
            log.warn("JWT 令牌格式错误: {}", e.getMessage());
            throw new JwtTokenInvalidException("令牌格式错误", e);
        } catch (Exception e) {
            log.warn("JWT 令牌验证失败: {}", e.getMessage());
            throw new JwtTokenInvalidException("令牌验证失败", e);
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT 令牌
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        // JSON 反序列化时数字可能是 Integer 类型，需要转换为 Long
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT 令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }

    /**
     * 从令牌中获取租户ID
     *
     * @param token JWT 令牌
     * @return 租户ID
     */
    public Long getTenantId(String token) {
        Claims claims = parseToken(token);
        Object tenantId = claims.get("tenantId");
        if (tenantId instanceof Integer) {
            return ((Integer) tenantId).longValue();
        }
        return (Long) tenantId;
    }

    /**
     * 从令牌中获取令牌唯一ID（jti），用于令牌吊销
     *
     * @param token JWT 令牌
     * @return 令牌ID
     */
    public String getTokenId(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    /**
     * 判断令牌是否为刷新令牌
     *
     * @param token JWT 令牌
     * @return true-刷新令牌，false-访问令牌
     */
    public boolean isRefreshToken(String token) {
        Claims claims = parseToken(token);
        return "refresh".equals(claims.get("type"));
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT 令牌
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取令牌过期时间
     *
     * @param token JWT 令牌
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 获取 JWT 配置属性
     *
     * @return JwtProperties
     */
    public JwtProperties getJwtProperties() {
        return jwtProperties;
    }
}
