package com.example.user.api.feign;

import com.example.common.core.result.ApiResponse;
import com.example.user.api.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 * <p>
 * 供 auth-service、permission-service 等服务调用 user-service 查询用户信息。
 * </p>
 */
@FeignClient(name = "user-service", path = "/api/users")
public interface UserFeignClient {

    @GetMapping("/get-user/{id}")
    ApiResponse<UserDTO> getUserById(@PathVariable("id") Long id);
}
