package com.example.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.user.entity.User;
import com.example.user.entity.UserProfile;
import com.example.user.entity.UserAddress;
import com.example.user.mapper.UserMapper;
import com.example.user.mapper.UserProfileMapper;
import com.example.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务逻辑服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl<UserMapper, User>，自动获得基础的 Service 层 CRUD 方法。
 * 提供用户管理的核心业务逻辑，包括：
 * <ul>
 *     <li>用户的分页查询（支持多条件筛选）</li>
 *     <li>用户的创建、更新、删除（软删除）</li>
 *     <li>用户状态的启用/禁用切换</li>
 *     <li>用户收货地址的管理（增删查、设置默认地址）</li>
 *     <li>用户扩展信息的查询</li>
 * </ul>
 * </p>
 * <p>
 * 使用 @Transactional 注解确保数据库操作的原子性，
 * 使用 @Slf4j 注解提供日志记录能力，便于运行时排查问题。
 * </p>
 *
 * @see ServiceImpl MyBatis-Plus 提供的基础 Service 实现类
 * @see UserMapper 用户数据访问接口
 * @see User 用户实体类
 */
@Slf4j
@Service
@RequiredArgsConstructor  // 通过构造器自动注入 final 字段，保证依赖不可变
public class UserService extends ServiceImpl<UserMapper, User> {

    /** 用户扩展信息 Mapper，用于操作 sys_user_profile 表 */
    private final UserProfileMapper userProfileMapper;

    /** 用户收货地址 Mapper，用于操作 sys_user_address 表 */
    private final UserAddressMapper userAddressMapper;

    /**
     * 分页查询用户列表
     * <p>
     * 根据租户ID进行数据隔离，支持按用户名（模糊）、真实姓名（模糊）、
     * 部门ID（精确）和状态（精确）进行条件筛选。
     * 查询结果按创建时间降序排列，返回分页数据。
     * </p>
     *
     * @param tenantId 租户ID，用于多租户数据隔离
     * @param page     当前页码（从1开始）
     * @param size     每页记录数
     * @param username 用户名（可选，模糊匹配）
     * @param realName 真实姓名（可选，模糊匹配）
     * @param deptId   部门ID（可选，精确匹配）
     * @param status   用户状态（可选，精确匹配：0-禁用，1-启用）
     * @return 分页结果，包含用户列表和分页元信息
     */
    public Page<User> getUserPage(Long tenantId, int page, int size,
                                   String username, String realName, Long deptId, Integer status) {
        // 构建 Lambda 条件构造器，类型安全，避免字段名硬编码
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 必要条件：租户ID匹配 + 未删除的记录
        wrapper.eq(User::getTenantId, tenantId)
               .eq(User::getDeleted, 0);
        // 可选条件：用户名模糊查询（LIKE %username%）
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        // 可选条件：真实姓名模糊查询（LIKE %realName%）
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(User::getRealName, realName);
        }
        // 可选条件：部门ID精确匹配
        if (deptId != null) {
            wrapper.eq(User::getDeptId, deptId);
        }
        // 可选条件：状态精确匹配
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        // 按创建时间降序排列，最新创建的用户排在前面
        wrapper.orderByDesc(User::getCreatedAt);
        // 执行分页查询，new Page<>(page, size) 封装分页参数
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据用户ID获取用户基本信息
     *
     * @param userId 用户ID
     * @return 用户对象，如果不存在返回 null
     */
    public User getUserById(Long userId) {
        // 调用 MyBatis-Plus ServiceImpl 提供的 getById 方法，根据主键查询
        return getById(userId);
    }

    /**
     * 获取用户详情（含扩展信息）
     * <p>
     * 查询用户基本信息的同时，关联查询 sys_user_profile 表中的扩展信息。
     * 当前实现仅查询了 profile 数据，后续可通过 DTO 或扩展字段将两者合并返回给前端。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户基本信息（扩展信息暂未合并到返回结果中）
     */
    public User getUserWithProfile(Long userId) {
        // 查询用户基本信息
        User user = getById(userId);
        if (user != null) {
            // 查询用户的扩展详细信息（一对一关系，userId 即为 profile 表的主键）
            UserProfile profile = userProfileMapper.selectById(userId);
            // TODO: 可以通过扩展字段或 DTO 将 profile 信息合并到返回结果中
        }
        return user;
    }

