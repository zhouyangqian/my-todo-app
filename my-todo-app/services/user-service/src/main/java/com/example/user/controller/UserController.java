package com.example.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.user.cache.UserCache;
import com.example.user.dto.UserCreateDTO;
import com.example.user.dto.UserUpdateDTO;
import com.example.user.dto.UserVO;
import com.example.user.entity.Role;
import com.example.user.entity.User;
import com.example.user.entity.UserAddress;
import com.example.user.service.RoleService;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * <p>
 * 提供用户相关的 RESTful API 接口，包括用户的增删改查、启用/禁用、
 * 用户地址管理等功能。所有接口路径以 /api/users 为前缀。
 * 使用 Swagger/OpenAPI 注解生成接口文档，方便前端对接和联调。
 * </p>
 * <p>
 * 通过构造器注入（@RequiredArgsConstructor）的方式依赖注入 UserService，
 * 避免了字段注入的不可变性问题，提高了代码的可测试性。
 * </p>
 */
@Tag(name = "用户管理", description = "用户增删改查API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /** 用户业务逻辑服务，处理用户相关的核心业务 */
    private final UserService userService;

    /** 角色服务，用于查询用户角色信息 */
    private final RoleService roleService;

    /** 用户缓存组件 */
    private final UserCache userCache;

    /**
     * 分页查询用户列表
     * <p>
     * 支持按用户名、真实姓名、部门ID、状态等条件进行模糊或精确筛选，
     * 结果按创建时间降序排列，返回分页数据。
     * </p>
     *
     * @param tenantId 租户ID，从请求头中获取，用于多租户数据隔离
     * @param page     当前页码，默认为第1页
     * @param size     每页记录数，默认为10条
     * @param username 用户名（可选），支持模糊查询
     * @param realName 真实姓名（可选），支持模糊查询
     * @param deptId   部门ID（可选），精确匹配
     * @param status   用户状态（可选），0-禁用，1-启用
     * @return 分页包装的用户列表数据
     */
    @RequiresPermission(code = "system:user:list", name = "查询用户列表")
    @Operation(summary = "分页查询用户")
    @GetMapping("/get-user-page")
    public ApiResponse<PageResult<UserVO>> getUserPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        // 调用服务层执行分页查询
        Page<User> result = userService.getUserPage(tenantId, page, size, username, realName, deptId, status);
        // 转换为 UserVO 分页结果
        List<UserVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        PageResult<UserVO> pageResult = PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 根据用户ID获取用户详情
     * <p>
     * 优先从缓存获取，缓存未命中则查询数据库并回填缓存。
     * </p>
     *
     * @param id 用户ID，通过URL路径传入
     * @return 用户详细信息
     */
    @RequiresPermission(code = "system:user:detail", name = "查询用户详情")
    @Operation(summary = "获取用户详情")
    @GetMapping("/get-user/{id}")
    public ApiResponse<UserVO> getUser(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        // 优先从缓存获取
        UserVO cached = userCache.get(tenantId, id);
        if (cached != null) {
            return ApiResponse.success(cached);
        }
        // 缓存未命中，查询数据库
        User user = userService.getUserById(id);
        if (user == null) {
            return ApiResponse.success(null);
        }
        UserVO vo = convertToVO(user);
        // 回填缓存
        userCache.put(tenantId, vo);
        return ApiResponse.success(vo);
    }

    /**
     * 创建新用户
     * <p>
     * 从请求头中提取租户ID和当前操作用户ID，自动填充到用户对象中，
     * 保证数据归属和审计信息的完整性。
     * </p>
     *
     * @param dto      创建用户请求数据
     * @param tenantId 租户ID，从请求头中获取，用于多租户数据隔离
     * @param userId   当前操作用户ID，从请求头中获取，记录创建人
     * @return 创建成功后的用户信息（含自动生成的ID）
     */
    @Operation(summary = "创建用户")
    @RequiresPermission(code = "system:user:create", name = "新增用户")
    @PostMapping("/create-user")
    public ApiResponse<UserVO> createUser(
            @RequestBody @Valid UserCreateDTO dto,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        // DTO 转 Entity
        User user = new User();
        user.setUserName(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRealName(dto.getRealName());
        user.setDeptId(dto.getDepartmentId());
        // 设置租户ID，确保用户归属于当前租户
        user.setTenantId(tenantId);
        // 设置创建人ID，用于审计追踪
        user.setCreatedBy(userId);
        // 调用服务层创建用户（会校验用户名唯一性）
        User created = userService.createUser(user);
        // 如果 DTO 中包含角色列表，自动分配角色
        if (dto.getRoleIdList() != null && !dto.getRoleIdList().isEmpty()) {
            roleService.assignRolesToUser(created.getId(), dto.getRoleIdList(), tenantId);
        }
        return ApiResponse.success(convertToVO(created));
    }

    /**
     * 更新用户信息
     * <p>
     * 通过URL路径指定要更新的用户ID，从请求头获取当前操作用户ID
     * 用于记录更新人信息。
     * </p>
     *
     * @param id     要更新的用户ID，通过URL路径传入
     * @param dto    更新用户请求数据
     * @param tenantId 租户ID
     * @param userId 当前操作用户ID，从请求头中获取，记录更新人
     * @return 更新后的用户完整信息
     */
    @Operation(summary = "更新用户")
    @RequiresPermission(code = "system:user:update", name = "更新用户")
    @PutMapping("/update-user/{id}")
    public ApiResponse<UserVO> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDTO dto,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // DTO 转 Entity
        User user = new User();
        user.setId(id);
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRealName(dto.getRealName());
        user.setAvatar(dto.getAvatar());
        user.setDeptId(dto.getDepartmentId());
        user.setStatus(dto.getStatus());
        // 设置更新人ID，用于审计追踪
        user.setUpdatedBy(userId != null ? userId : 1L);
        // 调用服务层执行更新操作
        User updated = userService.updateUser(user);
        // 清除缓存
        userCache.evict(tenantId, id);
        return ApiResponse.success(convertToVO(updated));
    }

    /**
     * 删除用户（软删除）
     * <p>
     * 逻辑删除用户，不会真正从数据库中移除记录，而是将 deleted 字段标记为1。
     * 已删除的用户在查询时会被自动过滤。
     * </p>
     *
     * @param id       要删除的用户ID，通过URL路径传入
     * @param tenantId 租户ID
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "删除用户")
    @RequiresPermission(code = "system:user:delete", name = "删除用户")
    @DeleteMapping("/delete-user/{id}")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        // 调用服务层执行软删除
        userService.deleteUser(id);
        // 清除缓存
        userCache.evict(tenantId, id);
        return ApiResponse.success();
    }

    /**
     * 启用用户
     *
     * @param id       要启用的用户ID
     * @param tenantId 租户ID
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "启用用户")
    @RequiresPermission(code = "system:user:enable", name = "启用用户")
    @PostMapping("/enable-user/{id}")
    public ApiResponse<Void> enableUser(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        userService.updateUserStatus(id, 1);
        userCache.evict(tenantId, id);
        return ApiResponse.success();
    }

    /**
     * 禁用用户
     *
     * @param id       要禁用的用户ID
     * @param tenantId 租户ID
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "禁用用户")
    @RequiresPermission(code = "system:user:disable", name = "禁用用户")
    @PostMapping("/disable-user/{id}")
    public ApiResponse<Void> disableUser(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        userService.updateUserStatus(id, 0);
        userCache.evict(tenantId, id);
        return ApiResponse.success();
    }

    /**
     * 获取指定用户的地址列表
     *
     * @param id 用户ID，通过URL路径传入
     * @return 该用户的所有收货地址列表
     */
    @RequiresPermission(code = "system:user:detail", name = "查询用户详情")
    @Operation(summary = "获取用户地址列表")
    @GetMapping("/{id}/addresses")
    public ApiResponse<List<UserAddress>> getUserAddresses(@PathVariable Long id) {
        List<UserAddress> addresses = userService.getUserAddresses(id);
        return ApiResponse.success(addresses);
    }

    /**
     * 为指定用户新增收货地址
     *
     * @param id       用户ID
     * @param address  地址信息
     * @param tenantId 租户ID
     * @return 新增成功后的地址信息
     */
    @Operation(summary = "添加用户地址")
    @PostMapping("/{id}/addresses")
    public ApiResponse<UserAddress> addAddress(
            @PathVariable Long id,
            @RequestBody UserAddress address,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        address.setUserId(id);
        address.setTenantId(tenantId);
        UserAddress created = userService.addAddress(address);
        return ApiResponse.success(created);
    }

    /**
     * 设置默认收货地址
     *
     * @param id        用户ID
     * @param addressId 要设为默认的地址ID
     * @return 空响应体
     */
    @Operation(summary = "设置默认地址")
    @PostMapping("/{id}/addresses/{addressId}/default")
    public ApiResponse<Void> setDefaultAddress(
            @PathVariable Long id,
            @PathVariable Long addressId) {
        userService.setDefaultAddress(id, addressId);
        return ApiResponse.success();
    }

    /**
     * 将 User Entity 转换为 UserVO
     */
    private UserVO convertToVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setTenantId(user.getTenantId());
        vo.setUsername(user.getUserName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setDepartmentId(user.getDeptId());
        vo.setStatus(user.getStatus());
        vo.setCreatedBy(user.getCreatedBy());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedBy(user.getUpdatedBy());
        vo.setUpdatedAt(user.getUpdatedAt());
        // 查询用户角色名称列表
        try {
            List<Role> roles = roleService.getRolesByUserId(user.getId());
            vo.setRoleNames(roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            // 角色查询失败不影响主流程
            vo.setRoleNames(List.of());
        }
        return vo;
    }
}
