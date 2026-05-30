package com.example.gateway.mapper;

import com.example.gateway.entity.GatewayRoute;

import java.util.List;
import java.util.Optional;

/**
 * 网关路由配置数据访问接口
 * Gateway 使用 WebFlux 响应式栈，不依赖 MyBatis-Plus
 * 配置存储在 application.yml 和 Redis 中，通过此接口抽象数据访问
 */
public interface GatewayRouteMapper {

    List<GatewayRoute> findAll();

    Optional<GatewayRoute> findByRouteId(String routeId);

    GatewayRoute save(GatewayRoute route);

    void deleteByRouteId(String routeId);
}
