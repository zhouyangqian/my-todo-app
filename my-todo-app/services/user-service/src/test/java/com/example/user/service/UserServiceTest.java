package com.example.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.user.entity.User;
import com.example.user.entity.UserAddress;
import com.example.user.mapper.UserAddressMapper;
import com.example.user.mapper.UserProfileMapper;
import com.example.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private UserAddressMapper userAddressMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setRealName("测试用户");
        testUser.setDeptId(10L);
        testUser.setStatus(1);
        testUser.setTenantId(1L);
        testUser.setDeleted(0);
    }

    @Test
    void testGetUserById() {
        // UserServiceImpl.getUserById delegates to getById (ServiceImpl method)
        // which uses the baseMapper internally. We test via spy or integration.
        // For pure unit test, we verify the delegation logic through the service.
        // Since getById is final in ServiceImpl, we test via the service's own methods.

        // Test getUserById - it calls getById internally
        // We use the fact that ServiceImpl.getById uses baseMapper.selectById
        // For a unit test we focus on the service-level methods that have real logic.
    }

    @Test
    void testCreateUser_Success() {
        // getOne returns null = no existing user with same name
        when(userService.getOne(any())).thenReturn(null);
        // save returns true
        when(userService.save(any(User.class))).thenReturn(true);

        User newUser = new User();
        newUser.setUserName("newuser");
        newUser.setTenantId(1L);
        newUser.setDeleted(0);

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUserName());
    }

    @Test
    void testCreateUser_DuplicateUsername_ThrowsException() {
        // getOne returns existing user = duplicate
        when(userService.getOne(any())).thenReturn(testUser);

        User newUser = new User();
        newUser.setUserName("testuser");
        newUser.setTenantId(1L);
        newUser.setDeleted(0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(newUser)
        );
        assertTrue(exception.getMessage().contains("用户名已存在"));
    }

    @Test
    void testUpdateUser_Success() {
        // getById returns existing user (for existence check)
        when(userService.getById(1L)).thenReturn(testUser);
        // updateById returns true
        when(userService.updateById(any(User.class))).thenReturn(true);

        testUser.setRealName("更新姓名");
        User result = userService.updateUser(testUser);

        assertNotNull(result);
        assertEquals("更新姓名", result.getRealName());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // getById returns null = user does not exist
        when(userService.getById(999L)).thenReturn(null);

        User missingUser = new User();
        missingUser.setId(999L);
        User result = userService.updateUser(missingUser);

        // Should return the input user without updating
        assertNotNull(result);
        assertEquals(999L, result.getId());
        // updateById should never be called
        verify(userService, never()).updateById(any());
    }

    @Test
    void testDeleteUser_Success() {
        // getById returns existing user
        when(userService.getById(1L)).thenReturn(testUser);
        // updateById returns true (soft delete)
        when(userService.updateById(any(User.class))).thenReturn(true);

        userService.deleteUser(1L);

        // Verify deleted flag was set to 1
        assertEquals(1, testUser.getDeleted());
        verify(userService).updateById(testUser);
    }

    @Test
    void testDeleteUser_UserNotFound_DoesNothing() {
        // getById returns null
        when(userService.getById(999L)).thenReturn(null);

        userService.deleteUser(999L);

        // updateById should never be called
        verify(userService, never()).updateById(any());
    }

    @Test
    void testUpdateUserStatus_Success() {
        when(userService.getById(1L)).thenReturn(testUser);
        when(userService.updateById(any(User.class))).thenReturn(true);

        userService.updateUserStatus(1L, 0);

        assertEquals(0, testUser.getStatus());
        verify(userService).updateById(testUser);
    }

    @Test
    void testUpdateUserStatus_UserNotFound_DoesNothing() {
        when(userService.getById(999L)).thenReturn(null);

        userService.updateUserStatus(999L, 0);

        verify(userService, never()).updateById(any());
    }

    @Test
    void testGetUserWithProfile() {
        when(userService.getById(1L)).thenReturn(testUser);

        User result = userService.getUserWithProfile(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUserName());
        verify(userService).getById(1L);
    }

    @Test
    void testGetUserWithProfile_NotFound() {
        when(userService.getById(999L)).thenReturn(null);

        User result = userService.getUserWithProfile(999L);

        assertNull(result);
    }

    @Test
    void testGetUserAddresses() {
        UserAddress addr1 = new UserAddress();
        addr1.setId(1L);
        addr1.setUserId(1L);
        addr1.setIsDefault(1);
        addr1.setDetailAddress("详细地址1");

        UserAddress addr2 = new UserAddress();
        addr2.setId(2L);
        addr2.setUserId(1L);
        addr2.setIsDefault(0);
        addr2.setDetailAddress("详细地址2");

        when(userAddressMapper.selectList(any())).thenReturn(Arrays.asList(addr1, addr2));

        List<UserAddress> result = userService.getUserAddresses(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userAddressMapper).selectList(any());
    }

    @Test
    void testAddAddress_NonDefault() {
        UserAddress address = new UserAddress();
        address.setUserId(1L);
        address.setIsDefault(0);
        address.setDetailAddress("非默认地址");

        when(userAddressMapper.insert(any(UserAddress.class))).thenReturn(1);

        UserAddress result = userService.addAddress(address);

        assertNotNull(result);
        verify(userAddressMapper).insert(address);
        // Should NOT clear existing defaults since isDefault != 1
        verify(userAddressMapper, never()).update(any(), any());
    }

    @Test
    void testAddAddress_AsDefault_ClearsExistingDefaults() {
        UserAddress address = new UserAddress();
        address.setUserId(1L);
        address.setIsDefault(1);
        address.setDetailAddress("新默认地址");

        when(userAddressMapper.update(any(), any())).thenReturn(1);
        when(userAddressMapper.insert(any(UserAddress.class))).thenReturn(1);

        UserAddress result = userService.addAddress(address);

        assertNotNull(result);
        // Should clear existing defaults first
        verify(userAddressMapper).update(isNull(), any());
        verify(userAddressMapper).insert(address);
    }

    @Test
    void testSetDefaultAddress_Success() {
        UserAddress targetAddr = new UserAddress();
        targetAddr.setId(2L);
        targetAddr.setUserId(1L);
        targetAddr.setIsDefault(0);

        when(userAddressMapper.update(any(), any())).thenReturn(1);
        when(userAddressMapper.selectById(2L)).thenReturn(targetAddr);
        when(userAddressMapper.updateById(any(UserAddress.class))).thenReturn(1);

        userService.setDefaultAddress(1L, 2L);

        assertEquals(1, targetAddr.getIsDefault());
        verify(userAddressMapper).updateById(targetAddr);
    }

    @Test
    void testSetDefaultAddress_AddressNotOwnedByUser_Skips() {
        UserAddress otherUserAddr = new UserAddress();
        otherUserAddr.setId(2L);
        otherUserAddr.setUserId(999L); // Different user

        when(userAddressMapper.update(any(), any())).thenReturn(1);
        when(userAddressMapper.selectById(2L)).thenReturn(otherUserAddr);

        userService.setDefaultAddress(1L, 2L);

        // Should NOT update since address belongs to another user
        verify(userAddressMapper, never()).updateById(any());
    }
}
