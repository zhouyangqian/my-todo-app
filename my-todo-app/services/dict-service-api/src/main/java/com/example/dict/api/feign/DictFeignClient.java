package com.example.dict.api.feign;

import com.example.common.core.result.ApiResponse;
import com.example.dict.api.dto.DictItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 字典服务 Feign 客户端
 * <p>
 * 供其他服务调用 dict-service 读取字典项和配置值。
 * </p>
 */
@FeignClient(name = "dict-service", path = "/api/dict")
public interface DictFeignClient {

    @GetMapping("/items/by-code/{code}")
    ApiResponse<List<DictItemDTO>> getItemsByCode(@PathVariable("code") String code);
}
