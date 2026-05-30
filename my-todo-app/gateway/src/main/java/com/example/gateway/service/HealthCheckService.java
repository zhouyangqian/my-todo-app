package com.example.gateway.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务健康检查服务
 * <p>
 * 定期通过 WebClient ping 各下游服务的 /actuator/health 端点，
 * 记录服务状态（UP/DOWN/UNKNOWN）、响应时间和错误信息。
 * </p>
 */
@Slf4j
@Component
public class HealthCheckService {

    private final WebClient webClient;

    /** 各服务健康状态缓存 */
    private final ConcurrentHashMap<String, ServiceHealth> healthMap = new ConcurrentHashMap<>();

    /** 已知服务列表及其默认端口 */
    private static final Map<String, Integer> KNOWN_SERVICES = Map.of(
            "auth-service", 8081,
            "user-service", 8082,
            "permission-service", 8083,
            "dict-service", 8084,
            "erp-service", 8085,
            "finance-service", 8086,
            "inventory-service", 8087
    );

    public HealthCheckService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 服务健康状态信息
     */
    @Data
    public static class ServiceHealth {
        /** 服务名称 */
        private String serviceName;
        /** 状态：UP、DOWN、UNKNOWN */
        private String status = "UNKNOWN";
        /** 最后检查时间 */
        private String lastCheckTime;
        /** 响应时间（毫秒） */
        private Long responseTime;
        /** 错误信息 */
        private String errorMessage;
    }

    /**
     * 定期检查所有已知服务的健康状态（每30秒）
     */
    @Scheduled(fixedRate = 30000)
    public void checkAllServices() {
        log.debug("开始健康检查，共{}个服务", KNOWN_SERVICES.size());
        for (Map.Entry<String, Integer> entry : KNOWN_SERVICES.entrySet()) {
            checkService(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 检查单个服务的健康状态
     *
     * @param serviceName 服务名称
     * @param port        服务端口
     */
    private void checkService(String serviceName, Integer port) {
        ServiceHealth health = healthMap.computeIfAbsent(serviceName, name -> {
            ServiceHealth h = new ServiceHealth();
            h.setServiceName(name);
            return h;
        });

        long startTime = System.currentTimeMillis();
        String url = "http://localhost:" + port + "/actuator/health";

        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;
            health.setStatus("UP");
            health.setResponseTime(responseTime);
            health.setLastCheckTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            health.setErrorMessage(null);
            log.debug("服务 {} 健康，响应时间: {}ms", serviceName, responseTime);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            health.setStatus("DOWN");
            health.setResponseTime(responseTime);
            health.setLastCheckTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            health.setErrorMessage(e.getMessage());
            log.warn("服务 {} 不可用: {}", serviceName, e.getMessage());
        }
    }

    /**
     * 获取所有服务的健康状态
     *
     * @return 服务健康状态列表
     */
    public List<ServiceHealth> getServiceStatuses() {
        // 确保所有已知服务都有记录
        for (String serviceName : KNOWN_SERVICES.keySet()) {
            healthMap.computeIfAbsent(serviceName, name -> {
                ServiceHealth h = new ServiceHealth();
                h.setServiceName(name);
                h.setStatus("UNKNOWN");
                return h;
            });
        }
        return new ArrayList<>(healthMap.values());
    }
}