    /**
     * 创建新用户
     * <p>
     * 在创建前会先校验同一租户下用户名是否已存在，如果存在则抛出异常。
     * 使用事务注解确保操作的原子性。
     * </p>
     *
     * @param user 待创建的用户对象（tenantId、createdBy 应已由 Controller 层设置）
     * @return 创建成功后的用户对象（含自动填充的字段）
     * @throws IllegalArgumentException 如果同一租户下用户名已存在
     */
    @Transactional  // 开启数据库事务，出现异常时自动回滚
    public User createUser(User user) {
        // 校验用户名唯一性：在同一租户下查询是否已存在相同的用户名
        User existing = getOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getTenantId, user.getTenantId())  // 限定当前租户
                .eq(User::getUsername, user.getUsername())   // 匹配相同用户名
                .eq(User::getDeleted, 0)                     // 排除已删除的记录
        );
        // 如果存在同名用户，抛出业务异常
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在: " + user.getUsername());
        }
        // 校验通过，保存用户到数据库
        save(user);
        // 记录操作日志，便于审计追踪
        log.info("创建用户: {}", user.getUsername());
        return user;
    }

    /**
     * 更新用户信息
     * <p>
     * 根据用户ID更新用户信息，仅更新请求体中传入的非空字段。
     * 使用事务注解确保操作的原子性。
     * </p>
     *
     * @param user 待更新的用户对象（必须包含 id 字段）
     * @return 更新后的用户对象
     */
    @Transactional  // 开启数据库事务，出现异常时自动回滚
    public User updateUser(User user) {
        // 记录更新前的用户信息
        log.info("更新用户 - ID: {}, username: {}, email: {}, phone: {}, realName: {}, status: {}",
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone(), user.getRealName(), user.getStatus());

        // 先查询用户是否存在
        User existingUser = getById(user.getId());
        if (existingUser == null) {
            log.warn("用户不存在，ID: {}", user.getId());
            return user;
        }
        log.info("更新前用户数据: username={}, email={}, phone={}, realName={}",
                existingUser.getUsername(), existingUser.getEmail(), existingUser.getPhone(), existingUser.getRealName());

        // 调用 MyBatis-Plus 的 updateById 方法，根据主键更新非空字段
        boolean success = updateById(user);

        // 记录更新结果
        log.info("更新结果: success={}, 影响行数: {}", success, success ? "1" : "0");

        // 重新查询验证
        User updated = getById(user.getId());
        log.info("更新后用户数据: username={}, email={}, phone={}, realName={}",
                updated.getUsername(), updated.getEmail(), updated.getPhone(), updated.getRealName());

        return user;
    }

    /**
     * 删除用户（软删除）
     * <p>
     * 不会真正从数据库中删除记录，而是将 deleted 字段设置为1。
     * 后续查询时，MyBatis-Plus 的 @TableLogic 注解会自动过滤已删除的记录。
     * 如果用户不存在，则不做任何操作。
     * </p>
     *
     * @param userId 待删除的用户ID
     */
    @Transactional  // 开启数据库事务，确保删除操作的原子性
    public void deleteUser(Long userId) {
        // 先查询用户是否存在
        User user = getById(userId);
        if (user != null) {
            // 将逻辑删除标志设为1（已删除）
            user.setDeleted(1);
            // 更新到数据库
            updateById(user);
            // 记录操作日志
            log.info("删除用户: {}", user.getUsername());
        }
    }

    /**
     * 更新用户状态（启用/禁用）
     * <p>
     * 切换用户的使用状态，禁用后用户将无法登录和访问系统资源。
     * 如果用户不存在，则不做任何操作。
     * </p>
     *
     * @param userId 用户ID
     * @param status 目标状态：0-禁用，1-启用
     */
    @Transactional  // 开启数据库事务，确保状态更新的原子性
    public void updateUserStatus(Long userId, Integer status) {
        // 先查询用户是否存在
        User user = getById(userId);
        if (user != null) {
            // 设置新的状态值
            user.setStatus(status);
            // 更新到数据库
            updateById(user);
            // 记录操作日志，包含用户ID和新状态
            log.info("更新用户状态: userId={}, status={}", userId, status);
        }
    }

    /**
     * 获取用户的所有有效收货地址
     * <p>
     * 查询指定用户下所有未删除的收货地址，默认地址排在最前面。
     * </p>
     *
     * @param userId 用户ID
     * @return 该用户的收货地址列表，默认地址排在前面
     */
    public List<UserAddress> getUserAddresses(Long userId) {
        return userAddressMapper.selectList(
            new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)     // 查询该用户的地址
                .eq(UserAddress::getDeleted, 0)          // 仅查询未删除的记录
                .orderByDesc(UserAddress::getIsDefault)  // 默认地址（isDefault=1）排在前面
        );
    }

    /**
     * 为用户新增收货地址
     * <p>
     * 如果新增的地址被设置为默认地址（isDefault=1），会先将该用户下已有的
     * 默认地址取消，确保每个用户始终只有一个默认地址。
     * </p>
     *
     * @param address 待新增的地址对象（userId、tenantId 应已由 Controller 层设置）
     * @return 新增成功后的地址对象（含自动生成的ID）
     */
    @Transactional  // 开启数据库事务，确保取消旧默认地址和新增新地址的原子性
    public UserAddress addAddress(UserAddress address) {
        // 如果新地址被设置为默认地址，需要先取消该用户下其他已有的默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            // 将该用户下所有当前默认地址的 isDefault 字段设为0（取消默认）
            userAddressMapper.update(null,
                new LambdaUpdateWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, address.getUserId())  // 限定当前用户
                    .eq(UserAddress::getIsDefault, 1)                 // 查找当前的默认地址
                    .set(UserAddress::getIsDefault, 0)                // 将其设为非默认
            );
        }
        // 插入新的地址记录到数据库
        userAddressMapper.insert(address);
        return address;
    }

    /**
     * 设置默认收货地址
     * <p>
     * 分两步执行：
     * <ol>
     *     <li>取消该用户下所有当前的默认地址（将 isDefault 设为0）</li>
     *     <li>将指定地址设为默认（将 isDefault 设为1）</li>
     * </ol>
     * 同时会校验目标地址是否归属于该用户，防止越权操作。
     * 使用事务注解确保两步操作的原子性。
     * </p>
     *
     * @param userId    用户ID
     * @param addressId 要设为默认的地址ID
     */
    @Transactional  // 开启数据库事务，确保取消旧默认和设置新默认的原子性
    public void setDefaultAddress(Long userId, Long addressId) {
        // 第一步：取消该用户下所有当前的默认地址
        userAddressMapper.update(null,
            new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)     // 限定当前用户
                .eq(UserAddress::getIsDefault, 1)       // 查找所有默认地址
                .set(UserAddress::getIsDefault, 0)      // 取消默认标记
        );

        // 第二步：将指定的地址设置为默认
        UserAddress address = userAddressMapper.selectById(addressId);
        // 安全校验：地址必须存在，且必须归属于该用户（防止越权操作）
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);                // 设为默认地址
            userAddressMapper.updateById(address);  // 更新到数据库
        }
    }
}
