package com.example.common.permission.checker;

import com.example.common.core.exception.BusinessException;
import com.example.common.core.result.ApiResponse;
import com.example.permission.api.vo.PermissionCheckVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 权限校验器
 * <p>
 * 通过 RestTemplate 调用 permission-service 的 check-permission 接口，
 * 校验用户是否拥有指定权限。
 * </p>
 */
@Slf4j
@Component
public class PermissionChecker {

    private final RestTemplate restTemplate;

    public PermissionChecker(@LoadBalanced RestTemplate loadBalancedRestTemplate) {
        this.restTemplate = loadBalancedRestTemplate;
    }

    /**
     * 校验用户是否拥有指定权限
     *
     * @param userId         用户ID
     * @param tenantId       租户ID
     * @param permissionCode 权限编码
     * @return true=拥有权限, false=无权限
     */
    public boolean checkPermission(Long userId, Long tenantId, String permissionCode) {
        try {
            PermissionCheckVO request = new PermissionCheckVO();
            request.setUserId(userId);
            request.setPermissionCode(permissionCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Tenant-Id", String.valueOf(tenantId));

            HttpEntity<PermissionCheckVO> entity = new HttpEntity<>(request, headers);

            @SuppressWarnings("unchecked")
            ApiResponse<Boolean> response = restTemplate.postForObject(
                    "http://permission-service/api/permissions/check-permission",
                    entity,
                    ApiResponse.class
            );

            if (response == null || response.getData() == null) {
                return false;
            }
            return Boolean.TRUE.equals(response.getData());
        } catch (Exception e) {
            log.error("权限校验服务调用失败: userId={}, permission={}", userId, permissionCode, e);
            throw new BusinessException(500, "权限校验服务暂不可用");
        }
    }

    /**
     * RestTemplate 配置（带负载均衡）
     */
    @Configuration
    public static class RestTemplateConfig {

        @Bean
        @LoadBalanced
        public RestTemplate loadBalancedRestTemplate() {
            return new RestTemplate();
        }
    }
}
