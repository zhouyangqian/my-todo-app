package com.example.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.api.dto.TenantDTO;
import com.example.auth.api.dto.TenantRegisterResultDTO;
import com.example.auth.api.vo.TenantRegisterVO;
import com.example.auth.entity.Tenant;
import com.example.auth.entity.User;
import com.example.auth.mapper.TenantMapper;
import com.example.auth.mapper.UserMapper;
import com.example.common.core.exception.BusinessException;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.util.CodeGenerateUtil;
import com.example.permission.api.vo.TenantInitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 租户服务实现
 * <p>
 * 处理租户注册、查询等业务逻辑。
 * 注册时同时创建租户记录和管理员账户。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public TenantRegisterResultDTO registerTenant(TenantRegisterVO request) {
        // 校验租户名称唯一性
        Tenant existingTenant = tenantMapper.selectOne(
                new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getTenantName, request.getTenantName())
        );
        if (existingTenant != null) {
            throw new BusinessException(400, "租户名称已存在");
        }

        // 创建租户记录
        Tenant tenant = new Tenant();
        tenant.setTenantName(request.getTenantName());

        // 自动生成租户编码（格式：ZU-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("ZU",
                request.getTenantName(),
                code -> tenantMapper.selectCount(
                    new LambdaQueryWrapper<Tenant>().eq(Tenant::getTenantCode, code)
                ) > 0
        );
        tenant.setTenantCode(generatedCode);
        tenant.setStatus(1); // 1-正常
        tenant.setUserLimit(5); // 默认5个用户
        tenant.setContactName(request.getContactName());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.insert(tenant);

        Long tenantId = tenant.getId();

        // 自动生成管理员凭据（前端不再传入）
        // 用户名默认使用联系人姓名的拼音首字母，空则回退"admin"
        String baseUsername = (request.getAdminUsername() != null && !request.getAdminUsername().isBlank())
                ? request.getAdminUsername()
                : (request.getContactName() != null && !request.getContactName().isBlank()
                    ? CodeGenerateUtil.toPinyinInitials(request.getContactName()) : "admin");
        // 重名检测：同名时追加 01、02...递增
        String adminUsername = baseUsername;
        int suffix = 1;
        while (userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserName, adminUsername)
                        .eq(User::getTenantId, tenantId)
        ) > 0) {
            adminUsername = baseUsername + String.format("%02d", suffix++);
        }
        String adminEmail = (request.getAdminEmail() != null && !request.getAdminEmail().isBlank())
                ? request.getAdminEmail() : request.getContactEmail();
        String adminPassword = (request.getAdminPassword() != null && !request.getAdminPassword().isBlank())
                ? request.getAdminPassword() : generateRandomPassword();

        // 创建管理员账户
        User admin = new User();
        admin.setTenantId(tenantId);
        admin.setUserName(adminUsername);
        admin.setPassWord(passwordEncoder.encode(adminPassword));
        admin.setEmail(adminEmail);
        admin.setRealName(request.getContactName());
        admin.setStatus(1);  // 1-正常
        admin.setLocked(0);  // 0-未锁定
        admin.setLoginFailCount(0);
        admin.setPasswordChangedAt(LocalDateTime.now());
        userMapper.insert(admin);

        log.info("租户注册成功: tenantId={}, tenantCode={}, adminUserId={}",
                tenantId, generatedCode, admin.getId());

        // 初始化租户默认角色和权限（best-effort，不影响注册主流程）
        initTenantPermissions(tenantId, admin.getId(), adminUsername);

        return TenantRegisterResultDTO.builder()
                .tenantId(tenantId)
                .tenantCode(generatedCode)
                .adminUsername(adminUsername)
                .adminPassword(adminPassword)
                .build();
    }

    /** 生成 12 位随机密码（字母数字混合） */
    private String generateRandomPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 调用 permission-service 初始化租户默认角色和权限
     * <p>best-effort 模式：失败不阻塞注册流程，仅记录警告日志</p>
     */
    private void initTenantPermissions(Long tenantId, Long adminUserId, String adminUsername) {
        try {
            TenantInitVO initVO = new TenantInitVO();
            initVO.setTenantId(tenantId);
            initVO.setAdminUserId(adminUserId);
            initVO.setAdminUsername(adminUsername);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TenantInitVO> entity = new HttpEntity<>(initVO, headers);

            restTemplate.postForObject(
                    "http://permission-service/api/permissions/init-tenant",
                    entity,
                    ApiResponse.class
            );

            log.info("租户权限初始化成功: tenantId={}", tenantId);
        } catch (Exception e) {
            // permission-service 不可用时不影响租户注册的主流程
            log.warn("租户权限初始化失败（permission-service 可能不可用）: tenantId={}, error={}",
                    tenantId, e.getMessage());
        }
    }

    @Override
    public TenantDTO getTenantInfo(Long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(404, "租户不存在");
        }

        return TenantDTO.builder()
                .id(tenant.getId())
                .tenantName(tenant.getTenantName())
                .tenantCode(tenant.getTenantCode())
                .status(tenant.getStatus())
                .userLimit(tenant.getUserLimit())
                .contactName(tenant.getContactName())
                .contactEmail(tenant.getContactEmail())
                .contactPhone(tenant.getContactPhone())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
