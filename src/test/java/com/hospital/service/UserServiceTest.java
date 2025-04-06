package com.hospital.service;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.RoleRepository;
import com.hospital.repository.UserRepository;
import com.hospital.repository.NurseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private NurseRepository nurseRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private Set<Role> roleSet;

    @BeforeEach
    void setUp() {
        // Create test data
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
        testUser.setCreatedAt(LocalDateTime.now().minusDays(60)); // 60 days old

        testRole = new Role();
        testRole.setId(1);
        testRole.setName(Role.RoleName.ADMIN);
        
        roleSet = new HashSet<>();
        roleSet.add(testRole);
        testUser.setRoles(roleSet);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        when(userRepository.findAll()).thenReturn(userList);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testGetUserByEmail() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testExistsByEmail() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void testRegisterUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(any(Role.RoleName.class))).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        // Act
        User result = userService.registerUser(newUser, Role.RoleName.ADMIN);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(passwordEncoder, times(1)).encode("password");
        verify(roleRepository, times(1)).findByName(Role.RoleName.ADMIN);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserWithPatientRole() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        Role patientRole = new Role();
        patientRole.setId(2);
        patientRole.setName(Role.RoleName.PATIENT);
        when(roleRepository.findByName(eq(Role.RoleName.PATIENT))).thenReturn(Optional.of(patientRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User newUser = new User();
        newUser.setEmail("patient@example.com");
        newUser.setPassword("password");

        // Act
        User result = userService.registerUser(newUser, Role.RoleName.PATIENT);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository, times(1)).existsByEmail("patient@example.com");
        verify(passwordEncoder, times(1)).encode("password");
        verify(roleRepository, times(1)).findByName(Role.RoleName.PATIENT);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        User newUser = new User();
        newUser.setEmail("existing@example.com");
        newUser.setPassword("password");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(newUser, Role.RoleName.PATIENT);
        });
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser() {
        // Create a new instance of UserService for this test
        UserService localUserService = new UserService();
        
        // Manually inject the mocks
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(localUserService, "userRepository", userRepository);
        ReflectionTestUtils.setField(localUserService, "passwordEncoder", passwordEncoder);
        
        // Create a partial mock that will use real methods but with mocked dependencies
        UserService spyUserService = spy(localUserService);
        
        // Set up the stub for getUserById to avoid calling the repository directly
        doReturn(Optional.of(testUser)).when(spyUserService).getUserById(1);
        
        // Set up the repository save method
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Set up the password encoder
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Create the user to update
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setName("Updated Name");
        updatedUser.setPassword("newPassword");

        // Act
        User result = spyUserService.updateUser(updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        
        // Capture the saved user to verify the password was encoded
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("encodedNewPassword", userCaptor.getValue().getPassword());
        
        // Verify encoding happened
        verify(passwordEncoder, times(1)).encode("newPassword");
    }

    @Test
    void testUpdateUserWithEncodedPassword() {
        // Create a new instance of UserService for this test
        UserService localUserService = new UserService();
        
        // Manually inject the mocks
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(localUserService, "userRepository", userRepository);
        ReflectionTestUtils.setField(localUserService, "passwordEncoder", passwordEncoder);
        
        // Create a partial mock that will use real methods but with mocked dependencies
        UserService spyUserService = spy(localUserService);
        
        // Set up the stub for getUserById to avoid calling the repository directly
        doReturn(Optional.of(testUser)).when(spyUserService).getUserById(1);
        
        // Set up the repository save method
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Create the user to update
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setName("Updated Name");
        updatedUser.setPassword("$2a$10$encodedPassword");

        // Act
        User result = spyUserService.updateUser(updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        
        // Capture the saved user to verify the password was not encoded again
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("$2a$10$encodedPassword", userCaptor.getValue().getPassword());
        
        // Verify encoding didn't happen
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testDeleteUser() {
        // Create a new instance of UserService for this test
        UserService localUserService = new UserService();
        
        // Manually inject the mocks
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(localUserService, "userRepository", userRepository);
        
        // Create a partial mock that will use real methods but with mocked dependencies
        UserService spyUserService = spy(localUserService);
        
        // Set up the stub for getUserById to avoid calling the repository directly
        doReturn(Optional.of(testUser)).when(spyUserService).getUserById(1);
        
        // Set up the deleteById method
        doNothing().when(userRepository).deleteById(anyInt());

        // Act
        spyUserService.deleteUser(1);

        // Assert
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void testFindUsersWithNurseRoleButNoProfile() {
        // Arrange
        Role nurseRole = new Role();
        nurseRole.setId(2);
        nurseRole.setName(Role.RoleName.NURSE);

        User nurseUser = new User();
        nurseUser.setId(2);
        nurseUser.setEmail("nurse@example.com");
        Set<Role> nurseRoles = new HashSet<>();
        nurseRoles.add(nurseRole);
        nurseUser.setRoles(nurseRoles);

        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        userList.add(nurseUser);

        when(roleRepository.findByName(eq(Role.RoleName.NURSE))).thenReturn(Optional.of(nurseRole));
        when(userRepository.findAll()).thenReturn(userList);
        when(nurseRepository.findByUser(eq(nurseUser))).thenReturn(Optional.empty());
        
        // Act
        List<User> result = userService.findUsersWithNurseRoleButNoProfile();

        // Assert
        assertEquals(1, result.size());
        assertEquals(nurseUser.getId(), result.get(0).getId());
        verify(roleRepository, times(1)).findByName(Role.RoleName.NURSE);
        verify(userRepository, times(1)).findAll();
        verify(nurseRepository, times(1)).findByUser(nurseUser);
    }

    @Test
    void testGetRecentRegistrations() {
        // Arrange
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        LocalDateTime twoMonthsAgo = today.minusMonths(2);

        User recentUser1 = new User();
        recentUser1.setId(2);
        recentUser1.setCreatedAt(today);

        User recentUser2 = new User();
        recentUser2.setId(3);
        recentUser2.setCreatedAt(yesterday);

        User oldUser = new User();
        oldUser.setId(4);
        oldUser.setCreatedAt(twoMonthsAgo);

        List<User> userList = new ArrayList<>();
        userList.add(recentUser1);
        userList.add(recentUser2);
        userList.add(oldUser);
        userList.add(testUser);

        when(userRepository.findAll()).thenReturn(userList);
        
        // Act
        List<User> result = userService.getRecentRegistrations();

        // Assert
        assertEquals(3, result.size()); // Only the users with recent creation dates
        assertEquals(recentUser1.getId(), result.get(0).getId()); // Most recent first
        verify(userRepository, times(1)).findAll();
    }
    
    // Helper class to access private fields
    private static class ReflectionTestUtils {
        public static void setField(Object target, String fieldName, Object value) {
            try {
                java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field: " + fieldName, e);
            }
        }
    }
} 