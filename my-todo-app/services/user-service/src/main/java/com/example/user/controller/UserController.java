package com.example.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.user.entity.User;
import com.example.user.entity.UserAddress;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "分页查询用户")
    @GetMapping("/get-user-page")
    public ApiResponse<PageResult<User>> getUserPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        // 调用服务层执行分页查询
        Page<User> result = userService.getUserPage(tenantId, page, size, username, realName, deptId, status);
        // 转换为 PageResult 返回
        PageResult<User> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 根据用户ID获取用户详情
     *
     * @param id 用户ID，通过URL路径传入
     * @return 用户详细信息
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/get-user/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        // 根据主键查询用户信息
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 创建新用户
     * <p>
     * 从请求头中提取租户ID和当前操作用户ID，自动填充到用户对象中，
     * 保证数据归属和审计信息的完整性。
     * </p>
     *
     * @param user     用户信息，通过请求体以JSON格式传入
     * @param tenantId 租户ID，从请求头中获取，用于多租户数据隔离
     * @param userId   当前操作用户ID，从请求头中获取，记录创建人
     * @return 创建成功后的用户信息（含自动生成的ID）
     */
    @Operation(summary = "创建用户")
    @PostMapping("/create-user")
    public ApiResponse<User> createUser(
            @RequestBody User user,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        // 设置租户ID，确保用户归属于当前租户
        user.setTenantId(tenantId);
        // 设置创建人ID，用于审计追踪
        user.setCreatedBy(userId);
        // 调用服务层创建用户（会校验用户名唯一性）
        User created = userService.createUser(user);
        return ApiResponse.success(created);
    }

    /**
     * 更新用户信息
     * <p>
     * 通过URL路径指定要更新的用户ID，从请求头获取当前操作用户ID
     * 用于记录更新人信息。
     * </p>
     *
     * @param id     要更新的用户ID，通过URL路径传入
     * @param user   更新后的用户信息，通过请求体以JSON格式传入
     * @param userId 当前操作用户ID，从请求头中获取，记录更新人
     * @return 更新后的用户完整信息
     */
    @Operation(summary = "更新用户")
    @PutMapping("/update-user/{id}")
    public ApiResponse<User> updateUser(
            @PathVariable Long id,
            @RequestBody User user,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 将路径中的ID设置到用户对象，确保更新的是目标用户
        user.setId(id);
        // 设置更新人ID，用于审计追踪（如果请求头中没有则使用默认值）
        user.setUpdatedBy(userId != null ? userId : 1L);
        // 调用服务层执行更新操作
        User updated = userService.updateUser(user);
        return ApiResponse.success(updated);
    }

    /**
     * 删除用户（软删除）
     * <p>
     * 逻辑删除用户，不会真正从数据库中移除记录，而是将 deleted 字段标记为1。
     * 已删除的用户在查询时会被自动过滤。
     * </p>
     *
     * @param id 要删除的用户ID，通过URL路径传入
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete-user/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 调用服务层执行软删除
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    /**
     * 启用用户
     * <p>
     * 将用户状态设置为1（启用），启用后用户可以正常登录和使用系统。
     * </p>
     *
     * @param id 要启用的用户ID，通过URL路径传入
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "启用用户")
    @PostMapping("/enable-user/{id}")
    public ApiResponse<Void> enableUser(@PathVariable Long id) {
        // 将用户状态设置为1（启用）
        userService.updateUserStatus(id, 1);
        return ApiResponse.success();
    }

    /**
     * 禁用用户
     * <p>
     * 将用户状态设置为0（禁用），禁用后用户无法登录和访问系统资源。
     * </p>
     *
     * @param id 要禁用的用户ID，通过URL路径传入
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "禁用用户")
    @PostMapping("/disable-user/{id}")
    public ApiResponse<Void> disableUser(@PathVariable Long id) {
        // 将用户状态设置为0（禁用）
        userService.updateUserStatus(id, 0);
        return ApiResponse.success();
    }

    /**
     * 获取指定用户的地址列表
     * <p>
     * 查询某个用户下所有未删除的收货地址，默认地址排在最前面。
     * </p>
     *
     * @param id 用户ID，通过URL路径传入
     * @return 该用户的所有收货地址列表
     */
    @Operation(summary = "获取用户地址列表")
    @GetMapping("/{id}/addresses")
    public ApiResponse<List<UserAddress>> getUserAddresses(@PathVariable Long id) {
        // 查询该用户的所有有效地址
        List<UserAddress> addresses = userService.getUserAddresses(id);
        return ApiResponse.success(addresses);
    }

    /**
     * 为指定用户新增收货地址
     * <p>
     * 自动填充用户ID和租户ID，如果该地址被设置为默认地址，
     * 服务层会自动取消该用户的其他默认地址。
     * </p>
     *
     * @param id       用户ID，通过URL路径传入
     * @param address  地址信息，通过请求体以JSON格式传入
     * @param tenantId 租户ID，从请求头中获取，用于多租户数据隔离
     * @return 新增成功后的地址信息（含自动生成的ID）
     */
    @Operation(summary = "添加用户地址")
    @PostMapping("/{id}/addresses")
    public ApiResponse<UserAddress> addAddress(
            @PathVariable Long id,
            @RequestBody UserAddress address,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        // 设置地址归属的用户ID
        address.setUserId(id);
        // 设置租户ID，确保数据隔离
        address.setTenantId(tenantId);
        // 调用服务层新增地址（如果为默认地址会自动处理其他地址的默认状态）
        UserAddress created = userService.addAddress(address);
        return ApiResponse.success(created);
    }

    /**
     * 设置默认收货地址
     * <p>
     * 将指定地址设置为默认地址，同时自动取消该用户下其他地址的默认状态，
     * 确保每个用户只有一个默认收货地址。
     * </p>
     *
     * @param id        用户ID，通过URL路径传入
     * @param addressId 要设为默认的地址ID，通过URL路径传入
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "设置默认地址")
    @PostMapping("/{id}/addresses/{addressId}/default")
    public ApiResponse<Void> setDefaultAddress(
            @PathVariable Long id,
            @PathVariable Long addressId) {
        // 调用服务层设置默认地址（会先取消已有的默认地址）
        userService.setDefaultAddress(id, addressId);
        return ApiResponse.success();
    }
}
