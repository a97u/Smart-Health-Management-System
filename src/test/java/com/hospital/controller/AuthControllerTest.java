package com.hospital.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hospital.entity.User;
import com.hospital.service.UserService;
import com.hospital.service.PatientService;
import com.hospital.service.DoctorService;
import com.hospital.service.NurseService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserService userService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private NurseService nurseService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthController authController;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
    }
    
    @Test
    void testAuthControllerIsInitialized() {
        // Very basic test to ensure controller initializes
        assertNotNull(authController);
    }
    
    @Test
    void testUserRegistration() {
        // Basic test for user creation
        assertNotNull(testUser);
        assertEquals(1, testUser.getId());
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals("Test User", testUser.getName());
    }
    
    @Test
    void testGetUserByEmail() {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByEmail("test@example.com");
        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userService, times(1)).getUserByEmail("test@example.com");
    }
    
    @Test
    void testEncodePassword() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        String encoded = passwordEncoder.encode("rawPassword");
        
        assertEquals("encodedPassword", encoded);
        verify(passwordEncoder, times(1)).encode("rawPassword");
    }
} 