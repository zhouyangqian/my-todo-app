package com.example.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.api.dto.TenantDTO;
import com.example.auth.api.vo.TenantRegisterVO;
import com.example.auth.entity.Tenant;
import com.example.auth.entity.User;
import com.example.auth.mapper.TenantMapper;
import com.example.auth.mapper.UserMapper;
import com.example.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public Long registerTenant(TenantRegisterVO request) {
        // 校验租户编码唯一性
        Tenant existingTenant = tenantMapper.selectOne(
                new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getTenantCode, request.getTenantCode())
        );
        if (existingTenant != null) {
            throw new BusinessException(400, "租户编码已存在");
        }

        // 校验租户名称唯一性
        existingTenant = tenantMapper.selectOne(
                new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getTenantName, request.getTenantName())
        );
        if (existingTenant != null) {
            throw new BusinessException(400, "租户名称已存在");
        }

        // 创建租户记录
        Tenant tenant = new Tenant();
        tenant.setTenantName(request.getTenantName());
        tenant.setTenantCode(request.getTenantCode());
        tenant.setStatus(1); // 1-正常
        tenant.setUserLimit(5); // 默认5个用户
        tenant.setContactName(request.getContactName());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.insert(tenant);

        Long tenantId = tenant.getId();

        // 校验管理员用户名唯一性（在租户范围内）
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserName, request.getAdminUsername())
                        .eq(User::getTenantId, tenantId)
        );
        if (existingUser != null) {
            throw new BusinessException(400, "管理员用户名已存在");
        }

        // 创建管理员账户
        User admin = new User();
        admin.setTenantId(tenantId);
        admin.setUserName(request.getAdminUsername());
        admin.setPassWord(passwordEncoder.encode(request.getAdminPassword()));
        admin.setEmail(request.getAdminEmail());
        admin.setRealName(request.getContactName());
        admin.setStatus(1);  // 1-正常
        admin.setLocked(0);  // 0-未锁定
        admin.setLoginFailCount(0);
        admin.setPasswordChangedAt(LocalDateTime.now());
        userMapper.insert(admin);

        log.info("租户注册成功: tenantId={}, tenantCode={}, adminUserId={}",
                tenantId, request.getTenantCode(), admin.getId());

        return tenantId;
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
