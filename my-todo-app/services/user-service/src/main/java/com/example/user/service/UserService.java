package com.example.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.user.entity.User;
import com.example.user.entity.UserAddress;

import java.util.List;

/**
 * 用户业务逻辑服务接口
 */
public interface UserService extends IService<User> {

    Page<User> getUserPage(Long tenantId, int page, int size,
                           String username, String realName, Long deptId, Integer status);

    User getUserById(Long userId);

    User getUserWithProfile(Long userId);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long userId);

    void updateUserStatus(Long userId, Integer status);

    List<UserAddress> getUserAddresses(Long userId);

    UserAddress addAddress(UserAddress address);

    void setDefaultAddress(Long userId, Long addressId);
}
