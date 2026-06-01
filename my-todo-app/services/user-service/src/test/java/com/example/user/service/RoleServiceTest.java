package com.example.user.service;

import com.example.user.entity.Role;
import com.example.user.entity.UserRole;
import com.example.user.mapper.RoleMapper;
import com.example.user.mapper.UserRoleMapper;
import com.example.user.service.impl.RoleServiceImpl;
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
class RoleServiceTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("管理员");
        testRole.setRoleCode("ADMIN");
        testRole.setDescription("系统管理员角色");
        testRole.setStatus(1);
        testRole.setIsSystem(0);
        testRole.setTenantId(1L);
        testRole.setDeleted(0);
    }

    @Test
    void testCreateRole_Success() {
        // getOne returns null = no existing role with same code
        when(roleService.getOne(any())).thenReturn(null);
        when(roleService.save(any(Role.class))).thenReturn(true);

        Role newRole = new Role();
        newRole.setRoleName("普通用户");
        newRole.setRoleCode("USER");
        newRole.setTenantId(1L);

        Role result = roleService.createRole(newRole);

        assertNotNull(result);
        assertEquals("USER", result.getRoleCode());
        assertEquals(1, result.getStatus());  // createRole sets status=1
        assertEquals(0, result.getIsSystem()); // createRole sets isSystem=0
    }

    @Test
    void testCreateRole_DuplicateRoleCode_ThrowsException() {
        // getOne returns existing role = duplicate code
        when(roleService.getOne(any())).thenReturn(testRole);

        Role newRole = new Role();
        newRole.setRoleCode("ADMIN");
        newRole.setTenantId(1L);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roleService.createRole(newRole)
        );
        assertTrue(exception.getMessage().contains("角色编码已存在"));
    }

    @Test
    void testUpdateRole_Success() {
        when(roleService.getById(1L)).thenReturn(testRole);
        when(roleService.updateById(any(Role.class))).thenReturn(true);

        testRole.setRoleName("超级管理员");
        Role result = roleService.updateRole(testRole);

        assertNotNull(result);
        verify(roleService).updateById(testRole);
    }

    @Test
    void testUpdateRole_RoleNotFound_ThrowsException() {
        when(roleService.getById(999L)).thenReturn(null);

        Role missingRole = new Role();
        missingRole.setId(999L);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roleService.updateRole(missingRole)
        );
        assertTrue(exception.getMessage().contains("角色不存在"));
    }

    @Test
    void testUpdateRole_SystemRole_PreservesRoleCode() {
        testRole.setIsSystem(1); // Mark as system role
        testRole.setRoleCode("ORIGINAL_CODE");

        when(roleService.getById(1L)).thenReturn(testRole);
        when(roleService.updateById(any(Role.class))).thenReturn(true);

        Role updateRequest = new Role();
        updateRequest.setId(1L);
        updateRequest.setRoleCode("MODIFIED_CODE"); // Try to change code
        updateRequest.setRoleName("新名称");

        roleService.updateRole(updateRequest);

        // System role should preserve its original code
        assertEquals("ORIGINAL_CODE", updateRequest.getRoleCode());
    }

    @Test
    void testDeleteRole_Success() {
        when(roleService.getById(1L)).thenReturn(testRole);
        when(roleService.updateById(any(Role.class))).thenReturn(true);

        roleService.deleteRole(1L);

        assertEquals(1, testRole.getDeleted());
        verify(roleService).updateById(testRole);
    }

    @Test
    void testDeleteRole_SystemRole_ThrowsException() {
        testRole.setIsSystem(1); // System role cannot be deleted

        when(roleService.getById(1L)).thenReturn(testRole);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> roleService.deleteRole(1L)
        );
        assertTrue(exception.getMessage().contains("系统角色不允许删除"));
    }

    @Test
    void testDeleteRole_NotFound_DoesNothing() {
        when(roleService.getById(999L)).thenReturn(null);

        roleService.deleteRole(999L);

        verify(roleService, never()).updateById(any());
    }

    @Test
    void testAssignRolesToUser_NewAssignment() {
        // No existing user-role mapping
        when(userRoleMapper.selectOne(any())).thenReturn(null);
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        List<Long> roleIds = Arrays.asList(1L, 2L);
        roleService.assignRolesToUser(10L, roleIds, 1L);

        // Should insert 2 new user-role records
        verify(userRoleMapper, times(2)).insert(any(UserRole.class));
    }

    @Test
    void testAssignRolesToUser_AlreadyAssigned_SkipsDuplicate() {
        UserRole existingMapping = new UserRole();
        existingMapping.setUserId(10L);
        existingMapping.setRoleId(1L);

        // First role already assigned, second role not assigned
        when(userRoleMapper.selectOne(any()))
            .thenReturn(existingMapping)  // Role 1 already exists
            .thenReturn(null);            // Role 2 is new
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        List<Long> roleIds = Arrays.asList(1L, 2L);
        roleService.assignRolesToUser(10L, roleIds, 1L);

        // Should only insert 1 record (for role 2, since role 1 already assigned)
        verify(userRoleMapper, times(1)).insert(any(UserRole.class));
    }

    @Test
    void testRemoveRolesFromUser() {
        when(userRoleMapper.delete(any())).thenReturn(2);

        List<Long> roleIds = Arrays.asList(1L, 2L);
        roleService.removeRolesFromUser(10L, roleIds, 1L);

        verify(userRoleMapper).delete(any());
    }

    @Test
    void testGetRolesByUserId() {
        UserRole userRole1 = new UserRole();
        userRole1.setUserId(10L);
        userRole1.setRoleId(1L);

        UserRole userRole2 = new UserRole();
        userRole2.setUserId(10L);
        userRole2.setRoleId(2L);

        when(userRoleMapper.selectList(any()))
            .thenReturn(Arrays.asList(userRole1, userRole2));
        when(roleService.list(any())).thenReturn(Arrays.asList(testRole));

        List<Role> result = roleService.getRolesByUserId(10L);

        assertNotNull(result);
        verify(userRoleMapper).selectList(any());
        verify(roleService).list(any());
    }

    @Test
    void testGetRolesByUserId_NoRoles() {
        when(userRoleMapper.selectList(any())).thenReturn(List.of());

        List<Role> result = roleService.getRolesByUserId(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        // Should not call roleService.list since no user-role mappings exist
        verify(roleService, never()).list(any());
    }
}
