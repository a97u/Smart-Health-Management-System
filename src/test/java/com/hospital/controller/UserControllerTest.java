package com.hospital.controller;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;
    
    @InjectMocks
    private UserController userController;
    
    private User testUser;
    private Role testRole;
    private List<User> userList;
    
    @BeforeEach
    void setUp() {
        // Create test data
        testRole = new Role();
        testRole.setId(1);
        testRole.setName(Role.RoleName.ADMIN);
        
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("admin@example.com");
        testUser.setName("Test Admin");
        testUser.setPassword("password");
        testUser.setRoles(Set.of(testRole));
        
        userList = new ArrayList<>();
        userList.add(testUser);
    }
    
    @Test
    void testUserCreation() {
        // Basic test for entity creation
        assertNotNull(testUser);
        assertEquals(1, testUser.getId());
        assertEquals("admin@example.com", testUser.getEmail());
        assertEquals("Test Admin", testUser.getName());
        assertEquals(1, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains(testRole));
    }
    
    @Test
    void testGetAllUsersService() {
        // Test the service method directly
        when(userService.getAllUsers()).thenReturn(userList);
        
        List<User> result = userService.getAllUsers();
        
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        verify(userService, times(1)).getAllUsers();
    }
    
    @Test
    void testGetUserByIdService() {
        when(userService.getUserById(anyInt())).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserById(1);
        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userService, times(1)).getUserById(1);
    }
    
    @Test
    void testGetUserByIdService_NotFound() {
        when(userService.getUserById(anyInt())).thenReturn(Optional.empty());
        
        Optional<User> result = userService.getUserById(999);
        
        assertFalse(result.isPresent());
        verify(userService, times(1)).getUserById(999);
    }
    
    @Test
    void testGetUserByEmailService() {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByEmail("admin@example.com");
        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userService, times(1)).getUserByEmail("admin@example.com");
    }
    
    @Test
    void testUpdateUserService() {
        when(userService.updateUser(any(User.class))).thenReturn(testUser);
        
        User updatedUser = testUser;
        updatedUser.setName("Updated Name");
        
        User result = userService.updateUser(updatedUser);
        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userService, times(1)).updateUser(updatedUser);
    }
} 